package com.ilque.domain.achievement.entity;

import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "achievement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_achievement_code",
                columnNames = "code"
        )
)
public class Achievement extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long achievementId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementConditionType conditionType;

    @Column(nullable = false)
    private Integer conditionValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementTier tier;

    @Column(nullable = false)
    private Integer rewardExp;

    @Column(nullable = false)
    private Boolean isHidden = false;

    @Column(length = 255)
    private String badgeIcon;

    private Achievement(
            String code,
            String title,
            String description,
            AchievementConditionType conditionType,
            Integer conditionValue,
            AchievementTier tier,
            Integer rewardExp,
            Boolean isHidden,
            String badgeIcon
    ) {
        this.code = normalizeRequired(code, "code");
        this.title = normalizeRequired(title, "title");
        this.description = normalizeRequired(description, "description");
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.tier = tier;
        this.rewardExp = rewardExp;
        this.isHidden = isHidden == null ? false : isHidden;
        this.badgeIcon = normalizeNullable(badgeIcon);
        validateState();
    }

    public static Achievement create(
            String code,
            String title,
            String description,
            AchievementConditionType conditionType,
            Integer conditionValue,
            AchievementTier tier,
            Integer rewardExp,
            Boolean isHidden,
            String badgeIcon
    ) {
        return new Achievement(code, title, description, conditionType, conditionValue,
                tier, rewardExp, isHidden, badgeIcon);
    }

    @PrePersist
    @PreUpdate
    private void validateBeforePersist() {
        validateState();
    }

    private void validateState() {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("code는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new BadRequestException("title은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new BadRequestException("description은 필수입니다.");
        }
        if (conditionType == null) {
            throw new BadRequestException("conditionType은 필수입니다.");
        }
        if (conditionValue == null || conditionValue < 1) {
            throw new BadRequestException("conditionValue는 1 이상이어야 합니다.");
        }
        if (tier == null) {
            throw new BadRequestException("tier는 필수입니다.");
        }
        if (rewardExp == null || rewardExp < 0) {
            throw new BadRequestException("rewardExp는 0 이상이어야 합니다.");
        }
        if (isHidden == null) {
            throw new BadRequestException("isHidden은 필수입니다.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + "은 필수입니다.");
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
