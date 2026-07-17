package com.timegrid.common;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class WorkingDays {

    private WorkingDays() {
    }

    // TODO: descontar feriados quando existir o banco de horas
    public static long between(LocalDate startDate, LocalDate endDate) {
        long days = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }

}