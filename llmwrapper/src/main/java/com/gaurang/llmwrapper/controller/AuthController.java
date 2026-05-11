package com.gaurang.llmwrapper.controller;

import com.gaurang.llmwrapper.dto.AuthResponse;
import com.gaurang.llmwrapper.dto.LoginRequest;
import com.gaurang.llmwrapper.dto.SignupRequest;
import com.gaurang.llmwrapper.dto.UserProfile;
import com.gaurang.llmwrapper.exception.ApiException;
import com.gaurang.llmwrapper.security.AuthenticatedUser;
import com.gaurang.llmwrapper.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserProfile me(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "authentication required");
        }
        return authService.profile(user.id());
    }
}
