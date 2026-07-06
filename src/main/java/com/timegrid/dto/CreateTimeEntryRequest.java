package com.timegrid.dto;

import com.timegrid.enums.TimeEntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateTimeEntryRequest(
        @NotNull LocalDate workDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @PositiveOrZero Integer breakMinutes,
        @NotNull TimeEntryType type,
        @Size(max = 500) String notes) {
}
