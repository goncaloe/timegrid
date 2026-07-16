package com.timegrid.service;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReportTable(
        String title,
        List<String> headers,
        List<List<Object>> rows) {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public static String text(Object value) {
        // switch com pattern matching:
        return switch (value) {
            case null -> "";
            case LocalDate date -> date.format(DATE);
            case LocalTime time -> time.format(TIME);
            default -> value.toString();
        };
    }

}