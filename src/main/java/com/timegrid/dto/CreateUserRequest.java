package com.timegrid.dto;

import java.util.UUID;

public record CreateUserRequest(
        String username,
        String email,
        String fullName,
        String password,
        UUID organizationId,
        UUID teamId) {
}
