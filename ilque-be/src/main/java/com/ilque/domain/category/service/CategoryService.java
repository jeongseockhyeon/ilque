package com.ilque.domain.category.service;

import com.ilque.domain.category.dto.CategoryRequest;
import com.ilque.domain.category.entity.Category;
import com.ilque.domain.category.repository.CategoryRepository;
import com.ilque.global.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    @Transactional
    public Category create(CategoryRequest request){
        validate(request);
        return repository.save(toEntity(request));
    }

    private Category toEntity(CategoryRequest request){
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    private void validate(CategoryRequest request){
        String name = request.getName();
        if (name == null || name.isBlank()) throw new BadRequestException("카테고리 이름은 필수입니다.");
    }
}
