package com.ilque.domain.execution.dto;

import com.ilque.domain.execution.entity.ExecutionStatus;
import com.ilque.domain.execution.entity.QuestExecution;
import com.ilque.domain.quest.entity.QuestType;

import java.time.Instant;

public record QuestExecutionResponse(
        Long questExecutionId,
        Long questId,
        Long originalQuestId,
        String questTitleSnapshot,
        QuestType questTypeSnapshot,
        Instant periodStart,
        Instant periodEnd,
        ExecutionStatus status,
        Instant startedAt,
        Instant completedAt,
        Integer targetCount,
        Integer actualCount,
        Integer targetDuration,
        Integer actualDuration,
        Integer baseExp,
        Integer expPerMinute,
        Integer completionBonus,
        Integer earnedExp
) {
    public static QuestExecutionResponse from(QuestExecution execution) {
        return new QuestExecutionResponse(
                execution.getQuestExecutionId(),
                execution.getQuest() == null ? null : execution.getQuest().getQuestId(),
                execution.getOriginalQuestId(),
                execution.getQuestTitleSnapshot(),
                execution.getQuestTypeSnapshot(),
                execution.getPeriodStart(),
                execution.getPeriodEnd(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getTargetCount(),
                execution.getActualCount(),
                execution.getTargetDuration(),
                execution.getActualDuration(),
                execution.getBaseExp(),
                execution.getExpPerMinute(),
                execution.getCompletionBonus(),
                execution.getEarnedExp());
    }
}
