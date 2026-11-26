package com.ilque.domain.todo.dto;

import com.ilque.domain.todo.entity.QuickTodo;

import java.time.Instant;

public record QuickTodoResponse(
        Long quickTodoId,
        String title,
        Instant createdAt,
        Instant completedAt,
        Boolean active
) {
    public static QuickTodoResponse from(QuickTodo todo) {
        return new QuickTodoResponse(todo.getQuickTodoId(), todo.getTitle(), todo.getCreatedAt(),
                todo.getCompletedAt(), todo.getActive());
    }
}
