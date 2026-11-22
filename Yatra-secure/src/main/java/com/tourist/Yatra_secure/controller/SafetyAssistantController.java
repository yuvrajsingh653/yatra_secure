package com.tourist.Yatra_secure.controller;

import com.tourist.Yatra_secure.service.SafetyAssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class SafetyAssistantController {
    private final SafetyAssistantService assistant;

    public SafetyAssistantController(SafetyAssistantService assistant) {
        this.assistant = assistant;
    }

    public record AskRequest(@NotBlank @Size(max = 2000) String message) {}

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public Map<String, Object> ask(@Valid @RequestBody AskRequest request) {
        return assistant.ask(request.message());
    }
}
