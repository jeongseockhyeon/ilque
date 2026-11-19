package com.ilque.domain.focus.dto;

import com.ilque.domain.focus.entity.FocusSession;
import com.ilque.domain.focus.entity.FocusSessionStatus;

import java.time.Instant;

public record FocusSessionResponse(
        Long focusSessionId,
        Long questExecutionId,
        Instant startedAt,
        Instant endedAt,
        Integer durationMinutes,
        FocusSessionStatus status
) {
    public static FocusSessionResponse from(FocusSession session) {
        return new FocusSessionResponse(
                session.getFocusSessionId(),
                session.getQuestExecution().getQuestExecutionId(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getDurationMinutes(),
                session.getStatus());
    }
}
