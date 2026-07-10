package com.timegrid.dto;

import com.timegrid.entity.Role;
import java.util.UUID;

public record RoleResponse(UUID id, String name) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getName());
    }

}
