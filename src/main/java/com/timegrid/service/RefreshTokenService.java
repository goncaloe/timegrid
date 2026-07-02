package com.timegrid.service;

import com.timegrid.entity.RefreshToken;
import com.timegrid.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    public static String newTokenValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Transactional
    public IssuedToken create(UUID userId, Instant expiresAt) {
        String raw = newTokenValue();
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(hash(raw));
        entity.setExpiresAt(expiresAt);
        refreshTokenRepository.save(entity);
        return new IssuedToken(entity, raw);
    }

    @Transactional
    public Optional<RefreshToken> findByValue(String token) {
        return refreshTokenRepository.findByTokenHash(hash(token));
    }

    @Transactional
    public void revoke(RefreshToken token, Instant now) {
        token.setRevokedAt(now);
    }

    @Transactional
    public void revokeAllForUser(UUID userId, Instant now) {
        refreshTokenRepository.revokeAllActiveForUser(userId, now);
    }

    public record IssuedToken(RefreshToken entity, String rawValue) {
    }

}
