package com.timegrid.dto;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AbsenceRequestResponse(
        UUID id,
        UUID userId,
        String userFullName,
        LocalDate startDate,
        LocalDate endDate,
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
                request.getType(),
                request.getNotes(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getApprover() != null ? request.getApprover().getId() : null,
                request.getApprovedAt(),
                request.getCreatedAt());
    }

}
