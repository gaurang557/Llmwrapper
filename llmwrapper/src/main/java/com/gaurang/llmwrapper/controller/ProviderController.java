package com.gaurang.llmwrapper.controller;

import com.gaurang.llmwrapper.config.LlmProperties;
import com.gaurang.llmwrapper.dto.ProviderInfo;
import com.gaurang.llmwrapper.service.UsageService;
import com.gaurang.llmwrapper.service.provider.LlmProvider;
import com.gaurang.llmwrapper.service.provider.ProviderRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProviderController {

    private final ProviderRegistry registry;
    private final UsageService usageService;

    public ProviderController(ProviderRegistry registry, UsageService usageService) {
        this.registry = registry;
        this.usageService = usageService;
    }

    @GetMapping("/providers")
    public List<ProviderInfo> list() {
        List<ProviderInfo> result = new ArrayList<>();
        for (LlmProvider provider : registry.all()) {
            LlmProperties.ProviderConfig cfg = registry.configFor(provider.name());
            long used = usageService.totalUsed(provider.name());
            result.add(new ProviderInfo(
                    provider.name(),
                    provider.defaultModel(),
                    provider.isConfigured(),
                    cfg.getAnonymousLimit(),
                    cfg.getTotalLimit(),
                    used
            ));
        }
        return result;
    }
}
