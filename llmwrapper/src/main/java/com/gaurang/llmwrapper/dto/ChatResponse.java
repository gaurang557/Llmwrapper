package com.gaurang.llmwrapper.dto;

public record ChatResponse(
        String provider,
        String model,
        String content,
        long promptTokens,
        long completionTokens,
        long totalTokens
) {
}
