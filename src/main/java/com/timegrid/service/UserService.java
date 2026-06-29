package com.timegrid.service;

import com.timegrid.dto.CreateUserRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Role;
import com.timegrid.entity.Team;
import com.timegrid.entity.User;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.RoleRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_ROLE = "EMPLOYEE";

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(CreateUserRequest request) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Team team = null;
        if (request.teamId() != null) {
            team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

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

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
