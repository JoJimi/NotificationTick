package org.example.backend.global.jwt.redis;

public record RefreshSession(String familyId, String currentJti, long rotatedAt) { }

