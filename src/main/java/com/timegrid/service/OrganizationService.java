package com.timegrid.service;

import com.timegrid.dto.CreateOrganizationRequest;
import com.timegrid.dto.UpdateOrganizationRequest;
import com.timegrid.entity.Organization;
import com.timegrid.enums.OrganizationStatus;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.exception.BusinessRuleException;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.time.ZoneId;
import java.time.DateTimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        return organizationRepository.findByDeletedAtIsNull();
    }

    @Transactional(readOnly = true)
    public List<Organization> findDeleted() {
        return organizationRepository.findByDeletedAtIsNotNull();
    }

    @Transactional(readOnly = true)
    public Organization findById(UUID id) {
        return organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Organization not found.", id));
    }

    @Transactional
    public Organization create(CreateOrganizationRequest request) {
        validateTimezone(request.timezone());

        if (organizationRepository.existsByName(request.name())) {
            throw new ConflictException("There is already an organization with this name.");
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
            throw new ConflictException("There is already an organization with this name.");
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
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organization not found.", id));

        if (organization.getDeletedAt() == null) {
            return organization;
        }

        organization.setDeletedAt(null);
        return organizationRepository.save(organization);
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            throw new BusinessRuleException("Unknown timezone.", timezone);
        }
    }

}
