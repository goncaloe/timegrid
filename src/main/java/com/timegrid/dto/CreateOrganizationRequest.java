package com.timegrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) String timezone) {
}
