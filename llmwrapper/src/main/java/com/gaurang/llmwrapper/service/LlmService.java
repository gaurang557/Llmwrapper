package com.gaurang.llmwrapper.service;

import com.gaurang.llmwrapper.config.LlmProperties;
import com.gaurang.llmwrapper.dto.ChatMessage;
import com.gaurang.llmwrapper.dto.ChatRequest;
import com.gaurang.llmwrapper.dto.ChatResponse;
import com.gaurang.llmwrapper.dto.HealthStatus;
import com.gaurang.llmwrapper.exception.ApiException;
import com.gaurang.llmwrapper.service.provider.LlmProvider;
import com.gaurang.llmwrapper.service.provider.ProviderRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmService {

    private final ProviderRegistry registry;
    private final InputPreprocessor preprocessor;
    private final UsageService usageService;

    public LlmService(ProviderRegistry registry, InputPreprocessor preprocessor, UsageService usageService) {
        this.registry = registry;
        this.preprocessor = preprocessor;
        this.usageService = usageService;
    }

    public ChatResponse chat(ChatRequest request, Long userId, String clientIp) {
        LlmProvider provider = registry.resolve(request.provider());
        LlmProperties.ProviderConfig cfg = registry.configFor(provider.name());
        usageService.enforceLimits(provider.name(), cfg, userId, clientIp);

        List<ChatMessage> cleaned = preprocessor.clean(request.messages());

        boolean success = false;
        ChatResponse response = null;
        try {
            response = provider.chat(request.model(), cleaned, request.temperature(), request.maxTokens());
            success = true;
            return response;
        } finally {
            long tokens = response == null ? 0 : response.totalTokens();
            String model = response == null
                    ? (request.model() != null ? request.model() : provider.defaultModel())
                    : response.model();
            usageService.record(provider.name(), model, userId, clientIp, tokens, success);
        }
    }

    public HealthStatus health(String providerName) {
        LlmProvider provider = registry.get(providerName);
        if (provider == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "unknown provider: " + providerName);
        }
        if (!provider.isConfigured()) {
            return new HealthStatus(providerName, false, "provider not configured", 0);
        }
        long start = System.currentTimeMillis();
        String err = provider.ping();
        long elapsed = System.currentTimeMillis() - start;
        return new HealthStatus(providerName, err == null, err == null ? "ok" : err, elapsed);
    }
}
