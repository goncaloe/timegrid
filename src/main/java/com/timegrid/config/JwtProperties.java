package com.timegrid.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timegrid.security.jwt")
public record JwtProperties(
        String secret,
        Duration accessTtl,
        Duration refreshTtl) {
}
