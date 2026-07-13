package com.timegrid.event;

import com.timegrid.enums.TimeEntryStatus;
import java.util.UUID;

public record TimeEntryStatusChanged(
        UUID timeEntryId,
        UUID organizationId,
        UUID ownerId,
        UUID changedBy,
        TimeEntryStatus from,
        TimeEntryStatus to,
        String comment) {
}