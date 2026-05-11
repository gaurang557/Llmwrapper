package com.gaurang.llmwrapper.service.provider;

import com.gaurang.llmwrapper.dto.ChatMessage;
import com.gaurang.llmwrapper.dto.ChatResponse;

import java.util.List;

public interface LlmProvider {

    String name();

    boolean isConfigured();

    String defaultModel();

    ChatResponse chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens);

    /** Reachability check — returns null on success or an error message on failure. */
    String ping();
}
