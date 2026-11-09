package com.ilque.domain.category.dto;

import com.ilque.domain.category.entity.Category;

public record CategoryResponse(
        Long categoryId,
        String name,
        String description,
        Boolean active
) {
    public static CategoryResponse from(Category category){
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }
}
