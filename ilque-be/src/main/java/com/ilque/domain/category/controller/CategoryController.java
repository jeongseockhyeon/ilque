package com.ilque.domain.category.controller;

import com.ilque.domain.category.dto.CategoryRequest;
import com.ilque.domain.category.dto.CategoryResponse;
import com.ilque.domain.category.entity.Category;
import com.ilque.domain.category.service.CategoryService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @GetMapping("/{categoryId}")
    public ApiResponseEnvelope<CategoryResponse> findById(@PathVariable Long categoryId){
        return ApiResponseEnvelope.success(CategoryResponse.from(service.findById(categoryId)));
    }
    @PostMapping
    public ResponseEntity<ApiResponseEnvelope<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request){
        return ResponseEntity.ok(ApiResponseEnvelope.success(CategoryResponse.from(service.create(request))));
    }

}
