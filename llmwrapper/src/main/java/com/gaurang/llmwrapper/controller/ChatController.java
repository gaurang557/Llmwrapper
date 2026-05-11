package com.gaurang.llmwrapper.controller;

import com.gaurang.llmwrapper.dto.ChatRequest;
import com.gaurang.llmwrapper.dto.ChatResponse;
import com.gaurang.llmwrapper.security.AuthenticatedUser;
import com.gaurang.llmwrapper.service.ClientIpResolver;
import com.gaurang.llmwrapper.service.LlmService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final LlmService llmService;
    private final ClientIpResolver ipResolver;

    public ChatController(LlmService llmService, ClientIpResolver ipResolver) {
        this.llmService = llmService;
        this.ipResolver = ipResolver;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest body,
                             @AuthenticationPrincipal AuthenticatedUser user,
                             HttpServletRequest request) {
        Long userId = user == null ? null : user.id();
        String clientIp = ipResolver.resolve(request);
        return llmService.chat(body, userId, clientIp);
    }
}
