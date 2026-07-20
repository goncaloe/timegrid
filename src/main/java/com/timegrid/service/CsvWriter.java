package com.timegrid.service;

import com.timegrid.enums.ExportFormat;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvWriter implements ReportWriter {

    // o Excel em PT usa ; como separador e só deteta UTF-8 com BOM
    private static final String BOM = "﻿";
    private static final char SEPARATOR = ';';

    @Override
    public ExportFormat format() {
        return ExportFormat.CSV;
    }

    @Override
    public byte[] write(ReportTable table) {
        StringBuilder csv = new StringBuilder(BOM);
        appendLine(csv, table.headers());
        for (List<Object> row : table.rows()) {
            appendLine(csv, row);
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendLine(StringBuilder csv, List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                csv.append(SEPARATOR);
            }
            csv.append(escape(ReportTable.text(values.get(i))));
        }
        csv.append("\r\n");
    }

    static String escape(String value) {
        if (value.indexOf(SEPARATOR) < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0
                && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

}