package com.timegrid.service;

import com.timegrid.dto.CreateTeamRequest;
import com.timegrid.dto.UpdateTeamRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Team;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.security.CurrentUser;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private TeamService teamService;

    @Test
    void createAssignsOwnOrganization() {
        UUID organizationId = UUID.randomUUID();
        Organization organization = new Organization();

        CreateTeamRequest request = new CreateTeamRequest("Backend", "dep de backend");

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(teamRepository.existsByOrganizationIdAndNameAndDeletedAtIsNull(organization.getId(), "Backend"))
                .thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Team created = teamService.create(request);

        assertThat(created.getName()).isEqualTo("Backend");
        assertThat(created.getOrganization()).isEqualTo(organization);
    }

    @Test
    void createRejectsDuplicateName() {
        UUID organizationId = UUID.randomUUID();
        Organization organization = new Organization();

        CreateTeamRequest request = new CreateTeamRequest("Backend", null);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(teamRepository.existsByOrganizationIdAndNameAndDeletedAtIsNull(organization.getId(), "Backend"))
                .thenReturn(true);

        assertThatThrownBy(() -> teamService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findAllReturnsOnlyOwnOrganizationTeams() {
        UUID organizationId = UUID.randomUUID();
        Team team = new Team();

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(teamRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId)).thenReturn(List.of(team));

        assertThat(teamService.findAll()).containsExactly(team);
    }

    @Test
    void updateRejectsTeamFromAnotherOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UpdateTeamRequest request = new UpdateTeamRequest("Nova", null);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(teamRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(teamId, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.update(teamId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteMarksDeletedAt() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        Team team = new Team();

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(teamRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(teamId, organizationId))
                .thenReturn(Optional.of(team));

        teamService.delete(teamId);

        assertThat(team.getDeletedAt()).isNotNull();
    }

    @Test
    void restoreClearsDeletedAt() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setDeletedAt(Instant.now());

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(teamRepository.findByIdAndOrganizationId(teamId, organizationId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Team restored = teamService.restore(teamId);

        assertThat(restored.getDeletedAt()).isNull();
    }

}
