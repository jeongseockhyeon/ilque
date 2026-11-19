package com.ilque.domain.achievement.entity;

import com.ilque.domain.user.entity.User;
import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_achievement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_achievement_user_achievement",
                columnNames = {"user_id", "achievement_id"}
        )
)
public class UserAchievement extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userAchievementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false)
    private Instant unlockedAt;

    @Column(nullable = false)
    private Boolean equipped = false;

    private UserAchievement(User user, Achievement achievement, Instant unlockedAt) {
        if (user == null) {
            throw new BadRequestException("user는 필수입니다.");
        }
        if (achievement == null) {
            throw new BadRequestException("achievement는 필수입니다.");
        }
        if (unlockedAt == null) {
            throw new BadRequestException("unlockedAt은 필수입니다.");
        }
        this.user = user;
        this.achievement = achievement;
        this.unlockedAt = unlockedAt;
        this.equipped = false;
    }

    public static UserAchievement unlock(User user, Achievement achievement, Instant unlockedAt) {
        return new UserAchievement(user, achievement, unlockedAt);
    }

    public void equip() {
        this.equipped = true;
    }

    public void unequip() {
        this.equipped = false;
    }
}
