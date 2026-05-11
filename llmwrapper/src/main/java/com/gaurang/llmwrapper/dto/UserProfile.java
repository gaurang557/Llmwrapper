package com.gaurang.llmwrapper.dto;

import java.time.Instant;

public record UserProfile(
        Long id,
        String username,
        String email,
        long tokenUsage,
        long requestCount,
        Instant createdAt,
        Instant lastLoginAt
) {
}
