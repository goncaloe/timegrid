package com.timegrid.service;

import com.timegrid.enums.ExportFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class XlsxWriter implements ReportWriter {

    private static final int MAX_COLUMN_CHARS = 60;

    @Override
    public ExportFormat format() {
        return ExportFormat.XLSX;
    }

    @Override
    public byte[] write(ReportTable table) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(table.title()));
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));

            int columns = table.headers().size();
            int[] widths = new int[columns];

            Row header = sheet.createRow(0);
            for (int c = 0; c < columns; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(table.headers().get(c));
                cell.setCellStyle(headerStyle);
                widths[c] = table.headers().get(c).length();
            }

            int r = 1;
            for (List<Object> values : table.rows()) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < columns; c++) {
                    Object value = values.get(c);
                    Cell cell = row.createCell(c);
                    switch (value) {
                        case null -> cell.setBlank();
                        case Number number -> cell.setCellValue(number.doubleValue());
                        case LocalDate date -> {
                            cell.setCellValue(date);
                            cell.setCellStyle(dateStyle);
                        }
                        default -> cell.setCellValue(ReportTable.text(value));
                    }
                    widths[c] = Math.max(widths[c], ReportTable.text(value).length());
                }
            }

            for (int c = 0; c < columns; c++) {
                sheet.setColumnWidth(c, (Math.min(widths[c], MAX_COLUMN_CHARS) + 2) * 256);
            }
            sheet.createFreezePane(0, 1);
            if (columns > 0) {
                sheet.setAutoFilter(new CellRangeAddress(0, Math.max(r - 1, 0), 0, columns - 1));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

}