package com.loban.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loban.cloudinary")
public record CloudinaryProperties(
        /** Si faux, les photos restent en data URL en base (mode dev sans Cloudinary). */
        boolean enabled,
        String cloudName,
        String apiKey,
        String apiSecret
) {}
