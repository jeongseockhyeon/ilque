package com.ilque.domain.execution.dto;

public record CompleteExecutionRequest(
        Integer actualCount,
        Integer actualDuration,
        Boolean partialCompletion
) {
}
