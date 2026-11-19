package com.ilque.domain.today.dto;

import com.ilque.domain.execution.dto.QuestExecutionResponse;

import java.util.List;

public record TodayResponse(
        List<QuestExecutionResponse> executions
) {
}
