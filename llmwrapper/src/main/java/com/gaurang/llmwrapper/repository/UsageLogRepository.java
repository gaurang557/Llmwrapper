package com.gaurang.llmwrapper.repository;

import com.gaurang.llmwrapper.model.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    long countByProvider(String provider);

    long countByProviderAndClientIp(String provider, String clientIp);

    long countByProviderAndUserId(String provider, Long userId);
}
