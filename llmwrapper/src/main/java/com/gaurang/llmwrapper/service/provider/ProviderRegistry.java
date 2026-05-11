package com.gaurang.llmwrapper.service.provider;

import com.gaurang.llmwrapper.config.LlmProperties;
import com.gaurang.llmwrapper.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProviderRegistry {

    private final LlmProperties properties;
    private final Map<String, LlmProvider> providers = new LinkedHashMap<>();

    public ProviderRegistry(LlmProperties properties, RestClient llmRestClient) {
        this.properties = properties;
        for (Map.Entry<String, LlmProperties.ProviderConfig> e : properties.getProviders().entrySet()) {
            String key = e.getKey().toLowerCase();
            LlmProperties.ProviderConfig cfg = e.getValue();
            LlmProvider provider = switch (key) {
                case "openai" -> new OpenAiProvider(cfg, llmRestClient);
                case "anthropic" -> new AnthropicProvider(cfg, llmRestClient);
                case "gemini" -> new GeminiProvider(cfg, llmRestClient);
                default -> null;
            };
            if (provider != null) providers.put(key, provider);
        }
    }

    public LlmProvider resolve(String requested) {
        String name = (requested == null || requested.isBlank())
                ? properties.getDefaultProvider()
                : requested.toLowerCase();
        LlmProvider provider = providers.get(name);
        if (provider == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "unknown provider: " + name);
        }
        if (!provider.isConfigured()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "provider not configured (missing api key): " + name);
        }
        return provider;
    }

    public LlmProvider get(String name) {
        return providers.get(name == null ? null : name.toLowerCase());
    }

    public LlmProperties.ProviderConfig configFor(String name) {
        return properties.getProviders().get(name);
    }

    public Collection<LlmProvider> all() {
        return providers.values();
    }
}
