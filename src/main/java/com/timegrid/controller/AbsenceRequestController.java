package com.timegrid.controller;

import com.timegrid.dto.AbsenceRequestResponse;
import com.timegrid.dto.CreateAbsenceRequestRequest;
import com.timegrid.entity.AbsenceRequest;
import com.timegrid.service.AbsenceRequestService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
                .map(AbsenceRequestResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbsenceRequestResponse create(@Valid @RequestBody CreateAbsenceRequestRequest request) {
        AbsenceRequest absenceRequest = absenceRequestService.create(
                request.startDate(), request.endDate(), request.type(), request.notes());
        return AbsenceRequestResponse.from(absenceRequest);
    }

}
