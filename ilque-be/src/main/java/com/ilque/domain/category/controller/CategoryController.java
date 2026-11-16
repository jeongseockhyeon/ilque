package com.ilque.domain.category.controller;

import com.ilque.domain.category.dto.CategoryPatchRequest;
import com.ilque.domain.category.dto.CategoryRequest;
import com.ilque.domain.category.dto.CategoryResponse;
import com.ilque.domain.category.service.CategoryService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @GetMapping("/{categoryId}")
    public ApiResponseEnvelope<CategoryResponse> findById(@PathVariable Long categoryId){
        return ApiResponseEnvelope.success(CategoryResponse.from(service.findById(categoryId)));
    }

    @GetMapping
    public ApiResponseEnvelope<List<CategoryResponse>> findAll(){
        return ApiResponseEnvelope.success(service.findAll().stream().map(CategoryResponse::from).toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponseEnvelope<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request){
        return ResponseEntity.ok(ApiResponseEnvelope.success(CategoryResponse.from(service.create(request))));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponseEnvelope<CategoryResponse> softDelete(@PathVariable Long categoryId){
        return ApiResponseEnvelope.success(CategoryResponse.from(service.softDelete(categoryId)));
    }

    @PatchMapping("/{categoryId}")
    public ApiResponseEnvelope<CategoryResponse> update(@PathVariable Long categoryId,
                                                        @Valid @RequestBody CategoryPatchRequest request){
        return ApiResponseEnvelope.success(CategoryResponse.from(service.patch(categoryId, request)));
    }

}
