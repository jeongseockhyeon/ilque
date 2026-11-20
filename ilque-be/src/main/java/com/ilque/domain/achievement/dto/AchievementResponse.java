package com.ilque.domain.achievement.dto;

import com.ilque.domain.achievement.entity.Achievement;
import com.ilque.domain.achievement.entity.AchievementConditionType;
import com.ilque.domain.achievement.entity.AchievementTier;
import com.ilque.domain.achievement.entity.UserAchievement;

import java.time.Instant;

public record AchievementResponse(
        Long achievementId,
        String code,
        String title,
        String description,
        AchievementConditionType conditionType,
        Integer conditionValue,
        AchievementTier tier,
        Integer rewardExp,
        Boolean isHidden,
        String badgeIcon,
        Boolean unlocked,
        Instant unlockedAt,
        Boolean equipped
) {

    public static AchievementResponse from(Achievement achievement, UserAchievement userAchievement) {
        return new AchievementResponse(
                achievement.getAchievementId(),
                achievement.getCode(),
                achievement.getTitle(),
                achievement.getDescription(),
                achievement.getConditionType(),
                achievement.getConditionValue(),
                achievement.getTier(),
                achievement.getRewardExp(),
                achievement.getIsHidden(),
                achievement.getBadgeIcon(),
                userAchievement != null,
                userAchievement == null ? null : userAchievement.getUnlockedAt(),
                userAchievement != null && Boolean.TRUE.equals(userAchievement.getEquipped())
        );
    }

    public static AchievementResponse from(UserAchievement userAchievement) {
        return from(userAchievement.getAchievement(), userAchievement);
    }
}
