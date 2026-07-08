package com.timegrid.dto;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AbsenceRequestResponse(
        UUID id,
        UUID userId,
        String userFullName,
        LocalDate startDate,
        LocalDate endDate,
        long workingDays,
        AbsenceType type,
        String notes,
        AbsenceStatus status,
        String rejectionReason,
        UUID approverId,
        Instant approvedAt,
        Instant createdAt) {

    public static AbsenceRequestResponse from(AbsenceRequest request) {
        return new AbsenceRequestResponse(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getFullName(),
                request.getStartDate(),
                request.getEndDate(),
                workingDays(request.getStartDate(), request.getEndDate()),
                request.getType(),
                request.getNotes(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getApprover() != null ? request.getApprover().getId() : null,
                request.getApprovedAt(),
                request.getCreatedAt());
    }

    private static long workingDays(LocalDate startDate, LocalDate endDate) {
        long days = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }

}
