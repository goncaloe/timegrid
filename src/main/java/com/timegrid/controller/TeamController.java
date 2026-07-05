package com.timegrid.controller;

import com.timegrid.dto.CreateTeamRequest;
import com.timegrid.dto.TeamResponse;
import com.timegrid.dto.UpdateTeamRequest;
import com.timegrid.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
	public List<TeamResponse> findAll() {
		return teamService.findAll().stream()
                .map(TeamResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@Valid @RequestBody CreateTeamRequest request) {
        return TeamResponse.from(teamService.create(request));
    }

    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return TeamResponse.from(teamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        teamService.delete(id);
    }

    @PostMapping("/{id}/restore")
    public TeamResponse restore(@PathVariable UUID id) {
        return TeamResponse.from(teamService.restore(id));
    }

}
