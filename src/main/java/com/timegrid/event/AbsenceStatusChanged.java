package com.timegrid.event;

import com.timegrid.enums.AbsenceStatus;
import java.util.UUID;

public record AbsenceStatusChanged(
        UUID absenceId,
        UUID organizationId,
        UUID ownerId,
        UUID changedBy,
        AbsenceStatus from,
        AbsenceStatus to,
        String comment) {
}