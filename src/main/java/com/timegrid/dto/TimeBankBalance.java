package com.timegrid.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TimeBankBalance(
        UUID userId,
        LocalDate from,
        LocalDate to,
        int expectedMinutes,
        int workedMinutes,
        int balanceMinutes,
        List<TimeBankDay> days) {
}