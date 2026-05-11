package com.gaurang.llmwrapper.service.provider;

import com.gaurang.llmwrapper.config.LlmProperties;
import com.gaurang.llmwrapper.dto.ChatMessage;
import com.gaurang.llmwrapper.dto.ChatResponse;
import com.gaurang.llmwrapper.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnthropicProvider extends AbstractLlmProvider {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestClient restClient;

    public AnthropicProvider(LlmProperties.ProviderConfig config, RestClient restClient) {
        super("anthropic", config);
        this.restClient = restClient;
    }

    @Override
    public ChatResponse chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens) {
        String chosenModel = (model == null || model.isBlank()) ? config.getDefaultModel() : model;

        StringBuilder systemBuilder = new StringBuilder();
        List<Map<String, Object>> apiMessages = new ArrayList<>();
        for (ChatMessage m : messages) {
            if ("system".equals(m.role())) {
                if (systemBuilder.length() > 0) systemBuilder.append("\n\n");
                systemBuilder.append(m.content());
            } else {
                apiMessages.add(Map.of("role", m.role(), "content", m.content()));
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", chosenModel);
        body.put("messages", apiMessages);
        body.put("max_tokens", maxTokens != null ? maxTokens : 1024);
        if (systemBuilder.length() > 0) body.put("system", systemBuilder.toString());
        if (temperature != null) body.put("temperature", temperature);

        try {
            Map<?, ?> resp = restClient.post()
                    .uri(config.getBaseUrl() + "/messages")
                    .header("x-api-key", config.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return parse(resp, chosenModel);
        } catch (HttpStatusCodeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "anthropic error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "anthropic unreachable: " + e.getMessage());
        }
    }

    @Override
    public String ping() {
        try {
            restClient.post()
                    .uri(config.getBaseUrl() + "/messages")
                    .header("x-api-key", config.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", config.getDefaultModel(),
                            "max_tokens", 1,
                            "messages", List.of(Map.of("role", "user", "content", "ping"))
                    ))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        } catch (HttpStatusCodeException e) {
            return "http " + e.getStatusCode();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private ChatResponse parse(Map<?, ?> resp, String model) {
        if (resp == null) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "anthropic returned empty response");
        }
        List<Map<String, Object>> content = (List<Map<String, Object>>) resp.get("content");
        StringBuilder text = new StringBuilder();
        if (content != null) {
            for (Map<String, Object> block : content) {
                if ("text".equals(block.get("type"))) {
                    text.append(block.getOrDefault("text", ""));
                }
            }
        }
        Map<String, Object> usage = (Map<String, Object>) resp.get("usage");
        long prompt = OpenAiProvider.numberOrZero(usage, "input_tokens");
        long completion = OpenAiProvider.numberOrZero(usage, "output_tokens");
        return new ChatResponse("anthropic", model, text.toString(), prompt, completion, prompt + completion);
    }
}
