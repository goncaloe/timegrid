package com.timegrid.dto;

import com.timegrid.enums.TimeBankReason;
import java.time.LocalDate;

public record TimeBankDay(LocalDate date, int expectedMinutes, int workedMinutes, TimeBankReason reason) {
}