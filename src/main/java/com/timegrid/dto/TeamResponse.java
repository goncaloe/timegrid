package com.timegrid.dto;

import com.timegrid.entity.Team;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        String description,
        UUID organizationId) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getOrganization().getId());
    }

}
