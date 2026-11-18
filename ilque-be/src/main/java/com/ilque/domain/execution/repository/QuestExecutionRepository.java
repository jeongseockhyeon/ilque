package com.ilque.domain.execution.repository;

import com.ilque.domain.execution.entity.QuestExecution;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuestExecutionRepository extends JpaRepository<QuestExecution, Long> {

    Optional<QuestExecution> findByQuest_QuestIdAndPeriodStart(Long questId, Instant periodStart);

    boolean existsByQuest_QuestIdAndPeriodStart(Long questId, Instant periodStart);

    List<QuestExecution> findAllByPeriodStartGreaterThanEqualAndPeriodStartLessThanOrderByPeriodStartDescCompletedAtDesc(
            Instant from,
            Instant to
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update QuestExecution execution
               set execution.quest = null
             where execution.quest.questId = :questId
            """)
    int detachQuestReferences(@Param("questId") Long questId);
}
