package com.arinazhou.featherlog.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class BirdApiTest extends ApiTestSupport {

    @Test
    void requestsWithoutTokenAreRejected() throws Exception {
        mvc.perform(get("/api/birds")).andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsConflictAndBadPasswordIsUnauthorized() throws Exception {
        String body = json.writeValueAsString(Map.of(
                "email", "dup@example.com", "password", "correct-horse", "displayName", "Dup"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "DUP@example.com", "password", "correct-horse"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "dup@example.com", "password", "wrong-pass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createdBirdGetsBudgieDefaultsAndCareSchedule() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");

        mvc.perform(authed(get("/api/birds/" + birdId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value("Budgerigar"))
                .andExpect(jsonPath("$.sex").value("UNKNOWN"))
                .andExpect(jsonPath("$.targetMinGrams").value(30.0))
                .andExpect(jsonPath("$.targetMaxGrams").value(40.0));

        mvc.perform(authed(get("/api/birds/" + birdId + "/care-tasks"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].intervalDays").value(1));
    }

    @Test
    void usersCannotSeeOrModifyEachOthersBirds() throws Exception {
        String alice = registerUser();
        String bob = registerUser();
        long aliceBird = createBird(alice, "Sky");

        mvc.perform(authed(get("/api/birds/" + aliceBird), bob)).andExpect(status().isNotFound());
        mvc.perform(authed(get("/api/birds/" + aliceBird + "/weights"), bob)).andExpect(status().isNotFound());
        mvc.perform(authed(delete("/api/birds/" + aliceBird), bob)).andExpect(status().isNotFound());
        mvc.perform(authed(get("/api/birds"), bob)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(authed(get("/api/birds"), alice)).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void validationErrorsListOffendingFields() throws Exception {
        String token = registerUser();
        mvc.perform(authed(post("/api/birds"), token).content("{\"name\":\"\",\"targetMinGrams\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.targetMinGrams").exists());
    }

    @Test
    void invalidTargetRangeIsBadRequest() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");
        mvc.perform(authed(put("/api/birds/" + birdId), token)
                        .content("{\"name\":\"Sky\",\"targetMinGrams\":40,\"targetMaxGrams\":30}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingABirdRemovesIt() throws Exception {
        String token = registerUser();
        long birdId = createBird(token, "Sky");
        mvc.perform(authed(post("/api/birds/" + birdId + "/weights"), token).content("{\"grams\":35.0}"))
                .andExpect(status().isCreated());
        mvc.perform(authed(delete("/api/birds/" + birdId), token)).andExpect(status().isNoContent());
        mvc.perform(authed(get("/api/birds/" + birdId), token)).andExpect(status().isNotFound());
    }
}
