package com.timegrid.dto;

import com.timegrid.security.AppUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String fullName,
        UUID organizationId,
        String organizationName,
        List<String> roles,
        List<String> permissions,
        String accessToken,
        String refreshToken,
        Instant expiresAt) {

    public static AuthResponse from(AppUserDetails user, String accessToken, Instant expiresAt,
            String refreshToken) {
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getOrganizationId(),
                user.getOrganizationName(),
                user.getRoleNames(),
                user.getPermissionNames(),
                accessToken,
                refreshToken,
                expiresAt);
    }

}