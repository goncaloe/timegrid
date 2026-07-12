package com.timegrid.dto;

import com.timegrid.enums.WorkflowEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorkflowTransitionRequest(
        @NotNull WorkflowEntityType entityType,
        @NotBlank @Size(max = 20) String fromStatus,
        @NotBlank @Size(max = 20) String toStatus,
        @NotBlank @Size(max = 20) String role) {
}