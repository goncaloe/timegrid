package com.timegrid.service;

import com.timegrid.dto.CreateOrganizationRequest;
import com.timegrid.dto.UpdateOrganizationRequest;
import com.timegrid.entity.Organization;
import com.timegrid.enums.OrganizationStatus;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.security.CurrentUser;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public Organization findCurrent() {
        return organizationRepository.findByIdAndDeletedAtIsNull(currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("organization.not-found", currentUser.organizationId()));
    }

    @Transactional(readOnly = true)
    public Organization findById(UUID id) {
        requireOwn(id);
        return organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("organization.not-found", id));
    }

    @Transactional
    public Organization create(CreateOrganizationRequest request) {
        validateTimezone(request.timezone());

        if (organizationRepository.existsByName(request.name())) {
            throw new ConflictException("organization.name.taken");
        }

        Organization organization = new Organization();
        organization.setName(request.name());
        organization.setTimezone(request.timezone());
        organization.setStatus(OrganizationStatus.ACTIVE);

        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization update(UUID id, UpdateOrganizationRequest request) {
        Organization organization = findById(id);

        validateTimezone(request.timezone());

        if (organizationRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ConflictException("organization.name.taken");
        }

        organization.setName(request.name());
        organization.setTimezone(request.timezone());
        organization.setStatus(request.status());

        return organizationRepository.save(organization);
    }

    @Transactional
    public void delete(UUID id) {
        Organization organization = findById(id);
        organization.setDeletedAt(Instant.now());
    }

    @Transactional
    public Organization restore(UUID id) {
        requireOwn(id);
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("organization.not-found", id));

        organization.setDeletedAt(null);
        return organizationRepository.save(organization);
    }

    private void requireOwn(UUID id) {
        if (!id.equals(currentUser.organizationId())) {
            throw new NotFoundException("organization.not-found", id);
        }
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            throw new BusinessRuleException("organization.invalid.timezone", timezone);
        }
    }

}