package com.arinazhou.featherlog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arinazhou.featherlog.care.CareTaskService;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HealthMonitoringApiTest extends ApiTestSupport {

    @Autowired
    CareTaskService careTaskService;

    @BeforeEach
    void resetClock() {
        clock().set(START);
    }

    private JsonNode logWeight(String token, long birdId, double grams) throws Exception {
        String body = "{\"grams\":" + grams + ",\"measuredAt\":\"" + clock.instant() + "\"}";
        return readJson(mvc.perform(authed(post("/api/birds/" + birdId + "/weights"), token).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void steadyWeightLossRaisesOneCriticalAlertThatCanBeResolved() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");

        // Two weeks of stable ~35 g weigh-ins.
        for (double g : new double[] {35.0, 35.2, 34.9, 35.1, 35.0}) {
            logWeight(token, birdId, g);
            clock().advance(Duration.ofDays(3));
        }
        clock().set(START.plus(Duration.ofDays(20)));

        // Then a week of decline.
        JsonNode first = logWeight(token, birdId, 32.0);
        clock().advance(Duration.ofDays(2));
        JsonNode second = logWeight(token, birdId, 31.0);

        assertThat(first.at("/assessment/status").asText()).isEqualTo("WATCH");
        assertThat(first.at("/newAlerts/0/type").asText()).isEqualTo("WEIGHT_DROP");
        assertThat(second.at("/assessment/status").asText()).isEqualTo("CONCERN");
        // An open WEIGHT_DROP alert already exists, so no duplicate is created.
        assertThat(second.get("newAlerts")).isEmpty();

        JsonNode alerts = readJson(mvc.perform(authed(get("/api/alerts"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn().getResponse().getContentAsString());
        long alertId = alerts.get(0).get("id").asLong();

        mvc.perform(authed(get("/api/birds/" + birdId + "/health"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight.status").value("CONCERN"))
                .andExpect(jsonPath("$.openAlerts", hasSize(1)));

        mvc.perform(authed(post("/api/alerts/" + alertId + "/resolve"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedAt").exists());
        mvc.perform(authed(get("/api/alerts"), token)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(authed(get("/api/alerts?open=false"), token)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void weightHistoryIsPagedNewestFirst() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");
        for (int i = 0; i < 5; i++) {
            logWeight(token, birdId, 35.0 + i * 0.1);
            clock().advance(Duration.ofDays(1));
        }
        mvc.perform(authed(get("/api/birds/" + birdId + "/weights?page=0&size=2"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].grams").value(35.4))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void overdueCareTasksRaiseAlertsUntilCompleted() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");

        // Water and food are due daily; let 30 hours pass.
        clock().advance(Duration.ofHours(30));
        int opened = careTaskService.raiseOverdueAlerts();
        assertThat(opened).isGreaterThanOrEqualTo(2);
        // Running the job again does not duplicate alerts.
        assertThat(careTaskService.raiseOverdueAlerts()).isZero();

        JsonNode tasks = readJson(mvc.perform(authed(get("/api/birds/" + birdId + "/care-tasks"), token))
                .andReturn().getResponse().getContentAsString());
        JsonNode water = null;
        for (JsonNode t : tasks) {
            if (t.get("type").asText().equals("FRESH_WATER")) {
                water = t;
            }
        }
        assertThat(water).isNotNull();
        assertThat(water.get("overdue").asBoolean()).isTrue();

        mvc.perform(authed(post("/api/birds/" + birdId + "/care-tasks/" + water.get("id").asLong() + "/complete"),
                        token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdue").value(false))
                .andExpect(jsonPath("$.nextDueAt").value(clock.instant().plus(Duration.ofDays(1)).toString()));

        JsonNode openAlerts = readJson(mvc.perform(authed(get("/api/alerts"), token))
                .andReturn().getResponse().getContentAsString());
        for (JsonNode a : openAlerts) {
            if (a.get("birdId").asLong() == birdId) {
                assertThat(a.get("careTaskId").asLong()).isNotEqualTo(water.get("id").asLong());
            }
        }
    }

    @Test
    void customCareTaskUsesTypeDefaultsWhenOmitted() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");
        mvc.perform(authed(post("/api/birds/" + birdId + "/care-tasks"), token)
                        .content("{\"type\":\"CUSTOM\",\"title\":\"Rotate foraging toys\",\"intervalDays\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Rotate foraging toys"))
                .andExpect(jsonPath("$.nextDueAt").value(Instant.parse(clock.instant().toString())
                        .plus(Duration.ofDays(3)).toString()));
    }
}
