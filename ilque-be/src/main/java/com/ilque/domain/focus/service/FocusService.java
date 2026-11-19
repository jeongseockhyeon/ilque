package com.ilque.domain.focus.service;

import com.ilque.domain.execution.entity.ExecutionStatus;
import com.ilque.domain.execution.entity.QuestExecution;
import com.ilque.domain.execution.repository.QuestExecutionRepository;
import com.ilque.domain.execution.service.ExecutionService;
import com.ilque.domain.focus.entity.FocusSession;
import com.ilque.domain.focus.entity.FocusSessionStatus;
import com.ilque.domain.focus.repository.FocusSessionRepository;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FocusService {

    private final ExecutionService executionService;
    private final QuestExecutionRepository questExecutionRepository;
    private final FocusSessionRepository focusSessionRepository;

    @Transactional
    public FocusSession start(Long questId) {
        QuestExecution execution = executionService.ensureCurrentExecution(questId, Instant.now());
        if (execution == null) {
            throw new BadRequestException("현재 날짜는 해당 CUSTOM Quest의 수행일이 아닙니다.");
        }
        if (execution.getQuestTypeSnapshot() != QuestType.TIME_BASED) {
            throw new BadRequestException("TIME_BASED Quest만 FocusSession을 사용할 수 있습니다.");
        }
        if (focusSessionRepository.existsByQuestExecution_QuestExecutionIdAndStatus(
                execution.getQuestExecutionId(), FocusSessionStatus.ACTIVE)) {
            throw new BadRequestException("이미 실행 중인 FocusSession이 있습니다.");
        }
        if (execution.getStatus() == ExecutionStatus.PENDING) {
            execution.start(Instant.now());
        }
        return focusSessionRepository.save(FocusSession.start(execution, Instant.now()));
    }

    @Transactional
    public FocusSession stop(Long focusSessionId) {
        FocusSession session = focusSessionRepository.findById(focusSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 FocusSession을 찾을 수 없습니다."));
        Instant endedAt = Instant.now();
        session.stop(endedAt);

        QuestExecution execution = session.getQuestExecution();
        int currentDuration = execution.getActualDuration() == null ? 0 : execution.getActualDuration();
        execution.recordProgress(null, currentDuration + session.getDurationMinutes(),
                execution.getStartedAt() == null ? session.getStartedAt() : execution.getStartedAt());
        return session;
    }
}
