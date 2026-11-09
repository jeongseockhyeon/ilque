package com.ilque.domain.category.entity;

import com.ilque.global.base.Base;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category")
public class Category extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public void update(
            String name,
            String description,
            Boolean active
    ){
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public void softDelete(){
        this.active = false;
    }
}
