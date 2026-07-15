package com.timegrid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timegrid.notifications")
public record NotificationProperties(
        String baseUrl,
        Email email) {

    public record Email(boolean enabled, String from) {
    }

}