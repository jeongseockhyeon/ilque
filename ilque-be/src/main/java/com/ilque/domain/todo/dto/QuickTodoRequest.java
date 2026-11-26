package com.ilque.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuickTodoRequest(
        @NotBlank @Size(max = 100) String title
) {
}
