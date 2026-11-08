package com.ilque.domain.category.dto;

import lombok.Getter;

@Getter
public class CategoryPatchRequest {
    private String name;
    private String description;
    private Boolean active;

}
