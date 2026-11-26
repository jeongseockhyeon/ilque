package com.ilque.domain.todo.controller;

import com.ilque.domain.todo.dto.QuickTodoRequest;
import com.ilque.domain.todo.dto.QuickTodoResponse;
import com.ilque.domain.todo.service.QuickTodoService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/quick-todos")
@RequiredArgsConstructor
public class QuickTodoController {

    private final QuickTodoService quickTodoService;

    @GetMapping
    public ApiResponseEnvelope<List<QuickTodoResponse>> findDashboardTodos(@RequestParam LocalDate date) {
        return ApiResponseEnvelope.success(quickTodoService.findDashboardTodos(date));
    }

    @PostMapping
    public ApiResponseEnvelope<QuickTodoResponse> create(@Valid @RequestBody QuickTodoRequest request) {
        return ApiResponseEnvelope.success(quickTodoService.create(request));
    }

    @PatchMapping("/{quickTodoId}/complete")
    public ApiResponseEnvelope<QuickTodoResponse> complete(@PathVariable Long quickTodoId) {
        return ApiResponseEnvelope.success(quickTodoService.complete(quickTodoId));
    }
}
