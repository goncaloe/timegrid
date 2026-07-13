package com.timegrid.service;

import com.timegrid.dto.AuthResponse;
import com.timegrid.dto.LoginRequest;
import com.timegrid.dto.RefreshRequest;
import com.timegrid.dto.RefreshResponse;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Permission;
import com.timegrid.entity.RefreshToken;
import com.timegrid.entity.Role;
import com.timegrid.entity.User;
import com.timegrid.enums.LogAction;
import com.timegrid.enums.UserStatus;
import com.timegrid.exception.ForbiddenException;
import com.timegrid.exception.UnauthorizedException;
import com.timegrid.security.AppUserDetails;
import com.timegrid.security.AppUserDetailsService;
import com.timegrid.security.JwtService;
import com.timegrid.service.LogService;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LogService logService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsTokensForActiveUser() {
        AppUserDetails details = activeUser();
        when(userDetailsService.loadUserByUsername("goncalo.esteves")).thenReturn(details);
        when(passwordEncoder.matches("timegrid2026", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), any(), any()))
                .thenReturn(new JwtService.AccessToken("access-1", Instant.now().plusSeconds(900)));
        when(refreshTokenService.create(any(), any()))
                .thenReturn(new RefreshTokenService.IssuedToken(new RefreshToken(), "refresh-1"));

        AuthResponse response = authService.login(new LoginRequest("goncalo.esteves", "timegrid2026"));

        assertThat(response.accessToken()).isEqualTo("access-1");
        assertThat(response.refreshToken()).isEqualTo("refresh-1");
        assertThat(response.roles()).containsExactly("ADMIN");
        verify(logService).record(any(), any(), eq(LogAction.LOGIN_SUCCESS), any(), any(), any());
    }

    @Test
    void loginRejectsWrongPassword() {
        AppUserDetails details = activeUser();
        when(userDetailsService.loadUserByUsername("goncalo.esteves")).thenReturn(details);
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("goncalo.esteves", "errada")))
                .isInstanceOf(UnauthorizedException.class);
        verify(refreshTokenService, never()).create(any(), any());
        verify(logService).record(any(), any(), eq(LogAction.LOGIN_FAILED), any(), any(), eq("user.credentials.invalid"));
    }

    @Test
    void loginRejectsInactiveUser() {
        when(userDetailsService.loadUserByUsername("ines.moura"))
                .thenReturn(userWithStatus(UserStatus.INACTIVE));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ines.moura", "timegrid2026")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void loginRejectsLockedUser() {
        when(userDetailsService.loadUserByUsername("andre.santos"))
                .thenReturn(userWithStatus(UserStatus.LOCKED));

        assertThatThrownBy(() -> authService.login(new LoginRequest("andre.santos", "timegrid2026")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void loginRejectsDeletedUser() {
        when(userDetailsService.loadUserByUsername("beatriz.lopes")).thenReturn(deletedUser());

        assertThatThrownBy(() -> authService.login(new LoginRequest("beatriz.lopes", "timegrid2026")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userDetailsService.loadUserByUsername("ninguem"))
                .thenThrow(new UsernameNotFoundException(""));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ninguem", "timegrid2026")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshRotatesToken() {
        RefreshToken stored = tokenWith(TokenState.ACTIVE);
        AppUserDetails details = activeUser();
        when(refreshTokenService.findByValue("refresh-velho")).thenReturn(Optional.of(stored));
        when(userDetailsService.loadUserById(any())).thenReturn(details);
        when(jwtService.generateAccessToken(any(), any(), any()))
                .thenReturn(new JwtService.AccessToken("access-novo", Instant.now().plusSeconds(900)));
        RefreshToken replacement = new RefreshToken();
        when(refreshTokenService.create(any(), any()))
                .thenReturn(new RefreshTokenService.IssuedToken(replacement, "refresh-novo"));

        RefreshResponse response = authService.refresh(new RefreshRequest("refresh-velho"));

        assertThat(response.accessToken()).isEqualTo("access-novo");
        assertThat(response.refreshToken()).isEqualTo("refresh-novo");
        assertThat(stored.getReplacedBy()).isEqualTo(replacement.getId());
    }

    @Test
    void refreshRejectsRevokedTokenAndRevokesAll() {
        RefreshToken stored = tokenWith(TokenState.REVOKED);
        when(refreshTokenService.findByValue("refresh-usado")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("refresh-usado")))
                .isInstanceOf(UnauthorizedException.class);
        verify(refreshTokenService).revokeAllForUser(eq(stored.getUserId()), any());
    }

    @Test
    void refreshRejectsExpiredToken() {
        RefreshToken stored = tokenWith(TokenState.EXPIRED);
        when(refreshTokenService.findByValue("refresh-expirado")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("refresh-expirado")))
                .isInstanceOf(UnauthorizedException.class);
    }

    private AppUserDetails activeUser() {
        return details(UserStatus.ACTIVE);
    }

    private AppUserDetails deletedUser() {
        User user = baseUser("beatriz.lopes", UserStatus.ACTIVE);
        user.setDeletedAt(Instant.now());
        return new AppUserDetails(user);
    }

    private AppUserDetails userWithStatus(UserStatus status) {
        return details(status);
    }

    private AppUserDetails details(UserStatus status) {
        return new AppUserDetails(baseUser("goncalo.esteves", status));
    }

    private User baseUser(String username, UserStatus status) {
        Organization organization = new Organization();
        organization.setName("Tejo Digital");

        Role role = new Role();
        role.setName("ADMIN");
        Permission permission = new Permission();
        permission.setName("user:read");
        role.setPermissions(Set.of(permission));

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setFullName("Rita Carvalho");
        user.setStatus(status);
        user.setOrganization(organization);
        user.setRoles(Set.of(role));
        return user;
    }

    private RefreshToken tokenWith(TokenState state) {
        RefreshToken token = new RefreshToken();
        token.setUserId(UUID.randomUUID());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        if (state == TokenState.REVOKED || state == TokenState.EXPIRED) {
            if (state == TokenState.REVOKED) {
                token.setRevokedAt(Instant.now());
            } else {
                token.setExpiresAt(Instant.now().minusSeconds(1));
            }
        }
        return token;
    }

    private enum TokenState {
        ACTIVE, REVOKED, EXPIRED
    }

}