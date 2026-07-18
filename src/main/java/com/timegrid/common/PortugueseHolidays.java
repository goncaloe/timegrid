package com.timegrid.common;

import java.time.LocalDate;
import java.util.List;

public final class PortugueseHolidays {

    private PortugueseHolidays() {
    }

    public static List<NationalHoliday> of(int year) {
        LocalDate easterSunday = easterSunday(year);
        return List.of(
                new NationalHoliday(LocalDate.of(year, 1, 1), "Ano Novo"),
                new NationalHoliday(easterSunday.minusDays(2), "Sexta-feira Santa"),
                new NationalHoliday(easterSunday, "Páscoa"),
                new NationalHoliday(LocalDate.of(year, 4, 25), "Dia da Liberdade"),
                new NationalHoliday(LocalDate.of(year, 5, 1), "Dia do Trabalhador"),
                new NationalHoliday(easterSunday.plusDays(60), "Corpo de Deus"),
                new NationalHoliday(LocalDate.of(year, 6, 10), "Dia de Portugal"),
                new NationalHoliday(LocalDate.of(year, 8, 15), "Assunção de Nossa Senhora"),
                new NationalHoliday(LocalDate.of(year, 10, 5), "Implantação da República"),
                new NationalHoliday(LocalDate.of(year, 11, 1), "Todos os Santos"),
                new NationalHoliday(LocalDate.of(year, 12, 1), "Restauração da Independência"),
                new NationalHoliday(LocalDate.of(year, 12, 8), "Imaculada Conceição"),
                new NationalHoliday(LocalDate.of(year, 12, 25), "Natal"));
    }

    // algoritmo de Gauss/Meeus para o domingo de Páscoa
    private static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

}