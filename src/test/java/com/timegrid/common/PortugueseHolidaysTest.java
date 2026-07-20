package com.timegrid.common;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PortugueseHolidaysTest {

    @Test
    void movableHolidaysFor2026() {
        List<NationalHoliday> holidays = PortugueseHolidays.of(2026);

        assertThat(dateOf(holidays, "Sexta-feira Santa")).isEqualTo(LocalDate.of(2026, 4, 3));
        assertThat(dateOf(holidays, "Páscoa")).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(dateOf(holidays, "Corpo de Deus")).isEqualTo(LocalDate.of(2026, 6, 4));
    }

    @Test
    void movableHolidaysFor2027() {
        List<NationalHoliday> holidays = PortugueseHolidays.of(2027);

        assertThat(dateOf(holidays, "Páscoa")).isEqualTo(LocalDate.of(2027, 3, 28));
        assertThat(dateOf(holidays, "Corpo de Deus")).isEqualTo(LocalDate.of(2027, 5, 27));
    }

    @Test
    void fixedHolidaysAreAlwaysOnTheSameDate() {
        List<NationalHoliday> holidays = PortugueseHolidays.of(2026);

        assertThat(dateOf(holidays, "Ano Novo")).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(dateOf(holidays, "Dia da Liberdade")).isEqualTo(LocalDate.of(2026, 4, 25));
        assertThat(dateOf(holidays, "Dia do Trabalhador")).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(dateOf(holidays, "Dia de Portugal")).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(dateOf(holidays, "Assunção de Nossa Senhora")).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(dateOf(holidays, "Implantação da República")).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(dateOf(holidays, "Todos os Santos")).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(dateOf(holidays, "Restauração da Independência")).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(dateOf(holidays, "Imaculada Conceição")).isEqualTo(LocalDate.of(2026, 12, 8));
        assertThat(dateOf(holidays, "Natal")).isEqualTo(LocalDate.of(2026, 12, 25));
    }

    @Test
    void alwaysReturnsThirteenHolidays() {
        assertThat(PortugueseHolidays.of(2026)).hasSize(13);
    }

    private static LocalDate dateOf(List<NationalHoliday> holidays, String name) {
        return holidays.stream()
                .filter(holiday -> holiday.name().equals(name))
                .findFirst()
                .orElseThrow()
                .date();
    }

}