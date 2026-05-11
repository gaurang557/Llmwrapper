package com.gaurang.llmwrapper.dto;

public record ProviderInfo(
        String name,
        String defaultModel,
        boolean configured,
        int anonymousLimit,
        int totalLimit,
        long totalUsed
) {
}
