package com.arinazhou.featherlog.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "featherlog.jwt")
public record JwtProperties(String secret, Duration ttl) {

    public JwtProperties {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalArgumentException("featherlog.jwt.secret must be at least 32 bytes");
        }
        if (ttl == null) {
            ttl = Duration.ofHours(12);
        }
    }
}
