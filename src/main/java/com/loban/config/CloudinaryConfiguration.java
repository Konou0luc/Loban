package com.loban.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "loban.cloudinary", name = "enabled", havingValue = "true")
    public Cloudinary cloudinary(CloudinaryProperties p) {
        if (isBlank(p.cloudName()) || isBlank(p.apiKey()) || isBlank(p.apiSecret())) {
            throw new IllegalStateException(
                    "Cloudinary est activé (loban.cloudinary.enabled=true / CLOUDINARY_ENABLED=true) mais il manque "
                            + "CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY ou CLOUDINARY_API_SECRET dans l'environnement.");
        }
        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", p.cloudName(),
                        "api_key", p.apiKey(),
                        "api_secret", p.apiSecret()));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
