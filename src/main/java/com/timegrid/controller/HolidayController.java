package com.timegrid.controller;

import com.timegrid.dto.CreateHolidayRequest;
import com.timegrid.dto.HolidayResponse;
import com.timegrid.service.HolidayService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public List<HolidayResponse> findForYear(@RequestParam(required = false) Integer year) {
        int requestedYear = year != null ? year : LocalDate.now().getYear();
        return holidayService.findForYear(requestedYear).stream()
                .map(HolidayResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('holiday:write')")
    public HolidayResponse create(@Valid @RequestBody CreateHolidayRequest request) {
        return HolidayResponse.from(holidayService.create(request.date(), request.name()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('holiday:write')")
    public void delete(@PathVariable UUID id) {
        holidayService.delete(id);
    }

}