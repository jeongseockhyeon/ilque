package com.ilque.domain.achievement.service;

import com.ilque.domain.achievement.dto.AchievementResponse;
import com.ilque.domain.achievement.entity.Achievement;
import com.ilque.domain.achievement.entity.UserAchievement;
import com.ilque.domain.achievement.repository.AchievementRepository;
import com.ilque.domain.achievement.repository.UserAchievementRepository;
import com.ilque.domain.user.entity.User;
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

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserService userService;

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

    private User findLocalUser() {
        return userService.findLocalUser()
                .orElseThrow(() -> new ResourceNotFoundException("Local User를 찾을 수 없습니다."));
    }
}
