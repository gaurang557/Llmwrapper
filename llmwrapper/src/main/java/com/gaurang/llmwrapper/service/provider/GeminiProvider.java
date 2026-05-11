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

public class GeminiProvider extends AbstractLlmProvider {

    private final RestClient restClient;

    public GeminiProvider(LlmProperties.ProviderConfig config, RestClient restClient) {
        super("gemini", config);
        this.restClient = restClient;
    }

    @Override
    public ChatResponse chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens) {
        String chosenModel = (model == null || model.isBlank()) ? config.getDefaultModel() : model;

        List<Map<String, Object>> contents = new ArrayList<>();
        StringBuilder system = new StringBuilder();
        for (ChatMessage m : messages) {
            if ("system".equals(m.role())) {
                if (system.length() > 0) system.append("\n\n");
                system.append(m.content());
                continue;
            }
            String role = "assistant".equals(m.role()) ? "model" : "user";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", m.content()))
            ));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", contents);
        if (system.length() > 0) {
            body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", system.toString()))));
        }
        Map<String, Object> genConfig = new LinkedHashMap<>();
        if (temperature != null) genConfig.put("temperature", temperature);
        if (maxTokens != null) genConfig.put("maxOutputTokens", maxTokens);
        if (!genConfig.isEmpty()) body.put("generationConfig", genConfig);

        try {
            Map<?, ?> resp = restClient.post()
                    .uri(config.getBaseUrl() + "/models/" + chosenModel + ":generateContent?key=" + config.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return parse(resp, chosenModel);
        } catch (HttpStatusCodeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "gemini error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "gemini unreachable: " + e.getMessage());
        }
    }

    @Override
    public String ping() {
        try {
            restClient.get()
                    .uri(config.getBaseUrl() + "/models?key=" + config.getApiKey())
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
            throw new ApiException(HttpStatus.BAD_GATEWAY, "gemini returned empty response");
        }
        StringBuilder text = new StringBuilder();
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) resp.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content != null) {
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null) {
                    for (Map<String, Object> part : parts) {
                        Object t = part.get("text");
                        if (t != null) text.append(t);
                    }
                }
            }
        }
        Map<String, Object> usage = (Map<String, Object>) resp.get("usageMetadata");
        long prompt = OpenAiProvider.numberOrZero(usage, "promptTokenCount");
        long completion = OpenAiProvider.numberOrZero(usage, "candidatesTokenCount");
        long total = OpenAiProvider.numberOrZero(usage, "totalTokenCount");
        if (total == 0) total = prompt + completion;
        return new ChatResponse("gemini", model, text.toString(), prompt, completion, total);
    }
}
