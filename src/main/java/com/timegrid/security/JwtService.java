package com.timegrid.security;

import com.timegrid.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private static final String ISSUER = "timegrid";
    private static final String CLAIM_ORG = "org";
    private static final String CLAIM_PERMS = "perms";

    private final SecretKey secretKey;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.accessTtl = properties.accessTtl();
        this.refreshTtl = properties.refreshTtl();
    }

    public String generateAccessToken(UUID userId, UUID organizationId,
            Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        List<String> perms = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .claim(CLAIM_ORG, organizationId.toString())
                .claim(CLAIM_PERMS, perms)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTtl)))
                .signWith(secretKey)
                .compact();
    }

    public Optional<Claims> parse(String token) {
        return Optional.ofNullable(token)
                .flatMap(t -> {
                    try {
                        return Optional.of(Jwts.parser()
                                .verifyWith(secretKey)
                                .requireIssuer(ISSUER)
                                .build()
                                .parseSignedClaims(t)
                                .getPayload());
                    } catch (RuntimeException e) {
                        return Optional.empty();
                    }
                });
    }

    public UUID subject(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public UUID organizationId(Claims claims) {
        return UUID.fromString(claims.get(CLAIM_ORG, String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> permissions(Claims claims) {
        return List.copyOf(claims.get(CLAIM_PERMS, List.class));
    }

}
