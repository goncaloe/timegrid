package com.timegrid.service;

import com.timegrid.dto.CreateOrganizationRequest;
import com.timegrid.dto.UpdateOrganizationRequest;
import com.timegrid.entity.Organization;
import com.timegrid.enums.OrganizationStatus;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.OrganizationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    void findAllIgnoresDeletedOrganizations() {
        Organization active = new Organization();
        active.setDeletedAt(null);

        when(organizationRepository.findByDeletedAtIsNull()).thenReturn(List.of(active));

        assertThat(organizationService.findAll()).hasSize(1);
    }

    @Test
    void createAssignsActiveStatus() {
        CreateOrganizationRequest request = new CreateOrganizationRequest("Tejo Digital", "Europe/Lisbon");

        when(organizationRepository.existsByName("Tejo Digital")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        Organization created = organizationService.create(request);

        assertThat(created.getName()).isEqualTo("Tejo Digital");
        assertThat(created.getTimezone()).isEqualTo("Europe/Lisbon");
        assertThat(created.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
    }

    @Test
    void createRejectsInvalidTimezone() {
        CreateOrganizationRequest request = new CreateOrganizationRequest("Tejo Digital", "Mars/Olympus");

        assertThatThrownBy(() -> organizationService.create(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateName() {
        CreateOrganizationRequest request = new CreateOrganizationRequest("Tejo Digital", "Europe/Lisbon");

        when(organizationRepository.existsByName("Tejo Digital")).thenReturn(true);

        assertThatThrownBy(() -> organizationService.create(request))
                .isInstanceOf(ConflictException.class);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void updateChangesNameAndStatus() {
        UUID id = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setName("Antiga");
        organization.setStatus(OrganizationStatus.ACTIVE);

        UpdateOrganizationRequest request = new UpdateOrganizationRequest("Nova", "Atlantic/Azores",
                OrganizationStatus.INACTIVE);

        when(organizationRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(organization));
        when(organizationRepository.existsByNameAndIdNot("Nova", id)).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        Organization updated = organizationService.update(id, request);

        assertThat(updated.getName()).isEqualTo("Nova");
        assertThat(updated.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
    }

    @Test
    void updateRejectsDuplicateName() {
        UUID id = UUID.randomUUID();
        Organization organization = new Organization();

        UpdateOrganizationRequest request = new UpdateOrganizationRequest("Nova", "Europe/Lisbon",
                OrganizationStatus.ACTIVE);

        when(organizationRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(organization));
        when(organizationRepository.existsByNameAndIdNot("Nova", id)).thenReturn(true);

        assertThatThrownBy(() -> organizationService.update(id, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findByIdThrowsWhenNotExists() {
        UUID id = UUID.randomUUID();
        when(organizationRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.findById(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteMarksDeletedAt() {
        UUID id = UUID.randomUUID();
        Organization organization = new Organization();

        when(organizationRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(organization));

        organizationService.delete(id);

        assertThat(organization.getDeletedAt()).isNotNull();
        assertThat(organization.getDeletedAt()).isBefore(Instant.now().plusSeconds(5));
    }

    @Test
    void restoreClearsDeletedAt() {
        UUID id = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setDeletedAt(Instant.now());

        when(organizationRepository.findById(id)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        Organization restored = organizationService.restore(id);

        assertThat(restored.getDeletedAt()).isNull();
    }

}
