package com.timegrid.dto;

import com.timegrid.security.AppUserDetails;
import java.util.List;
import java.util.UUID;

public record MeResponse(
        UUID userId,
        String username,
        String fullName,
        UUID organizationId,
        String organizationName,
        List<String> roles,
        List<String> permissions) {

    public static MeResponse from(AppUserDetails user) {
        return new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getOrganizationId(),
                user.getOrganizationName(),
                user.getRoleNames(),
                user.getPermissionNames());
    }

}