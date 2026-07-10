package com.timegrid.service;

import com.timegrid.dto.AuthResponse;
import com.timegrid.dto.LoginRequest;
import com.timegrid.dto.LogoutRequest;
import com.timegrid.dto.RefreshRequest;
import com.timegrid.dto.RefreshResponse;
import com.timegrid.entity.RefreshToken;
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

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUserDetails user = authenticate(request.username(), request.password());
        JwtService.AccessToken accessToken = jwtService.generateAccessToken(
                user.getId(), user.getOrganizationId(), user.getAuthorities());
        RefreshTokenService.IssuedToken refreshToken = refreshTokenService.create(
                user.getId(), Instant.now().plus(jwtService.refreshTtl()));
        return AuthResponse.from(user, accessToken.value(), accessToken.expiresAt(),
                refreshToken.rawValue());
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshToken token = refreshTokenService.findByValue(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("auth.refresh.invalid"));

        Instant now = Instant.now();
        if (token.isRevoked()) {
            // um token revogado só volta a aparecer aqui se alguém o copiou antes da rotação (ex.: roubado);
            // não há como saber se foi o dono ou o atacante que chegou primeiro, por isso corta todos desse utilizador
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
        refreshTokenService.findByValue(request.refreshToken())
                .ifPresent(token -> refreshTokenService.revoke(token, Instant.now()));
    }

    private AppUserDetails authenticate(String username, String password) {
        AppUserDetails user;
        try {
            user = (AppUserDetails) userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new UnauthorizedException("user.credentials.invalid");
        }

        if (user.isSoftDeleted()) {
            throw new UnauthorizedException("user.deleted");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ForbiddenException("This user account is locked.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user.inactive");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("user.credentials.invalid");
        }
        return user;
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