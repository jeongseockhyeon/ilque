package com.ilque.domain.focus.repository;

import com.ilque.domain.focus.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    boolean existsByQuestExecution_QuestExecutionIdAndStatus(
            Long questExecutionId,
            com.ilque.domain.focus.entity.FocusSessionStatus status
    );

    boolean existsByQuestExecution_QuestExecutionId(Long questExecutionId);
}
