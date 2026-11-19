package com.ilque.domain.today.service;

import com.ilque.domain.execution.dto.QuestExecutionResponse;
import com.ilque.domain.execution.entity.QuestExecution;
import com.ilque.domain.execution.service.ExecutionService;
import com.ilque.domain.quest.repository.QuestRepository;
import com.ilque.domain.today.dto.TodayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TodayService {

    private final QuestRepository questRepository;
    private final ExecutionService executionService;

    @Transactional
    public TodayResponse findToday(Instant now) {
        return new TodayResponse(
                questRepository.findAllByActiveTrue().stream()
                        .sorted((left, right) -> left.getQuestId().compareTo(right.getQuestId()))
                        .map(quest -> executionService.ensureCurrentExecution(quest, now))
                        .filter(execution -> execution != null)
                        .map(QuestExecutionResponse::from)
                        .toList());
    }
}
