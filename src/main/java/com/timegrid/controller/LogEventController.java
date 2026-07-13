package com.timegrid.controller;

import com.timegrid.dto.LogEventResponse;
import com.timegrid.dto.PageResponse;
import com.timegrid.enums.LogAction;
import com.timegrid.service.LogService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log-events")
@RequiredArgsConstructor
public class LogEventController {

    private final LogService logService;

    @GetMapping
    @PreAuthorize("hasAuthority('log:read')")
    public PageResponse<LogEventResponse> search(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) LogAction action,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 200), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(logService.search(userId, action, from, to, pageable).map(LogEventResponse::from));
    }

}