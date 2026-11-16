package com.ilque.domain.execution.repository;

import com.ilque.domain.execution.entity.QuestExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface QuestExecutionRepository extends JpaRepository<QuestExecution, Long> {

    Optional<QuestExecution> findByQuest_QuestIdAndPeriodStart(Long questId, Instant periodStart);

    boolean existsByQuest_QuestIdAndPeriodStart(Long questId, Instant periodStart);
}
