package com.timegrid.dto;

import com.timegrid.entity.Role;
import com.timegrid.entity.User;
import com.timegrid.enums.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        UserStatus status,
        UUID organizationId,
        UUID teamId,
        Set<String> roles,
        int dailyWorkMinutes,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getOrganization().getId(),
                user.getTeam() != null ? user.getTeam().getId() : null,
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getDailyWorkMinutes(),
                user.getCreatedAt());
    }

}