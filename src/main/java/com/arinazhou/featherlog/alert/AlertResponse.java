package com.arinazhou.featherlog.alert;

import java.time.Instant;

public record AlertResponse(
        Long id,
        Long birdId,
        Long careTaskId,
        AlertType type,
        Severity severity,
        String message,
        Instant createdAt,
        Instant resolvedAt) {

    public static AlertResponse from(HealthAlert a) {
        return new AlertResponse(a.getId(), a.getBird().getId(),
                a.getCareTask() == null ? null : a.getCareTask().getId(),
                a.getType(), a.getSeverity(), a.getMessage(), a.getCreatedAt(), a.getResolvedAt());
    }
}
