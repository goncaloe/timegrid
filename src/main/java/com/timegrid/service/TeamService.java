package com.timegrid.service;

import com.timegrid.dto.CreateTeamRequest;
import com.timegrid.dto.UpdateTeamRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Team;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.TeamRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;

    public List<Team> findByOrganization(UUID organizationId) {
        return teamRepository.findByOrganizationId(organizationId);
    }

    public Team create(CreateTeamRequest request) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setOrganization(organization);

        return teamRepository.save(team);
    }

    public Team update(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        team.setName(request.name());
        team.setDescription(request.description());

        return teamRepository.save(team);
    }

}
