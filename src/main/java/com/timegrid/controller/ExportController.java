package com.timegrid.controller;

import com.timegrid.dto.ExportFile;
import com.timegrid.enums.ExportFormat;
import com.timegrid.service.ExportService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:export')")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/time-entries")
    public ResponseEntity<byte[]> timeEntries(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID teamId) {
        LocalDate start = from != null ? from : YearMonth.now().atDay(1);
        LocalDate end = to != null ? to : YearMonth.from(start).atEndOfMonth();
        return download(exportService.timeEntries(ExportFormat.parse(format), start, end, userId, teamId));
    }

    private static ResponseEntity<byte[]> download(ExportFile file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.filename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(file.format().contentType()))
                .contentLength(file.content().length)
                .body(file.content());
    }

}