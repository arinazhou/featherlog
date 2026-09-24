package com.arinazhou.featherlog.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arinazhou.featherlog.MutableClock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiTestSupport.ClockConfig.class)
abstract class ApiTestSupport {

    static final Instant START = Instant.parse("2026-06-01T08:00:00Z");

    @TestConfiguration
    static class ClockConfig {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(START);
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    Clock clock;

    MutableClock clock() {
        return (MutableClock) clock;
    }

    String registerUser() throws Exception {
        String body = json.writeValueAsString(Map.of(
                "email", "owner-" + UUID.randomUUID() + "@example.com",
                "password", "correct-horse",
                "displayName", "Test Owner"));
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asText();
    }

    long createBird(String token, String name) throws Exception {
        JsonNode bird = readJson(mvc.perform(authed(post("/api/birds"), token)
                        .content(json.writeValueAsString(Map.of("name", name, "colorMutation", "Sky Blue"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return bird.get("id").asLong();
    }

    MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    JsonNode readJson(String s) throws Exception {
        return json.readTree(s);
    }
}
