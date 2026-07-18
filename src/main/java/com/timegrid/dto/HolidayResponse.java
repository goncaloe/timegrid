package com.timegrid.dto;

import com.timegrid.common.NationalHoliday;
import com.timegrid.entity.Holiday;
import java.time.LocalDate;
import java.util.UUID;

public record HolidayResponse(UUID id, LocalDate date, String name, boolean national) {

    public static HolidayResponse from(Holiday holiday) {
        return new HolidayResponse(holiday.getId(), holiday.getDate(), holiday.getName(), false);
    }

    public static HolidayResponse national(NationalHoliday holiday) {
        return new HolidayResponse(null, holiday.date(), holiday.name(), true);
    }

}