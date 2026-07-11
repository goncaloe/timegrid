package com.timegrid.dto;

import com.timegrid.entity.StatusChange;
import java.time.Instant;
import java.util.UUID;

public record StatusChangeResponse(
        UUID id,
        String fromStatus,
        String toStatus,
        UUID changedBy,
        String comment,
        Instant changedAt) {

    public static StatusChangeResponse from(StatusChange change) {
        return new StatusChangeResponse(
                change.getId(),
                change.getFromStatus(),
                change.getToStatus(),
                change.getChangedBy(),
                change.getComment(),
                change.getChangedAt());
    }

}