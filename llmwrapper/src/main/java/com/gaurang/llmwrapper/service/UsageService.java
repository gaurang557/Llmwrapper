package com.gaurang.llmwrapper.service;

import com.gaurang.llmwrapper.config.LlmProperties;
import com.gaurang.llmwrapper.exception.RateLimitException;
import com.gaurang.llmwrapper.model.UsageLog;
import com.gaurang.llmwrapper.model.User;
import com.gaurang.llmwrapper.repository.UsageLogRepository;
import com.gaurang.llmwrapper.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageService {

    private final UsageLogRepository usageLogRepository;
    private final UserRepository userRepository;

    public UsageService(UsageLogRepository usageLogRepository, UserRepository userRepository) {
        this.usageLogRepository = usageLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public void enforceLimits(String provider, LlmProperties.ProviderConfig cfg, Long userId, String clientIp) {
        long totalUsed = usageLogRepository.countByProvider(provider);
        if (cfg.getTotalLimit() > 0 && totalUsed >= cfg.getTotalLimit()) {
            throw new RateLimitException("provider '" + provider + "' total request limit reached");
        }
        if (userId == null) {
            if (cfg.getAnonymousLimit() <= 0) {
                throw new RateLimitException("anonymous access disabled for provider '" + provider + "'");
            }
            long anonUsed = usageLogRepository.countByProviderAndClientIp(provider, clientIp);
            if (anonUsed >= cfg.getAnonymousLimit()) {
                throw new RateLimitException(
                        "anonymous limit reached for provider '" + provider + "' — please sign in");
            }
        }
    }

    @Transactional
    public void record(String provider, String model, Long userId, String clientIp, long tokens, boolean success) {
        UsageLog log = new UsageLog();
        log.setProvider(provider);
        log.setModel(model);
        log.setUserId(userId);
        log.setClientIp(clientIp);
        log.setTokensUsed(tokens);
        log.setSuccess(success);
        usageLogRepository.save(log);

        if (userId != null && success) {
            userRepository.findById(userId).ifPresent(u -> {
                u.setTokenUsage(u.getTokenUsage() + tokens);
                u.setRequestCount(u.getRequestCount() + 1);
                userRepository.save(u);
            });
        }
    }

    @Transactional(readOnly = true)
    public long totalUsed(String provider) {
        return usageLogRepository.countByProvider(provider);
    }
}
