package com.ilque.domain.category.service;

import com.ilque.domain.category.dto.CategoryPatchRequest;
import com.ilque.domain.category.dto.CategoryRequest;
import com.ilque.domain.category.entity.Category;
import com.ilque.domain.category.repository.CategoryRepository;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public Category findById(Long id){
        return repository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public List<Category> findAll() { return repository.findAll(); }

    @Transactional
    public Category create(CategoryRequest request){
        validate(request);
        return repository.save(toEntity(request));
    }

    @Transactional
    public Category patch(Long id, CategoryPatchRequest request){
        if (request == null) {
            throw new BadRequestException("요청 본문은 필수입니다.");
        }

        Category current = findById(id);

        String name = request.isNameProvided() ? request.getName() : current.getName();
        String description = request.isDescriptionProvided() ? request.getDescription() : current.getDescription();
        Boolean active = request.isActiveProvided() ? request.getActive() : current.getActive();

        validatePatchedState(name, active);

        current.update(
                name,
                description,
                active
                );
        return current;
    }

    @Transactional
    public Category softDelete(Long id){
        Category current = findById(id);
        current.softDelete();
        return current;
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

    private void validatePatchedState(String name, Boolean active){
        if (name == null || name.isBlank()) {
            throw new BadRequestException("카테고리 이름은 필수입니다.");
        }

        if (active == null) {
            throw new BadRequestException("카테고리 활성 상태는 필수입니다.");
        }
    }
}
