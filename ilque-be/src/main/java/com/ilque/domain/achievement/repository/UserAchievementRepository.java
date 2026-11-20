package com.ilque.domain.achievement.repository;

import com.ilque.domain.achievement.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    Optional<UserAchievement> findByUser_UserIdAndAchievement_AchievementId(
            Long userId,
            Long achievementId
    );

    List<UserAchievement> findAllByUser_UserIdOrderByUnlockedAtDesc(Long userId);

    Optional<UserAchievement> findByUser_UserIdAndEquippedTrue(Long userId);

    boolean existsByUser_UserIdAndAchievement_AchievementId(Long userId, Long achievementId);
}
