package com.tourist.Yatra_secure;

import com.tourist.Yatra_secure.service.SafetyAssistantFallback;
import com.tourist.Yatra_secure.service.SafetyAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class SafetyAssistantServiceTest {
    @Test
    void answersEmergencyQuestionWithoutProviderCredentials() {
        SafetyAssistantService service = new SafetyAssistantService(
                RestClient.builder(), new SafetyAssistantFallback(), "", "https://provider.test/v1", "test-model");

        Map<String, Object> response = service.ask("What should I do in an emergency?");

        assertEquals(true, response.get("available"));
        assertEquals("local", response.get("source"));
        assertEquals(true, response.get("message").toString().contains("local emergency services"));
    }

    @Test
    void usesLocalGuidanceWhenConfiguredProviderFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://provider.test/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        SafetyAssistantService service = new SafetyAssistantService(
                builder, new SafetyAssistantFallback(), "test-only-key", "https://provider.test/v1", "test-model");

        Map<String, Object> response = service.ask("How do YatraSecure safety zones work?");

        assertEquals(true, response.get("available"));
        assertEquals("local", response.get("source"));
        assertEquals(true, response.get("message").toString().contains("safety-zone map"));
        server.verify();
    }
}
