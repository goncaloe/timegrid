package com.timegrid.dto;

import java.time.Instant;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        Instant expiresAt) {
}
