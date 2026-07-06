package com.timegrid.dto;

import com.timegrid.entity.TimeEntry;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimeEntryResponse(
        UUID id,
        UUID userId,
        String userFullName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        int breakMinutes,
        long durationMinutes,
        TimeEntryType type,
        String notes,
        TimeEntryStatus status,
        String rejectionReason,
        UUID approverId,
        Instant approvedAt,
        Instant createdAt) {

    public static TimeEntryResponse from(TimeEntry entry) {
        long durationMinutes = Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes()
                - entry.getBreakMinutes();

        return new TimeEntryResponse(
                entry.getId(),
                entry.getUser().getId(),
                entry.getUser().getFullName(),
                entry.getWorkDate(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getBreakMinutes(),
                durationMinutes,
                entry.getType(),
                entry.getNotes(),
                entry.getStatus(),
                entry.getRejectionReason(),
                entry.getApprover() != null ? entry.getApprover().getId() : null,
                entry.getApprovedAt(),
                entry.getCreatedAt());
    }

}
