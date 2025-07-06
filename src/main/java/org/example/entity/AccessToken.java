package org.example.entity;

import java.time.Instant;

public record AccessToken(String accessToken, Instant expiresAt) {
}
