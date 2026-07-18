package com.timegrid.controller;

import com.timegrid.dto.TimeBankBalance;
import com.timegrid.service.TimeBankService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/time-bank")
@RequiredArgsConstructor
public class TimeBankController {

    private final TimeBankService timeBankService;

    @GetMapping("/me")
    public TimeBankBalance me(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return timeBankService.balanceForSelf(from, to);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public TimeBankBalance forUser(@PathVariable UUID id, @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return timeBankService.balanceForUser(id, from, to);
    }

}