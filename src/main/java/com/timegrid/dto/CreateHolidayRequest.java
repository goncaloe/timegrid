package com.timegrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateHolidayRequest(
        @NotNull LocalDate date,
        @NotBlank @Size(max = 150) String name) {
}