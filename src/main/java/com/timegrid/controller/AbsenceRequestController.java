package com.timegrid.controller;

import com.timegrid.dto.AbsenceRequestResponse;
import com.timegrid.dto.CreateAbsenceRequestRequest;
import com.timegrid.dto.RejectAbsenceRequestRequest;
import com.timegrid.dto.StatusChangeResponse;
import com.timegrid.dto.UpdateAbsenceRequestRequest;
import com.timegrid.entity.AbsenceRequest;
import com.timegrid.service.AbsenceRequestService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceRequestController {

    private final AbsenceRequestService absenceRequestService;

    @GetMapping
    public List<AbsenceRequestResponse> findOwn(@RequestParam(required = false) Integer year) {
        return absenceRequestService.findOwn(year).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}/history")
    public List<StatusChangeResponse> history(@PathVariable UUID id) {
        return absenceRequestService.history(id).stream()
                .map(StatusChangeResponse::from)
                .toList();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('absence:approve')")
    public List<AbsenceRequestResponse> pending(@RequestParam(required = false) UUID teamId) {
        return absenceRequestService.findPending(teamId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbsenceRequestResponse create(@Valid @RequestBody CreateAbsenceRequestRequest request) {
        AbsenceRequest absenceRequest = absenceRequestService.create(
                request.startDate(), request.endDate(), request.type(), request.notes());
        return toResponse(absenceRequest);
    }

    @PutMapping("/{id}")
    public AbsenceRequestResponse update(@PathVariable UUID id,
            @Valid @RequestBody UpdateAbsenceRequestRequest request) {
        AbsenceRequest absenceRequest = absenceRequestService.update(
                id, request.startDate(), request.endDate(), request.type(), request.notes());
        return toResponse(absenceRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        absenceRequestService.delete(id);
    }

    @PostMapping("/{id}/restore")
    public AbsenceRequestResponse restore(@PathVariable UUID id) {
        return toResponse(absenceRequestService.restore(id));
    }

    @PostMapping("/{id}/submit")
    public AbsenceRequestResponse submit(@PathVariable UUID id) {
        return toResponse(absenceRequestService.submit(id));
    }

    @PostMapping("/{id}/cancel")
    public AbsenceRequestResponse cancel(@PathVariable UUID id) {
        return toResponse(absenceRequestService.cancel(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('absence:approve')")
    public AbsenceRequestResponse approve(@PathVariable UUID id) {
        return toResponse(absenceRequestService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('absence:approve')")
    public AbsenceRequestResponse reject(@PathVariable UUID id,
            @Valid @RequestBody RejectAbsenceRequestRequest request) {
        return toResponse(absenceRequestService.reject(id, request.reason()));
    }

    private AbsenceRequestResponse toResponse(AbsenceRequest request) {
        long workingDays = absenceRequestService.workingDays(request);
        return AbsenceRequestResponse.from(request, workingDays, absenceRequestService.allowedActions(request));
    }

}