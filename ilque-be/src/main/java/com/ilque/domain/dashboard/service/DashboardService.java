package com.ilque.domain.dashboard.service;

import com.ilque.domain.exp.service.ExpLevelService;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.RecurrenceType;
import com.ilque.domain.quest.repository.QuestRepository;
import com.ilque.domain.resetpolicy.calculator.ResetBoundaryCalculator;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.entity.UserStatisticsSummary;
import com.ilque.domain.user.repository.UserStatisticsSummaryRepository;
import com.ilque.domain.user.service.UserService;
import com.ilque.domain.dashboard.dto.DashboardResponse;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final UserStatisticsSummaryRepository summaryRepository;
    private final QuestRepository questRepository;
    private final ExpLevelService expLevelService;
    private final ResetBoundaryCalculator resetBoundaryCalculator;

    @Transactional(readOnly = true)
    public DashboardResponse findDashboard(Instant now) {
        User user = userService.findLocalUser()
                .orElseThrow(() -> new ResourceNotFoundException("Local User를 찾을 수 없습니다."));

        long totalExp = user.getTotalExp();
        int level = user.getLevel();
        long previousLevelRequiredExp = level == 1 ? 0 : expLevelService.requiredExp(level - 1);
        long nextLevelRequiredExp = expLevelService.requiredExp(level);
        UserStatisticsSummary summary = summaryRepository.findByUser_UserId(user.getUserId()).orElse(null);

        return new DashboardResponse(
                totalExp,
                level,
                totalExp - previousLevelRequiredExp,
                nextLevelRequiredExp,
                summary == null ? 0 : summary.getCurrentStreak(),
                summary == null ? 0 : summary.getLongestStreak(),
                findNextResetAt(user.getUserId(), now)
        );
    }

    private Instant findNextResetAt(Long userId, Instant now) {
        return questRepository.findAllByUser_UserIdAndActiveTrue(userId).stream()
                .filter(this::hasRecurringActivePolicy)
                .map(quest -> nextBoundary(quest, now))
                .min(Instant::compareTo)
                .orElse(null);
    }

    private boolean hasRecurringActivePolicy(Quest quest) {
        return quest.getRecurrenceType() != RecurrenceType.ONCE
                && quest.getResetPolicy() != null
                && Boolean.TRUE.equals(quest.getResetPolicy().getActive());
    }

    private Instant nextBoundary(Quest quest, Instant now) {
        ResetPolicy policy = quest.getResetPolicy();
        Instant boundary = resetBoundaryCalculator.calculateFirstBoundary(quest.getCreatedAt(), policy);
        while (!boundary.isAfter(now)) {
            boundary = resetBoundaryCalculator.calculateNextBoundary(boundary, policy);
        }
        return boundary;
    }
}
