package com.timegrid.dto;

import com.timegrid.entity.WorkflowTransition;
import com.timegrid.enums.WorkflowEntityType;
import java.util.UUID;

public record WorkflowTransitionResponse(
        UUID id,
        WorkflowEntityType entityType,
        String fromStatus,
        String toStatus,
        String role) {

    public static WorkflowTransitionResponse from(WorkflowTransition transition) {
        return new WorkflowTransitionResponse(
                transition.getId(),
                transition.getEntityType(),
                transition.getFromStatus(),
                transition.getToStatus(),
                transition.getRole());
    }

}