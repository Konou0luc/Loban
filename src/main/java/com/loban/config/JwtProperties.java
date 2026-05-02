package com.loban.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loban.jwt")
public record JwtProperties(
        String secret,
        long expirationMs
) {
}
