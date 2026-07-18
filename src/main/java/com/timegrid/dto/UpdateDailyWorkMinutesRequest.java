package com.timegrid.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateDailyWorkMinutesRequest(@Min(1) @Max(1440) int dailyWorkMinutes) {
}