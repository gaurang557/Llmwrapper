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

public class OpenAiProvider extends AbstractLlmProvider {

    private final RestClient restClient;

    public OpenAiProvider(LlmProperties.ProviderConfig config, RestClient restClient) {
        super("openai", config);
        this.restClient = restClient;
    }

    @Override
    public ChatResponse chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens) {
        String chosenModel = (model == null || model.isBlank()) ? config.getDefaultModel() : model;
        List<Map<String, Object>> apiMessages = new ArrayList<>();
        for (ChatMessage m : messages) {
            apiMessages.add(Map.of("role", m.role(), "content", m.content()));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", chosenModel);
        body.put("messages", apiMessages);
        if (temperature != null) body.put("temperature", temperature);
        if (maxTokens != null) body.put("max_tokens", maxTokens);

        try {
            Map<?, ?> resp = restClient.post()
                    .uri(config.getBaseUrl() + "/chat/completions")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return parseOpenAiResponse(resp, chosenModel);
        } catch (HttpStatusCodeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "openai error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "openai unreachable: " + e.getMessage());
        }
    }

    @Override
    public String ping() {
        try {
            restClient.get()
                    .uri(config.getBaseUrl() + "/models")
                    .header("Authorization", "Bearer " + config.getApiKey())
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
    private ChatResponse parseOpenAiResponse(Map<?, ?> resp, String model) {
        if (resp == null) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "openai returned empty response");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "openai returned no choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = message == null ? "" : String.valueOf(message.getOrDefault("content", ""));

        Map<String, Object> usage = (Map<String, Object>) resp.get("usage");
        long prompt = numberOrZero(usage, "prompt_tokens");
        long completion = numberOrZero(usage, "completion_tokens");
        long total = numberOrZero(usage, "total_tokens");
        if (total == 0) total = prompt + completion;
        return new ChatResponse("openai", model, content, prompt, completion, total);
    }

    static long numberOrZero(Map<String, Object> map, String key) {
        if (map == null) return 0L;
        Object v = map.get(key);
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }
}
