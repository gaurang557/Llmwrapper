package com.gaurang.llmwrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String defaultProvider = "openai";
    private int requestTimeoutSeconds = 60;
    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();

    public String getDefaultProvider() { return defaultProvider; }
    public void setDefaultProvider(String defaultProvider) { this.defaultProvider = defaultProvider; }

    public int getRequestTimeoutSeconds() { return requestTimeoutSeconds; }
    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) { this.requestTimeoutSeconds = requestTimeoutSeconds; }

    public Map<String, ProviderConfig> getProviders() { return providers; }
    public void setProviders(Map<String, ProviderConfig> providers) { this.providers = providers; }

    public static class ProviderConfig {
        private boolean enabled = true;
        private String apiKey = "";
        private String baseUrl;
        private String defaultModel;
        private int anonymousLimit = 0;
        private int totalLimit = 0;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getDefaultModel() { return defaultModel; }
        public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }

        public int getAnonymousLimit() { return anonymousLimit; }
        public void setAnonymousLimit(int anonymousLimit) { this.anonymousLimit = anonymousLimit; }

        public int getTotalLimit() { return totalLimit; }
        public void setTotalLimit(int totalLimit) { this.totalLimit = totalLimit; }

        public boolean isConfigured() {
            return enabled && apiKey != null && !apiKey.isBlank();
        }
    }
}
