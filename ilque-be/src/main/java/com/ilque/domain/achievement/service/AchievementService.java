package com.ilque.domain.achievement.service;

import com.ilque.domain.achievement.dto.AchievementResponse;
import com.ilque.domain.achievement.entity.Achievement;
import com.ilque.domain.achievement.entity.UserAchievement;
import com.ilque.domain.achievement.repository.AchievementRepository;
import com.ilque.domain.achievement.repository.UserAchievementRepository;
import com.ilque.domain.execution.entity.ExecutionStatus;
import com.ilque.domain.execution.repository.QuestExecutionRepository;
import com.ilque.domain.focus.entity.FocusSessionStatus;
import com.ilque.domain.focus.repository.FocusSessionRepository;
import com.ilque.domain.exp.service.ExpLevelService;
import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.entity.UserStatisticsSummary;
import com.ilque.domain.user.repository.UserStatisticsSummaryRepository;
import com.ilque.domain.user.service.UserService;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserService userService;
    private final QuestExecutionRepository questExecutionRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final UserStatisticsSummaryRepository userStatisticsSummaryRepository;
    private final ExpLevelService expLevelService;

    @Transactional(readOnly = true)
    public List<AchievementResponse> findAll() {
        User user = findLocalUser();
        Map<Long, UserAchievement> userAchievements = userAchievementRepository
                .findAllByUser_UserIdOrderByUnlockedAtDesc(user.getUserId())
                .stream()
                .collect(Collectors.toMap(
                        userAchievement -> userAchievement.getAchievement().getAchievementId(),
                        Function.identity()
                ));

        return achievementRepository.findAll().stream()
                .map(achievement -> AchievementResponse.from(
                        achievement,
                        userAchievements.get(achievement.getAchievementId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> findUnlocked() {
        User user = findLocalUser();
        return userAchievementRepository.findAllByUser_UserIdOrderByUnlockedAtDesc(user.getUserId())
                .stream()
                .map(AchievementResponse::from)
                .toList();
    }

    @Transactional
    public AchievementResponse equip(Long achievementId) {
        User user = findLocalUser();
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 Achievement를 찾을 수 없습니다."));
        UserAchievement userAchievement = userAchievementRepository
                .findByUser_UserIdAndAchievement_AchievementId(user.getUserId(), achievementId)
                .orElseThrow(() -> new BadRequestException("해금된 Achievement만 장착할 수 있습니다."));

        userAchievementRepository.findByUser_UserIdAndEquippedTrue(user.getUserId())
                .filter(equippedAchievement -> equippedAchievement != userAchievement)
                .ifPresent(UserAchievement::unequip);
        userAchievement.equip();

        return AchievementResponse.from(achievement, userAchievement);
    }

    @Transactional
    public void checkAndUnlock(User user) {
        if (user == null || user.getUserId() == null) {
            throw new BadRequestException("저장된 user는 필수입니다.");
        }

        for (Achievement achievement : achievementRepository.findAll()) {
            if (!isAchieved(user, achievement)
                    || userAchievementRepository.existsByUser_UserIdAndAchievement_AchievementId(
                    user.getUserId(), achievement.getAchievementId())) {
                continue;
            }

            userAchievementRepository.save(UserAchievement.unlock(user, achievement, Instant.now()));
            expLevelService.addEarnedExp(user, achievement.getRewardExp());
        }
    }

    private boolean isAchieved(User user, Achievement achievement) {
        return switch (achievement.getConditionType()) {
            case FIRST_CLEAR, COMPLETED_COUNT ->
                    questExecutionRepository.countByQuest_User_UserIdAndStatus(
                            user.getUserId(), ExecutionStatus.COMPLETED) >= achievement.getConditionValue();
            case STREAK -> isStreakAchieved(user, achievement.getConditionValue());
            case FOCUS_MINUTES -> focusSessionRepository.sumDurationMinutesByUserIdAndStatus(
                    user.getUserId(), FocusSessionStatus.STOPPED) >= achievement.getConditionValue();
        };
    }

    private boolean isStreakAchieved(User user, int conditionValue) {
        return userStatisticsSummaryRepository.findByUser_UserId(user.getUserId())
                .map(summary -> hasReachedStreak(summary, conditionValue))
                .orElse(false);
    }

    private boolean hasReachedStreak(UserStatisticsSummary summary, int conditionValue) {
        return summary.getCurrentStreak() >= conditionValue
                || summary.getLongestStreak() >= conditionValue;
    }

    private User findLocalUser() {
        return userService.findLocalUser()
                .orElseThrow(() -> new ResourceNotFoundException("Local User를 찾을 수 없습니다."));
    }
}
