package com.ilque.domain.execution.service;

import com.ilque.domain.execution.entity.QuestExecution;
import com.ilque.domain.execution.repository.QuestExecutionRepository;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.RecurrenceType;
import com.ilque.domain.quest.repository.QuestRepository;
import com.ilque.domain.resetpolicy.calculator.ResetBoundaryCalculator;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final QuestRepository questRepository;
    private final QuestExecutionRepository questExecutionRepository;
    private final ResetBoundaryCalculator resetBoundaryCalculator;

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
        ResetBoundaryCalculator.PeriodBoundary period = resetBoundaryCalculator.calculateCurrentPeriod(
                quest.getCreatedAt(), now, quest.getResetPolicy());

        if (quest.getRecurrenceType() == RecurrenceType.CUSTOM
                && !isTargetDay(quest, period.start(), quest.getResetPolicy().getTimezone())) {
            return null;
        }

        return findOrCreate(quest, period.start(), period.end());
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
}
