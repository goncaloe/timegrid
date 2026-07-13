package com.timegrid.service;

import com.timegrid.dto.AuthResponse;
import com.timegrid.dto.LoginRequest;
import com.timegrid.dto.LogoutRequest;
import com.timegrid.dto.RefreshRequest;
import com.timegrid.dto.RefreshResponse;
import com.timegrid.entity.RefreshToken;
import com.timegrid.enums.LogAction;
import com.timegrid.enums.UserStatus;
import com.timegrid.exception.ForbiddenException;
import com.timegrid.exception.UnauthorizedException;
import com.timegrid.security.AppUserDetails;
import com.timegrid.security.AppUserDetailsService;
import com.timegrid.security.JwtService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LogService logService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUserDetails user = authenticate(request.username(), request.password());
        JwtService.AccessToken accessToken = jwtService.generateAccessToken(
                user.getId(), user.getOrganizationId(), user.getAuthorities());
        RefreshTokenService.IssuedToken refreshToken = refreshTokenService.create(
                user.getId(), Instant.now().plus(jwtService.refreshTtl()));
        AuthResponse response = AuthResponse.from(user, accessToken.value(), accessToken.expiresAt(),
                refreshToken.rawValue());
        logService.record(user.getOrganizationId(), user.getId(), LogAction.LOGIN_SUCCESS, null, null, null);
        return response;
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshToken token = refreshTokenService.findByValue(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("auth.refresh.invalid"));

        Instant now = Instant.now();
        if (token.isRevoked()) {
            // um token revogado só volta a aparecer aqui se alguém o copiou antes da rotação (ex.: roubado);
            // não há como saber se foi o dono ou o atacante que chegou primeiro, por isso corta tudo
            refreshTokenService.revokeAllForUser(token.getUserId(), now);
            throw new UnauthorizedException("auth.refresh.reused");
        }
        if (token.isExpired(now)) {
            refreshTokenService.revoke(token, now);
            throw new UnauthorizedException("auth.refresh.invalid");
        }

        AppUserDetails user = loadActive(token.getUserId());
        refreshTokenService.revoke(token, now);
        JwtService.AccessToken accessToken = jwtService.generateAccessToken(
                user.getId(), user.getOrganizationId(), user.getAuthorities());
        RefreshTokenService.IssuedToken refreshed = refreshTokenService.create(
                user.getId(), now.plus(jwtService.refreshTtl()));
        token.setReplacedBy(refreshed.entity().getId());
        return new RefreshResponse(accessToken.value(), refreshed.rawValue(), accessToken.expiresAt());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.findByValue(request.refreshToken()).ifPresent(token -> {
            refreshTokenService.revoke(token, Instant.now());
            AppUserDetails user = (AppUserDetails) userDetailsService.loadUserById(token.getUserId());
            logService.record(user.getOrganizationId(), user.getId(), LogAction.LOGOUT, null, null, null);
        });
    }

    private AppUserDetails authenticate(String username, String password) {
        AppUserDetails user;
        try {
            user = (AppUserDetails) userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            logService.record(null, null, LogAction.LOGIN_FAILED, null, null, "username: " + username);
            throw new UnauthorizedException("user.credentials.invalid");
        }

        try {
            checkCanLogin(user, password);
        } catch (UnauthorizedException | ForbiddenException e) {
            logService.record(user.getOrganizationId(), user.getId(), LogAction.LOGIN_FAILED, null, null,
                    e.getMessage());
            throw e;
        }
        return user;
    }

    private void checkCanLogin(AppUserDetails user, String password) {
        if (user.isSoftDeleted()) {
            throw new UnauthorizedException("user.deleted");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ForbiddenException("user.locked");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user.inactive");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("user.credentials.invalid");
        }
    }

    private AppUserDetails loadActive(UUID userId) {
        AppUserDetails user;
        try {
            user = (AppUserDetails) userDetailsService.loadUserById(userId);
        } catch (UsernameNotFoundException e) {
            throw new UnauthorizedException("auth.refresh.invalid");
        }
        if (user.isSoftDeleted() || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("auth.refresh.invalid");
        }
        return user;
    }

}