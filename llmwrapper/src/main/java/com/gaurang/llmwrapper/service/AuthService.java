package com.gaurang.llmwrapper.service;

import com.gaurang.llmwrapper.dto.AuthResponse;
import com.gaurang.llmwrapper.dto.LoginRequest;
import com.gaurang.llmwrapper.dto.SignupRequest;
import com.gaurang.llmwrapper.dto.UserProfile;
import com.gaurang.llmwrapper.exception.ApiException;
import com.gaurang.llmwrapper.model.User;
import com.gaurang.llmwrapper.repository.UserRepository;
import com.gaurang.llmwrapper.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest req) {
        String username = req.username().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "username already taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setLastLoginAt(Instant.now());
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, jwtService.getExpirationMs(), user.getUsername(), user.getId());
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String username = req.username().trim().toLowerCase();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, jwtService.getExpirationMs(), user.getUsername(), user.getId());
    }

    @Transactional(readOnly = true)
    public UserProfile profile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getTokenUsage(),
                user.getRequestCount(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
