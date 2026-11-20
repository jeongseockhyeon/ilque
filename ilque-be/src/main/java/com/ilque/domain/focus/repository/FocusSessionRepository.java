package com.ilque.domain.focus.repository;

import com.ilque.domain.focus.entity.FocusSession;
import com.ilque.domain.focus.entity.FocusSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    boolean existsByQuestExecution_QuestExecutionIdAndStatus(
            Long questExecutionId,
            com.ilque.domain.focus.entity.FocusSessionStatus status
    );

    boolean existsByQuestExecution_QuestExecutionId(Long questExecutionId);

    @Query("""
            select coalesce(sum(session.durationMinutes), 0)
              from FocusSession session
             where session.questExecution.quest.user.userId = :userId
               and session.status = :status
            """)
    long sumDurationMinutesByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") FocusSessionStatus status
    );
}
