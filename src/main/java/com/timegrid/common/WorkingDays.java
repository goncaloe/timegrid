package com.timegrid.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public final class WorkingDays {

    private WorkingDays() {
    }

    public static long between(LocalDate startDate, LocalDate endDate, Set<LocalDate> holidays) {
        long days = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY && !holidays.contains(date)) {
                days++;
            }
        }
        return days;
    }

}