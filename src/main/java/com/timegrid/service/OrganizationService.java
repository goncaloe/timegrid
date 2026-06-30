package com.timegrid.service;

import com.timegrid.dto.CreateOrganizationRequest;
import com.timegrid.dto.UpdateOrganizationRequest;
import com.timegrid.entity.Organization;
import com.timegrid.enums.OrganizationStatus;
import com.timegrid.repository.OrganizationRepository;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Organization create(CreateOrganizationRequest request) {
        if (organizationRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "o nome já existe");
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

        if (organizationRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "o nome já existe");
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
        organizationRepository.save(organization);
    }

    @Transactional
    public Organization restore(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (organization.getDeletedAt() == null) {
            return organization;
        }

        organization.setDeletedAt(null);
        return organizationRepository.save(organization);
    }

}
