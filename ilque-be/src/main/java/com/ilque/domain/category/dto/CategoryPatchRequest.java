package com.ilque.domain.category.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CategoryPatchRequest {
    @Size(max = 50)
    private String name;
    private String description;
    private Boolean active;

    private boolean nameProvided;
    private boolean descriptionProvided;
    private boolean activeProvided;

    @JsonSetter(value = "name", nulls = Nulls.SET)
    public void setName(String value) { nameProvided = true; name = value;}

    @JsonSetter(value = "description", nulls = Nulls.SET)
    public void setDescription(String value) { descriptionProvided = true; description = value;}

    @JsonSetter(value = "active", nulls = Nulls.SET)
    public void setActive(Boolean value) { activeProvided = true; active = value;}


}
