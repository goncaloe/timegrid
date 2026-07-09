package com.timegrid.controller;

import com.timegrid.dto.OrganizationResponse;
import com.timegrid.dto.CreateOrganizationRequest;
import com.timegrid.dto.UpdateOrganizationRequest;
import com.timegrid.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public List<OrganizationResponse> findAll() {
        return List.of(OrganizationResponse.from(organizationService.findCurrent()));
    }

    @GetMapping("/{id}")
    public OrganizationResponse findById(@PathVariable UUID id) {
        return OrganizationResponse.from(organizationService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('organization:write')")
    public OrganizationResponse create(@Valid @RequestBody CreateOrganizationRequest request) {
        return OrganizationResponse.from(organizationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:write')")
    public OrganizationResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return OrganizationResponse.from(organizationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('organization:write')")
    public void delete(@PathVariable UUID id) {
        organizationService.delete(id);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('organization:write')")
    public OrganizationResponse restore(@PathVariable UUID id) {
        return OrganizationResponse.from(organizationService.restore(id));
    }
}
