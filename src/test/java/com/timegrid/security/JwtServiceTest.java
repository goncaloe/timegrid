package com.timegrid.security;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.timegrid.config.JwtProperties;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

    private static final String SECRET = "bXktdGltZWdyaWQtZGV2LXNlY3JldC1rZXktZm9yLWpzb24td2ViLXRva2Vucy10aW1lZ3JpZA==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(15), Duration.ofDays(14)));
    }

    @Test
    void generatesAccessTokenWithUserClaims() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        JwtService.AccessToken access = jwtService.generateAccessToken(userId, orgId,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("user:read")));

        Claims claims = jwtService.parse(access.value()).orElseThrow();
        assertThat(jwtService.subject(claims)).isEqualTo(userId);
        assertThat(jwtService.organizationId(claims)).isEqualTo(orgId);
        assertThat(jwtService.permissions(claims)).contains("ROLE_ADMIN", "user:read");
    }

    @Test
    void parseReturnsEmptyForInvalidToken() {
        assertThat(jwtService.parse("token-invalido")).isEmpty();
    }

    @Test
    void parseReturnsEmptyForTokenSignedWithOtherKey() {
        JwtService other = new JwtService(new JwtProperties(
                "c2VncmVkby1vdXRyby1jaGF2ZS1wYXJhLXRlc3RlLWFscGhhLW51bWVyaWNvLTEyMw==",
                Duration.ofMinutes(15), Duration.ofDays(14)));
        JwtService.AccessToken token = other.generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), List.of());

        assertThat(jwtService.parse(token.value())).isEmpty();
    }

}
