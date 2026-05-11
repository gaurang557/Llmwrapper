package com.gaurang.llmwrapper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChatMessage(
        @NotBlank @Pattern(regexp = "system|user|assistant") String role,
        @NotBlank String content
) {
}
