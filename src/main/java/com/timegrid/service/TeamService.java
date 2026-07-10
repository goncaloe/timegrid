package com.timegrid.service;

import com.timegrid.dto.CreateTeamRequest;
import com.timegrid.dto.UpdateTeamRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Team;
import com.timegrid.exception.ConflictException;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.security.CurrentUser;
import com.timegrid.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return teamRepository.findByOrganizationIdAndDeletedAtIsNull(currentUser.organizationId());
    }

    @Transactional
    public Team create(CreateTeamRequest request) {
        Organization organization = organizationRepository.findById(currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("organization.not-found", currentUser.organizationId()));

        if (teamRepository.existsByOrganizationIdAndNameAndDeletedAtIsNull(
                organization.getId(), request.name())) {
            throw new ConflictException("team.name.taken");
        }

        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setOrganization(organization);

        return teamRepository.save(team);
    }

    @Transactional
    public Team update(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("team.not-found", id));

        if (teamRepository.existsByOrganizationIdAndNameAndDeletedAtIsNullAndIdNot(
                team.getOrganization().getId(), request.name(), id)) {
            throw new ConflictException("team.name.taken");
        }

        team.setName(request.name());
        team.setDescription(request.description());

        return teamRepository.save(team);
    }

    @Transactional
    public void delete(UUID id) {
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("team.not-found", id));
        team.setDeletedAt(Instant.now());
    }

    @Transactional
    public Team restore(UUID id) {
        Team team = teamRepository.findByIdAndOrganizationId(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("team.not-found", id));

        team.setDeletedAt(null);
        return teamRepository.save(team);
    }

}
