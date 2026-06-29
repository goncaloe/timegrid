package com.timegrid.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(min = 8) String password,
        @NotNull UUID organizationId,
        UUID teamId) {
}
