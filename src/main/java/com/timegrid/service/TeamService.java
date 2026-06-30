package com.timegrid.service;

import com.timegrid.dto.CreateTeamRequest;
import com.timegrid.dto.UpdateTeamRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Team;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.TeamRepository;
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

    @Transactional(readOnly = true)
    public List<Team> findByOrganization(UUID organizationId) {
        return teamRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId);
    }

    @Transactional
    public Team create(CreateTeamRequest request) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new NotFoundException("Organization not found.", request.organizationId()));

        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setOrganization(organization);

        return teamRepository.save(team);
    }

    @Transactional
    public Team update(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Team not found.", id));

        team.setName(request.name());
        team.setDescription(request.description());

        return teamRepository.save(team);
    }

    @Transactional
    public void delete(UUID id) {
        Team team = teamRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Team not found.", id));
        team.setDeletedAt(Instant.now());
    }

    @Transactional
    public Team restore(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Team not found.", id));

        team.setDeletedAt(null);
        return teamRepository.save(team);
    }

}
