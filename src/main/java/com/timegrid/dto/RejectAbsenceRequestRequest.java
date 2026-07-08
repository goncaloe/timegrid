package com.timegrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectAbsenceRequestRequest(@NotBlank @Size(max = 500) String reason) {
}
