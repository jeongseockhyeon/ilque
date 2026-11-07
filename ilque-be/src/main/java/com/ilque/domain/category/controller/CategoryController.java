package com.ilque.domain.category.controller;

import com.ilque.domain.category.dto.CategoryRequest;
import com.ilque.domain.category.dto.CategoryResponse;

import com.ilque.domain.category.serveice.CategoryService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    public ResponseEntity<ApiResponseEnvelope<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request){
        return ResponseEntity.ok(ApiResponseEnvelope.success(CategoryResponse.from(service.create(request))));
    }
}
