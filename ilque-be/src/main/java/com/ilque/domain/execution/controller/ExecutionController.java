package com.ilque.domain.execution.controller;

import com.ilque.domain.execution.dto.QuestExecutionResponse;
import com.ilque.domain.execution.dto.CompleteExecutionRequest;
import com.ilque.domain.execution.service.ExecutionService;
import com.ilque.global.response.ApiResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/quests/{questId}/start")
    public ApiResponseEnvelope<QuestExecutionResponse> start(@PathVariable Long questId) {
        return ApiResponseEnvelope.success(
                QuestExecutionResponse.from(executionService.start(questId)));
    }

    @PostMapping("/executions/{questExecutionId}/skip")
    public ApiResponseEnvelope<QuestExecutionResponse> skip(@PathVariable Long questExecutionId) {
        return ApiResponseEnvelope.success(
                QuestExecutionResponse.from(executionService.skip(questExecutionId)));
    }

    @PostMapping("/executions/{questExecutionId}/rest-day")
    public ApiResponseEnvelope<QuestExecutionResponse> restDay(@PathVariable Long questExecutionId) {
        return ApiResponseEnvelope.success(
                QuestExecutionResponse.from(executionService.markRestDay(questExecutionId)));
    }

    @PostMapping("/executions/{questExecutionId}/complete")
    public ApiResponseEnvelope<QuestExecutionResponse> complete(
            @PathVariable Long questExecutionId,
            @RequestBody(required = false) CompleteExecutionRequest request
    ) {
        return ApiResponseEnvelope.success(
                QuestExecutionResponse.from(executionService.complete(questExecutionId, request)));
    }
}
