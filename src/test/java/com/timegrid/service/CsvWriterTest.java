package com.timegrid.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CsvWriterTest {

    private final CsvWriter writer = new CsvWriter();

    @Test
    void startsWithUtf8Bom() {
        byte[] csv = writer.write(new ReportTable("t", List.of("A"), List.of()));

        assertThat(Arrays.copyOf(csv, 3)).containsExactly(0xEF, 0xBB, 0xBF);
    }

    @Test
    void usesSemicolonAndCrlf() {
        String csv = write(List.of("Employee", "Minutes"), List.of(List.of("Inês Moura", 480)));

        assertThat(csv).isEqualTo("Employee;Minutes\r\nInês Moura;480\r\n");
    }

    @Test
    void quotesValuesWithSemicolon() {
        String csv = write(List.of("Notes"), List.of(List.of("meeting; client")));

        assertThat(csv).endsWith("\"meeting; client\"\r\n");
    }

    @Test
    void doublesQuotesInsideValues() {
        String csv = write(List.of("Notes"), List.of(List.of("project \"Tejo\"")));

        assertThat(csv).endsWith("\"project \"\"Tejo\"\"\"\r\n");
    }

    @Test
    void quotesValuesWithLineBreaks() {
        String csv = write(List.of("Notes"), List.of(List.of("line 1\nline 2")));

        assertThat(csv).endsWith("\"line 1\nline 2\"\r\n");
    }

    @Test
    void formatsDatesTimesAndNulls() {
        String csv = write(List.of("Date", "Start", "Notes"),
                List.of(Arrays.asList(LocalDate.of(2026, 9, 3), LocalTime.of(9, 0), null)));

        assertThat(csv).endsWith("03/09/2026;09:00;\r\n");
    }

    @Test
    void escapeLeavesPlainValuesAlone() {
        assertThat(CsvWriter.escape("Office")).isEqualTo("Office");
    }

    private String write(List<String> headers, List<List<Object>> rows) {
        byte[] bytes = writer.write(new ReportTable("t", headers, rows));
        // sem o BOM, para comparar só o conteúdo
        return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
    }

}