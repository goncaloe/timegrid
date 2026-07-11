package com.timegrid.controller;

import com.timegrid.dto.CreateTimeEntryRequest;
import com.timegrid.dto.RejectTimeEntryRequest;
import com.timegrid.dto.StatusChangeResponse;
import com.timegrid.dto.TimeEntryResponse;
import com.timegrid.dto.UpdateTimeEntryRequest;
import com.timegrid.entity.TimeEntry;
import com.timegrid.service.TimeEntryService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping
    public List<TimeEntryResponse> findOwn(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return timeEntryService.findOwn(from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}/history")
    public List<StatusChangeResponse> history(@PathVariable UUID id) {
        return timeEntryService.history(id).stream()
                .map(StatusChangeResponse::from)
                .toList();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('time_entry:approve')")
    public List<TimeEntryResponse> pending(@RequestParam(required = false) UUID teamId) {
        return timeEntryService.findPending(teamId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryResponse create(@Valid @RequestBody CreateTimeEntryRequest request) {
        int breakMinutes = request.breakMinutes() != null ? request.breakMinutes() : 0;
        TimeEntry entry = timeEntryService.create(
                request.workDate(), request.startTime(), request.endTime(), breakMinutes,
                request.type(), request.notes());
        return toResponse(entry);
    }

    @PutMapping("/{id}")
    public TimeEntryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTimeEntryRequest request) {
        int breakMinutes = request.breakMinutes() != null ? request.breakMinutes() : 0;
        TimeEntry entry = timeEntryService.update(id,
                request.workDate(), request.startTime(), request.endTime(), breakMinutes,
                request.type(), request.notes());
        return toResponse(entry);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        timeEntryService.delete(id);
    }

    @PostMapping("/{id}/restore")
    public TimeEntryResponse restore(@PathVariable UUID id) {
        return toResponse(timeEntryService.restore(id));
    }

    @PostMapping("/{id}/submit")
    public TimeEntryResponse submit(@PathVariable UUID id) {
        return toResponse(timeEntryService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('time_entry:approve')")
    public TimeEntryResponse approve(@PathVariable UUID id) {
        return toResponse(timeEntryService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('time_entry:approve')")
    public TimeEntryResponse reject(@PathVariable UUID id, @Valid @RequestBody RejectTimeEntryRequest request) {
        return toResponse(timeEntryService.reject(id, request.reason()));
    }

    private TimeEntryResponse toResponse(TimeEntry entry) {
        return TimeEntryResponse.from(entry, timeEntryService.allowedActions(entry));
    }

}