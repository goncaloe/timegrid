package com.timegrid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.timegrid.dto.CreateUserRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Role;
import com.timegrid.entity.User;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.RoleRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;

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

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private UserService userService;

    @Test
    void createsUserWithDefaultRole() {
        UUID organizationId = UUID.randomUUID();
        Organization organization = new Organization();
        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");

        CreateUserRequest request = new CreateUserRequest(
                "goncalo.esteves", "goncalo.esteves@tejodigital.pt", "Gonçalo Esteves",
                "timegrid2026", null);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
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
                "gestor123", null);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsTeamFromAnotherOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        Organization organization = new Organization();

        CreateUserRequest request = new CreateUserRequest(
                "ines.moura", "ines.moura@tejodigital.pt", "Inês Moura",
                "equipa2026", teamId);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(teamRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(teamId, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByIdIgnoresUsersFromAnotherOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(userRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteMarksDeletedAt() {
        UUID organizationId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        User user = new User();

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(userRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(user));

        userService.delete(id);

        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void restoreClearsDeletedAt() {
        UUID organizationId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setDeletedAt(Instant.now());

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(userRepository.findByIdAndOrganizationId(id, organizationId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User restored = userService.restore(id);

        assertThat(restored.getDeletedAt()).isNull();
    }

}
