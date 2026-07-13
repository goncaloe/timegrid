package com.timegrid.dto;

import com.timegrid.entity.LogEvent;
import com.timegrid.enums.LogAction;
import java.time.Instant;
import java.util.UUID;

public record LogEventResponse(
        UUID id,
        UUID userId,
        LogAction action,
        String targetType,
        UUID targetId,
        String details,
        String ipAddress,
        String userAgent,
        Instant createdAt) {

    public static LogEventResponse from(LogEvent event) {
        return new LogEventResponse(
                event.getId(),
                event.getUserId(),
                event.getAction(),
                event.getTargetType(),
                event.getTargetId(),
                event.getDetails(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getCreatedAt());
    }

}