package com.timegrid.dto;

import com.timegrid.entity.Organization;
import com.timegrid.enums.OrganizationStatus;
import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String timezone,
        OrganizationStatus status,
        Instant createdAt) {

    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getTimezone(),
                organization.getStatus(),
                organization.getCreatedAt());
    }

}
