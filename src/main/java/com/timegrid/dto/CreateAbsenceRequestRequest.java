package com.timegrid.dto;

import com.timegrid.enums.AbsenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateAbsenceRequestRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull AbsenceType type,
        @Size(max = 500) String notes) {
}
