package com.ilque.domain.execution.controller;

import com.ilque.domain.execution.dto.QuestExecutionResponse;
import com.ilque.domain.execution.dto.CompleteExecutionRequest;
import com.ilque.domain.execution.service.ExecutionService;
import com.ilque.domain.focus.dto.FocusSessionResponse;
import com.ilque.domain.focus.service.FocusService;
import com.ilque.global.response.ApiResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;
    private final FocusService focusService;

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

    @GetMapping("/executions/history")
    public ApiResponseEnvelope<List<QuestExecutionResponse>> history(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return ApiResponseEnvelope.success(
                executionService.findHistory(from, to).stream()
                        .map(QuestExecutionResponse::from)
                        .toList());
    }

    @PostMapping("/quests/{questId}/focus/start")
    public ApiResponseEnvelope<FocusSessionResponse> startFocus(@PathVariable Long questId) {
        return ApiResponseEnvelope.success(
                FocusSessionResponse.from(focusService.start(questId)));
    }

    @PostMapping("/focus/{focusSessionId}/stop")
    public ApiResponseEnvelope<FocusSessionResponse> stopFocus(@PathVariable Long focusSessionId) {
        return ApiResponseEnvelope.success(
                FocusSessionResponse.from(focusService.stop(focusSessionId)));
    }
}
