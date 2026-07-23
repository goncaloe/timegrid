package com.timegrid.controller;

import com.timegrid.dto.CreateUserRequest;
import com.timegrid.dto.PageResponse;
import com.timegrid.dto.UpdateDailyWorkMinutesRequest;
import com.timegrid.dto.UpdateUserRolesRequest;
import com.timegrid.dto.UpdateUserTeamRequest;
import com.timegrid.dto.UserResponse;
import com.timegrid.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('user:write')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public PageResponse<UserResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        return PageResponse.from(userService.findAll(pageable).map(UserResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public UserResponse findById(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:write')")
    public UserResponse updateRoles(@PathVariable UUID id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return UserResponse.from(userService.updateRoles(id, request.roles()));
    }

    @PutMapping("/{id}/daily-work-minutes")
    @PreAuthorize("hasAuthority('user:write')")
    public UserResponse updateDailyWorkMinutes(@PathVariable UUID id,
            @Valid @RequestBody UpdateDailyWorkMinutesRequest request) {
        return UserResponse.from(userService.updateDailyWorkMinutes(id, request.dailyWorkMinutes()));
    }

    @PutMapping("/{id}/team")
    @PreAuthorize("hasAuthority('user:write')")
    public UserResponse updateTeam(@PathVariable UUID id, @Valid @RequestBody UpdateUserTeamRequest request) {
        return UserResponse.from(userService.updateTeam(id, request.teamId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:write')")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('user:write')")
    public UserResponse restore(@PathVariable UUID id) {
        return UserResponse.from(userService.restore(id));
    }

}