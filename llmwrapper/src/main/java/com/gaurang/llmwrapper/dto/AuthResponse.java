package com.gaurang.llmwrapper.dto;

public record AuthResponse(
        String token,
        long expiresInMs,
        String username,
        Long userId
) {
}
