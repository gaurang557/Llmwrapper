package com.gaurang.llmwrapper.dto;

public record HealthStatus(
        String provider,
        boolean reachable,
        String detail,
        long latencyMs
) {
}
