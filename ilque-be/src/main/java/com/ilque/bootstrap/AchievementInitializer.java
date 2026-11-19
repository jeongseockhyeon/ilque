package com.ilque.bootstrap;

import com.ilque.domain.achievement.entity.Achievement;
import com.ilque.domain.achievement.entity.AchievementConditionType;
import com.ilque.domain.achievement.entity.AchievementTier;
import com.ilque.domain.achievement.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AchievementInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AchievementInitializer.class);

    private final AchievementRepository achievementRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        log.info("Achievement 초기화를 시작합니다.");

        seed(
                "FIRST_CLEAR",
                "모험의 시작",
                "첫 Quest를 완료했습니다.",
                AchievementConditionType.FIRST_CLEAR,
                1,
                AchievementTier.BRONZE,
                20
        );
        seed(
                "STREAK_3",
                "작심삼일 극복",
                "3일 연속 Quest를 완료했습니다.",
                AchievementConditionType.STREAK,
                3,
                AchievementTier.SILVER,
                50
        );
        seed(
                "STREAK_7",
                "습관의 형성",
                "7일 연속 Quest를 완료했습니다.",
                AchievementConditionType.STREAK,
                7,
                AchievementTier.GOLD,
                100
        );
        seed(
                "FOCUS_10H",
                "극강의 몰입자",
                "누적 Focus 10시간을 달성했습니다.",
                AchievementConditionType.FOCUS_MINUTES,
                600,
                AchievementTier.PLATINUM,
                100
        );
        seed(
                "QUEST_100",
                "퀘스트 마스터",
                "Quest를 100회 완료했습니다.",
                AchievementConditionType.COMPLETED_COUNT,
                100,
                AchievementTier.DIAMOND,
                150
        );

        log.info("Achievement 초기화를 완료했습니다.");
    }

    private void seed(
            String code,
            String title,
            String description,
            AchievementConditionType conditionType,
            int conditionValue,
            AchievementTier tier,
            int rewardExp
    ) {
        if (achievementRepository.existsByCode(code)) {
            return;
        }

        achievementRepository.save(Achievement.create(
                code,
                title,
                description,
                conditionType,
                conditionValue,
                tier,
                rewardExp,
                false,
                null
        ));
    }
}
