package com.tourist.Yatra_secure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Service
public class SafetyAssistantService {
    private static final Logger log = LoggerFactory.getLogger(SafetyAssistantService.class);
    private final RestClient client;
    private final String apiKey;
    private final String model;
    private final SafetyAssistantFallback fallback;

    public SafetyAssistantService(RestClient.Builder builder,
                                  SafetyAssistantFallback fallback,
                                  @Value("${assistant.api-key:}") String apiKey,
                                  @Value("${assistant.base-url:https://api.openai.com/v1}") String baseUrl,
                                  @Value("${assistant.model:gpt-4o-mini}") String model) {
        this.client = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
        this.fallback = fallback;
    }

    public Map<String, Object> ask(String prompt) {
        if (apiKey == null || apiKey.isBlank()) return localResponse(prompt);
        try {
            Map<?, ?> body = client.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", model,
                            "messages", List.of(
                                    Map.of("role", "system", "content", "Give cautious general travel suggestions. Never claim to dispatch emergency services, monitor locations, or guarantee safety. For immediate danger, tell the user to contact local emergency services directly."),
                                    Map.of("role", "user", "content", prompt)
                            )
                    ))
                    .retrieve()
                    .body(Map.class);
            Object choicesValue = body == null ? null : body.get("choices");
            if (!(choicesValue instanceof List<?> choices) || choices.isEmpty()) {
                throw new IllegalStateException("Assistant provider returned no choices");
            }
            Object messageValue = choices.get(0) instanceof Map<?, ?> choice ? choice.get("message") : null;
            Object content = messageValue instanceof Map<?, ?> message ? message.get("content") : null;
            if (!(content instanceof String text) || text.isBlank()) {
                throw new IllegalStateException("Assistant provider returned an empty answer");
            }
            return Map.of("available", true, "source", "provider", "message", text);
        } catch (Exception providerError) {
            log.warn("Safety assistant provider failed; using local guidance ({})", providerError.getClass().getSimpleName());
            return localResponse(prompt);
        }
    }

    private Map<String, Object> localResponse(String prompt) {
        try {
            return Map.of("available", true, "source", "local", "message", fallback.respond(prompt));
        } catch (RuntimeException fallbackError) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Safety guidance is temporarily unavailable. Contact local emergency services directly if this is urgent.");
        }
    }
}
