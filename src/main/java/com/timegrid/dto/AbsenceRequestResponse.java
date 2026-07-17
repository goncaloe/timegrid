package com.timegrid.dto;

import com.timegrid.common.WorkingDays;
import com.timegrid.entity.AbsenceRequest;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
        Instant createdAt,
        List<String> allowedActions) {

    public static AbsenceRequestResponse from(AbsenceRequest request, List<String> allowedActions) {
        return new AbsenceRequestResponse(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getFullName(),
                request.getStartDate(),
                request.getEndDate(),
                WorkingDays.between(request.getStartDate(), request.getEndDate()),
                request.getType(),
                request.getNotes(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getApprover() != null ? request.getApprover().getId() : null,
                request.getApprovedAt(),
                request.getCreatedAt(),
                allowedActions);
    }

}