package com.gaurang.llmwrapper.service.provider;

import com.gaurang.llmwrapper.config.LlmProperties;

public abstract class AbstractLlmProvider implements LlmProvider {

    protected final LlmProperties.ProviderConfig config;
    protected final String providerName;

    protected AbstractLlmProvider(String providerName, LlmProperties.ProviderConfig config) {
        this.providerName = providerName;
        this.config = config;
    }

    @Override
    public String name() {
        return providerName;
    }

    @Override
    public boolean isConfigured() {
        return config != null && config.isConfigured();
    }

    @Override
    public String defaultModel() {
        return config == null ? null : config.getDefaultModel();
    }
}
