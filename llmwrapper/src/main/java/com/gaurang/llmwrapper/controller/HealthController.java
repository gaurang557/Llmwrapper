package com.gaurang.llmwrapper.controller;

import com.gaurang.llmwrapper.dto.HealthStatus;
import com.gaurang.llmwrapper.service.LlmService;
import com.gaurang.llmwrapper.service.provider.LlmProvider;
import com.gaurang.llmwrapper.service.provider.ProviderRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final LlmService llmService;
    private final ProviderRegistry registry;

    public HealthController(LlmService llmService, ProviderRegistry registry) {
        this.llmService = llmService;
        this.registry = registry;
    }

    @GetMapping
    public Map<String, Object> root() {
        return Map.of("status", "ok");
    }

    @GetMapping("/llm")
    public List<HealthStatus> all() {
        List<HealthStatus> out = new ArrayList<>();
        for (LlmProvider p : registry.all()) {
            out.add(llmService.health(p.name()));
        }
        return out;
    }

    @GetMapping("/llm/{provider}")
    public HealthStatus single(@PathVariable String provider) {
        return llmService.health(provider);
    }
}
