package com.timegrid.dto;

import com.timegrid.enums.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) String timezone,
        @NotNull OrganizationStatus status) {
}
