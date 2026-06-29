package com.timegrid.service;

import com.timegrid.dto.CreateUserRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Role;
import com.timegrid.entity.User;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.RoleRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createsUserWithDefaultRole() {
        Organization organization = new Organization();
        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");

        CreateUserRequest request = new CreateUserRequest(
                "goncalo.esteves", "goncalo.e@tejodigital.pt", "Gonçalo Esteves",
                "timegrid2026", UUID.randomUUID(), null);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(organizationRepository.findById(request.organizationId())).thenReturn(Optional.of(organization));
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(request.password())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.create(request);

        assertThat(created.getUsername()).isEqualTo("goncalo.esteves");
        assertThat(created.getPasswordHash()).isEqualTo("hash");
        assertThat(created.getRoles()).containsExactly(employeeRole);
    }

    @Test
    void rejectsDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest(
                "tiago.figueiredo", "tiago.figueiredo@tejodigital.pt", "Tiago Figueiredo",
                "gestor123", UUID.randomUUID(), null);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResponseStatusException.class);
    }
}
