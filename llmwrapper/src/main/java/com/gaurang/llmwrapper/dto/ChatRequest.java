package com.gaurang.llmwrapper.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChatRequest(
        String provider,
        String model,
        @NotEmpty @Valid List<ChatMessage> messages,
        Double temperature,
        Integer maxTokens
) {
}
