package com.timegrid.service;

import com.timegrid.dto.AuthResponse;
import com.timegrid.dto.LoginRequest;
import com.timegrid.enums.UserStatus;
import com.timegrid.exception.ForbiddenException;
import com.timegrid.exception.UnauthorizedException;
import com.timegrid.security.AppUserDetails;
import com.timegrid.security.AppUserDetailsService;
import com.timegrid.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        AppUserDetails user;
        try {
            user = (AppUserDetails) userDetailsService.loadUserByUsername(request.username());
        } catch (UsernameNotFoundException e) {
            throw new UnauthorizedException("user.credentials.invalid");
        }

        if (user.isSoftDeleted()) {
            throw new UnauthorizedException("user.deleted");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ForbiddenException("user.locked");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user.inactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("user.credentials.invalid");
        }

        JwtService.AccessToken accessToken = jwtService.generateAccessToken(
                user.getId(), user.getOrganizationId(), user.getAuthorities());
        return AuthResponse.from(user, accessToken.value(), accessToken.expiresAt());
    }

}
