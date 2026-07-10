package com.timegrid.service;

import com.timegrid.dto.CreateUserRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Role;
import com.timegrid.entity.Team;
import com.timegrid.entity.User;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.RoleRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_ROLE = "EMPLOYEE";

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional
    public User create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("user.username.taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("user.email.taken");
        }

        Organization organization = organizationRepository.findById(currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("organization.not-found", currentUser.organizationId()));

        Team team = null;
        if (request.teamId() != null) {
            team = teamRepository
                    .findByIdAndOrganizationIdAndDeletedAtIsNull(request.teamId(), currentUser.organizationId())
                    .orElseThrow(() -> new NotFoundException("team.not-found", request.teamId()));
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("The role EMPLOYEE does not exists in database."));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setOrganization(organization);
        user.setTeam(team);
        user.setRoles(Set.of(defaultRole));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByOrganizationIdAndDeletedAtIsNull(currentUser.organizationId(), pageable);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("user.not-found", id));
    }

    @Transactional
    public User updateRoles(UUID id, Set<String> roleNames) {
        User user = findById(id);

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new NotFoundException("Role not found.", name)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        user.setDeletedAt(Instant.now());
    }

    @Transactional
    public User restore(UUID id) {
        User user = userRepository.findByIdAndOrganizationId(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("user.not-found", id));

        user.setDeletedAt(null);
        return userRepository.save(user);
    }
}
