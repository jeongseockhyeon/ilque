package com.ilque.domain.streak.service;

import com.ilque.domain.execution.entity.ExecutionStatus;
import com.ilque.domain.execution.entity.QuestExecution;
import com.ilque.domain.execution.repository.QuestExecutionRepository;
import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.entity.UserStatisticsSummary;
import com.ilque.domain.user.repository.UserStatisticsSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StreakService {

    private static final ZoneId STREAK_ZONE = ZoneId.of("Asia/Seoul");

    private final QuestExecutionRepository questExecutionRepository;
    private final UserStatisticsSummaryRepository summaryRepository;

    @Transactional
    public void recalculate(User user) {
        UserStatisticsSummary summary = summaryRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> summaryRepository.save(UserStatisticsSummary.create(user)));

        List<QuestExecution> executions = questExecutionRepository
                .findAllByQuest_User_UserIdOrderByPeriodStartAsc(user.getUserId());
        Map<LocalDate, DayResult> results = new HashMap<>();

        for (QuestExecution execution : executions) {
            LocalDate date = executionDate(execution);
            DayResult result = results.computeIfAbsent(date, ignored -> new DayResult());
            if (execution.getStatus() == ExecutionStatus.COMPLETED) {
                result.completed = true;
            } else if (execution.getStatus() == ExecutionStatus.MISSED) {
                result.missed = true;
            } else if (execution.getStatus() == ExecutionStatus.REST_DAY) {
                result.restDay = true;
            } else if (execution.getStatus() == ExecutionStatus.SKIPPED) {
                result.skipped = true;
            }
        }

        if (results.isEmpty()) {
            summary.updateStreaks(0, 0, null);
            return;
        }

        LocalDate firstDate = results.keySet().stream().min(LocalDate::compareTo).orElseThrow();
        LocalDate lastDate = results.keySet().stream().max(LocalDate::compareTo).orElseThrow();
        int current = 0;
        int longest = 0;
        LocalDate lastCompletedDate = null;

        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            DayResult result = results.get(date);
            if (result == null) {
                current = 0;
                continue;
            }
            if (result.completed) {
                current++;
                longest = Math.max(longest, current);
                lastCompletedDate = date;
            } else if (result.missed) {
                current = 0;
            }
        }

        summary.updateStreaks(current, longest, lastCompletedDate);
    }

    private LocalDate executionDate(QuestExecution execution) {
        if (execution.getCompletedAt() != null) {
            return execution.getCompletedAt().atZone(STREAK_ZONE).toLocalDate();
        }
        return execution.getPeriodStart().atZone(STREAK_ZONE).toLocalDate();
    }

    private static class DayResult {
        private boolean completed;
        private boolean missed;
        private boolean restDay;
        private boolean skipped;
    }
}
