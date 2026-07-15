package com.timegrid.dto;

import com.timegrid.entity.Notification;
import com.timegrid.enums.NotificationCategory;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationCategory category,
        String title,
        String message,
        String link,
        boolean read,
        Instant readAt,
        Instant createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCategory(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt());
    }

}