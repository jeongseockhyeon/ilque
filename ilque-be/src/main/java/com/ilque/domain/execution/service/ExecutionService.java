package com.ilque.domain.execution.service;

import com.ilque.domain.execution.entity.QuestExecution;
import com.ilque.domain.execution.entity.ExecutionStatus;
import com.ilque.domain.execution.dto.CompleteExecutionRequest;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.domain.execution.repository.QuestExecutionRepository;
import com.ilque.domain.exp.service.ExpLevelService;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.RecurrenceType;
import com.ilque.domain.quest.repository.QuestRepository;
import com.ilque.domain.resetpolicy.calculator.ResetBoundaryCalculator;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.streak.service.StreakService;
import com.ilque.domain.user.entity.UserStatisticsSummary;
import com.ilque.domain.user.repository.UserStatisticsSummaryRepository;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final QuestRepository questRepository;
    private final QuestExecutionRepository questExecutionRepository;
    private final ResetBoundaryCalculator resetBoundaryCalculator;
    private final UserStatisticsSummaryRepository userStatisticsSummaryRepository;
    private final ExpLevelService expLevelService;
    private final StreakService streakService;

    @Transactional
    public QuestExecution ensureCurrentExecution(Long questId, Instant now) {
        Quest quest = questRepository.findByQuestIdAndActiveTrue(questId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 활성 Quest를 찾을 수 없습니다."));
        return ensureCurrentExecution(quest, now);
    }

    @Transactional
    public QuestExecution ensureCurrentExecution(Quest quest, Instant now) {
        if (quest == null) {
            throw new BadRequestException("Quest는 필수입니다.");
        }
        if (now == null) {
            throw new BadRequestException("기준 시각은 필수입니다.");
        }
        if (quest.getCreatedAt() == null) {
            throw new BadRequestException("생성 시각이 없는 Quest는 Execution을 생성할 수 없습니다.");
        }

        if (quest.getRecurrenceType() == RecurrenceType.ONCE) {
            return findOrCreate(quest, quest.getCreatedAt(), null);
        }

        validatePolicyCombination(quest);
        recoverMissedExecutions(quest, now);
        ResetBoundaryCalculator.PeriodBoundary period = resetBoundaryCalculator.calculateCurrentPeriod(
                quest.getCreatedAt(), now, quest.getResetPolicy());

        if (quest.getRecurrenceType() == RecurrenceType.CUSTOM
                && !isTargetDay(quest, period.start(), quest.getResetPolicy().getTimezone())) {
            return null;
        }

        return findOrCreate(quest, period.start(), period.end());
    }

    private void recoverMissedExecutions(Quest quest, Instant now) {
        ResetBoundaryCalculator.PeriodBoundary current = resetBoundaryCalculator.calculateCurrentPeriod(
                quest.getCreatedAt(), now, quest.getResetPolicy());
        Instant latestProcessed = quest.getLastProcessedResetAt();
        if (latestProcessed != null && !latestProcessed.isBefore(current.start())) {
            return;
        }

        List<ResetBoundaryCalculator.PeriodBoundary> missingPeriods = findMissingPeriods(quest, current);
        List<ResetBoundaryCalculator.PeriodBoundary> eligibleMissingPeriods = missingPeriods.stream()
                .filter(period -> isExecutionTarget(quest, period.start()))
                .filter(period -> questExecutionRepository
                        .findByQuest_QuestIdAndPeriodStart(quest.getQuestId(), period.start()).isEmpty())
                .toList();

        int summaryCount = Math.max(0, eligibleMissingPeriods.size() - 7);
        if (summaryCount > 0) {
            UserStatisticsSummary summary = userStatisticsSummaryRepository
                    .findByUser_UserId(quest.getUser().getUserId())
                    .orElseGet(() -> userStatisticsSummaryRepository
                            .save(UserStatisticsSummary.create(quest.getUser())));
            summary.addLongTermMissedCount(summaryCount);
        }

        int firstDetailedIndex = Math.max(0, eligibleMissingPeriods.size() - 7);
        for (ResetBoundaryCalculator.PeriodBoundary period : eligibleMissingPeriods.subList(firstDetailedIndex,
                eligibleMissingPeriods.size())) {
            QuestExecution execution = QuestExecution.create(quest, period.start(), period.end());
            execution.markMissed();
            questExecutionRepository.save(execution);
        }
        if (!eligibleMissingPeriods.isEmpty()) {
            streakService.recalculate(quest.getUser());
        }
        quest.markResetProcessed(current.start());
    }

    private List<ResetBoundaryCalculator.PeriodBoundary> findMissingPeriods(
            Quest quest,
            ResetBoundaryCalculator.PeriodBoundary current
    ) {
        List<ResetBoundaryCalculator.PeriodBoundary> periods = new ArrayList<>();
        Instant firstBoundary = resetBoundaryCalculator.calculateFirstBoundary(
                quest.getCreatedAt(), quest.getResetPolicy());
        if (firstBoundary.isAfter(current.start())) {
            return periods;
        }

        ResetBoundaryCalculator.PeriodBoundary period = new ResetBoundaryCalculator.PeriodBoundary(
                quest.getCreatedAt(), firstBoundary);
        while (period.start().isBefore(current.start())) {
            if (quest.getLastProcessedResetAt() == null
                    || period.start().isAfter(quest.getLastProcessedResetAt())) {
                periods.add(period);
            }
            Instant nextBoundary = resetBoundaryCalculator.calculateNextBoundary(
                    period.end(), quest.getResetPolicy());
            period = new ResetBoundaryCalculator.PeriodBoundary(period.end(), nextBoundary);
        }
        return periods;
    }

    @Transactional
    public QuestExecution start(Long questId) {
        QuestExecution execution = ensureCurrentExecution(questId, Instant.now());
        if (execution == null) {
            throw new BadRequestException("현재 날짜는 해당 CUSTOM Quest의 수행일이 아닙니다.");
        }
        if (execution.getStatus() == ExecutionStatus.IN_PROGRESS) {
            return execution;
        }
        execution.start(Instant.now());
        return execution;
    }

    @Transactional
    public QuestExecution skip(Long questExecutionId) {
        QuestExecution execution = findExecution(questExecutionId);
        execution.skip();
        if (execution.getQuest() != null) {
            streakService.recalculate(execution.getQuest().getUser());
        }
        return execution;
    }

    @Transactional
    public QuestExecution markRestDay(Long questExecutionId) {
        QuestExecution execution = findExecution(questExecutionId);
        execution.markRestDay();
        if (execution.getQuest() != null) {
            streakService.recalculate(execution.getQuest().getUser());
        }
        return execution;
    }

    @Transactional(readOnly = true)
    public List<QuestExecution> findHistory(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("from과 to는 필수입니다.");
        }
        if (!from.isBefore(to)) {
            throw new BadRequestException("from은 to보다 이전이어야 합니다.");
        }
        return questExecutionRepository
                .findAllByPeriodStartGreaterThanEqualAndPeriodStartLessThanOrderByPeriodStartDescCompletedAtDesc(
                        from, to);
    }

    @Transactional
    public QuestExecution complete(Long questExecutionId, CompleteExecutionRequest request) {
        QuestExecution execution = findExecution(questExecutionId);
        if (execution.getStatus() == ExecutionStatus.COMPLETED) {
            return execution;
        }
        if (request == null) {
            throw new BadRequestException("완료 요청 본문은 필수입니다.");
        }

        validateCompleteRequest(execution, request);
        Instant now = Instant.now();
        if (execution.getQuestTypeSnapshot() == QuestType.CHECK) {
            if (request.actualCount() < execution.getTargetCount()) {
                execution.recordProgress(request.actualCount(), null, now);
                return execution;
            }
            execution.complete(
                    request.actualCount(),
                    null,
                    execution.getBaseExp() + execution.getCompletionBonus(),
                    now);
            expLevelService.addEarnedExp(execution.getQuest().getUser(), execution.getEarnedExp());
            streakService.recalculate(execution.getQuest().getUser());
            return execution;
        }

        int targetDuration = execution.getTargetDuration();
        int actualDuration = request.actualDuration();
        boolean reachesCompletionThreshold = actualDuration * 100 >= targetDuration * 80;
        boolean partialCompletion = Boolean.TRUE.equals(request.partialCompletion());
        if (!reachesCompletionThreshold && !partialCompletion) {
            execution.recordProgress(null, actualDuration, now);
            return execution;
        }

        int timeExp = Math.min(actualDuration, targetDuration) * execution.getExpPerMinute();
        int earnedExp = execution.getBaseExp() + timeExp
                + (reachesCompletionThreshold ? execution.getCompletionBonus() : 0);
        execution.complete(null, actualDuration, earnedExp, now);
        expLevelService.addEarnedExp(execution.getQuest().getUser(), execution.getEarnedExp());
        streakService.recalculate(execution.getQuest().getUser());
        return execution;
    }

    private void validateCompleteRequest(QuestExecution execution, CompleteExecutionRequest request) {
        if (execution.getQuestTypeSnapshot() == QuestType.CHECK) {
            if (request.actualCount() == null || request.actualCount() < 0) {
                throw new BadRequestException("CHECK Quest의 actualCount는 0 이상이어야 합니다.");
            }
            if (request.actualDuration() != null) {
                throw new BadRequestException("CHECK Quest에서는 actualDuration을 사용할 수 없습니다.");
            }
            return;
        }

        if (request.actualDuration() == null || request.actualDuration() < 0) {
            throw new BadRequestException("TIME_BASED Quest의 actualDuration은 0 이상이어야 합니다.");
        }
        if (request.actualCount() != null) {
            throw new BadRequestException("TIME_BASED Quest에서는 actualCount를 사용할 수 없습니다.");
        }
    }

    private QuestExecution findExecution(Long questExecutionId) {
        return questExecutionRepository.findById(questExecutionId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 QuestExecution을 찾을 수 없습니다."));
    }

    private QuestExecution findOrCreate(Quest quest, Instant periodStart, Instant periodEnd) {
        Optional<QuestExecution> existing = questExecutionRepository
                .findByQuest_QuestIdAndPeriodStart(quest.getQuestId(), periodStart);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            return questExecutionRepository.saveAndFlush(QuestExecution.create(quest, periodStart, periodEnd));
        } catch (DataIntegrityViolationException ex) {
            return questExecutionRepository.findByQuest_QuestIdAndPeriodStart(quest.getQuestId(), periodStart)
                    .orElseThrow(() -> ex);
        }
    }

    private void validatePolicyCombination(Quest quest) {
        if (quest.getResetPolicy() == null) {
            throw new BadRequestException("반복 Quest에는 ResetPolicy가 필요합니다.");
        }

        ResetType resetType = quest.getResetPolicy().getResetType();
        boolean valid = switch (quest.getRecurrenceType()) {
            case DAILY -> resetType == ResetType.DAILY;
            case WEEKLY -> resetType == ResetType.WEEKLY;
            case CUSTOM -> resetType == ResetType.DAILY;
            case ONCE -> true;
        };
        if (!valid) {
            throw new BadRequestException("Quest의 recurrenceType과 ResetPolicy 조합이 올바르지 않습니다.");
        }
    }

    private boolean isTargetDay(Quest quest, Instant periodStart, String timezone) {
        DayOfWeek dayOfWeek = periodStart.atZone(ZoneId.of(timezone)).getDayOfWeek();
        return quest.getCustomDays().contains(dayOfWeek);
    }

    private boolean isExecutionTarget(Quest quest, Instant periodStart) {
        if (quest.getRecurrenceType() != RecurrenceType.CUSTOM) {
            return true;
        }
        return isTargetDay(quest, periodStart, quest.getResetPolicy().getTimezone());
    }
}
