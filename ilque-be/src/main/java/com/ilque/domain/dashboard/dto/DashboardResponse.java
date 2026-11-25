package com.ilque.domain.dashboard.dto;

import java.time.Instant;

public record DashboardResponse(
        long totalExp,
        int level,
        long currentLevelExp,
        long nextLevelRequiredExp,
        int currentStreak,
        int longestStreak,
        Instant nextResetAt
) {
}
