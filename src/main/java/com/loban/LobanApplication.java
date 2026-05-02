package com.loban;

import com.loban.config.DotEnvLoader;
import com.loban.config.CloudinaryProperties;
import com.loban.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CloudinaryProperties.class})
public class LobanApplication {

    public static void main(String[] args) {
        DotEnvLoader.load();
        SpringApplication.run(LobanApplication.class, args);
    }
}
