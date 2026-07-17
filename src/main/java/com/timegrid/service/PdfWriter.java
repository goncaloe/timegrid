package com.timegrid.service;

import com.timegrid.enums.ExportFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Component;

@Component
public class PdfWriter implements ReportWriter {

    private static final PDRectangle PAGE_SIZE = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
    private static final float MARGIN = 36;
    private static final float FONT_SIZE = 9;
    private static final float ROW_HEIGHT = 15;
    private static final float PADDING = 3;
    private static final int MAX_COLUMN_CHARS = 40;
    private static final DateTimeFormatter GENERATED_AT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public ExportFormat format() {
        return ExportFormat.PDF;
    }

    @Override
    public byte[] write(ReportTable table) {
        PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            float[] widths = columnWidths(table);
            float bottom = MARGIN + ROW_HEIGHT;

            PDPage page = null;
            PDPageContentStream content = null;
            float y = 0;
            int index = 0;
            do {
                if (content == null || y - ROW_HEIGHT < bottom) {
                    if (content != null) {
                        content.close();
                    }
                    page = new PDPage(PAGE_SIZE);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = PAGE_SIZE.getHeight() - MARGIN;
                    if (document.getNumberOfPages() == 1) {
                        text(content, bold, 14, MARGIN, y - 14, table.title());
                        text(content, regular, 8, MARGIN, y - 26, "Generated at " + LocalDateTime.now().format(GENERATED_AT));
                        y -= 40;
                    }
                    y = row(content, bold, widths, y, table.headers(), true);
                }
                if (index < table.rows().size()) {
                    y = row(content, regular, widths, y, table.rows().get(index), false);
                }
                index++;
            } while (index < table.rows().size());
            content.close();

            footers(document, regular);
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static float row(PDPageContentStream content, PDFont font, float[] widths, float y, List<?> values,
            boolean header) throws IOException {
        float width = PAGE_SIZE.getWidth() - 2 * MARGIN;
        if (header) {
            content.setNonStrokingColor(0.85f, 0.85f, 0.85f);
            content.addRect(MARGIN, y - ROW_HEIGHT, width, ROW_HEIGHT);
            content.fill();
            content.setNonStrokingColor(0f, 0f, 0f);
        }
        float x = MARGIN;
        for (int c = 0; c < widths.length; c++) {
            String value = fit(font, clean(font, ReportTable.text(values.get(c))), widths[c] - 2 * PADDING);
            text(content, font, FONT_SIZE, x + PADDING, y - ROW_HEIGHT + 4, value);
            x += widths[c];
        }
        content.setStrokingColor(0.75f, 0.75f, 0.75f);
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, y - ROW_HEIGHT);
        content.lineTo(MARGIN + width, y - ROW_HEIGHT);
        content.stroke();
        return y - ROW_HEIGHT;
    }

    private static void footers(PDDocument document, PDFont font) throws IOException {
        int total = document.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream content = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true)) {
                String label = "Page " + (i + 1) + " of " + total;
                float labelWidth = font.getStringWidth(label) / 1000 * 8;
                text(content, font, 8, PAGE_SIZE.getWidth() - MARGIN - labelWidth, MARGIN / 2, label);
            }
        }
    }

    private static void text(PDPageContentStream content, PDFont font, float size, float x, float y, String value)
            throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(value);
        content.endText();
    }

    private static float[] columnWidths(ReportTable table) {
        int columns = table.headers().size();
        int[] chars = new int[columns];
        for (int c = 0; c < columns; c++) {
            chars[c] = table.headers().get(c).length();
        }
        for (List<Object> row : table.rows()) {
            for (int c = 0; c < columns; c++) {
                chars[c] = Math.max(chars[c], ReportTable.text(row.get(c)).length());
            }
        }
        int total = 0;
        for (int c = 0; c < columns; c++) {
            chars[c] = Math.min(Math.max(chars[c], 4), MAX_COLUMN_CHARS);
            total += chars[c];
        }
        float available = PAGE_SIZE.getWidth() - 2 * MARGIN;
        float[] widths = new float[columns];
        for (int c = 0; c < columns; c++) {
            widths[c] = available * chars[c] / total;
        }
        return widths;
    }

    private static String fit(PDFont font, String value, float maxWidth) throws IOException {
        if (width(font, value) <= maxWidth) {
            return value;
        }
        String ellipsis = "…";
        int end = value.length();
        while (end > 0 && width(font, value.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return value.substring(0, end) + ellipsis;
    }

    private static float width(PDFont font, String value) throws IOException {
        return font.getStringWidth(value) / 1000 * FONT_SIZE;
    }

    private static String clean(PDFont font, String value) {
        StringBuilder result = new StringBuilder(value.length());
        value.codePoints().forEach(cp -> {
            if (Character.isWhitespace(cp)) {
                result.append(' ');
                return;
            }
            String ch = new String(Character.toChars(cp));
            try {
                font.encode(ch);
                result.append(ch);
            } catch (IOException | IllegalArgumentException e) {
                result.append('?');
            }
        });
        return result.toString();
    }

}