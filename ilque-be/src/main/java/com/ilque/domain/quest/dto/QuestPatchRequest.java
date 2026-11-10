package com.ilque.domain.quest.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.ilque.domain.quest.entity.Difficulty;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.domain.quest.entity.RecurrenceType;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.Set;

/** Quest 정의를 부분 수정하기 위한 요청 DTO입니다. */
@Getter
public class QuestPatchRequest {
    @Size(max = 100)
    private String title;
    @Size(max = 1000)
    private String description;
    private QuestType questType;
    private RecurrenceType recurrenceType;
    private Difficulty difficulty;
    private Long categoryId;
    private Long resetPolicyId;
    private Set<DayOfWeek> customDays;
    private Integer targetCount;
    private Integer targetTimeMinutes;
    private Boolean active;

    private boolean titleProvided;
    private boolean descriptionProvided;
    private boolean questTypeProvided;
    private boolean recurrenceTypeProvided;
    private boolean difficultyProvided;
    private boolean categoryIdProvided;
    private boolean resetPolicyIdProvided;
    private boolean customDaysProvided;
    private boolean targetCountProvided;
    private boolean targetTimeMinutesProvided;
    private boolean activeProvided;

    @JsonSetter(value = "title", nulls = Nulls.SET)
    public void setTitle(String value) {
        titleProvided = true;
        title = value;
    }

    @JsonSetter(value = "description", nulls = Nulls.SET)
    public void setDescription(String value) {
        descriptionProvided = true;
        description = value;
    }

    @JsonSetter(value = "questType", nulls = Nulls.SET)
    public void setQuestType(QuestType value) {
        questTypeProvided = true;
        questType = value;
    }

    @JsonSetter(value = "recurrenceType", nulls = Nulls.SET)
    public void setRecurrenceType(RecurrenceType value) {
        recurrenceTypeProvided = true;
        recurrenceType = value;
    }

    @JsonSetter(value = "difficulty", nulls = Nulls.SET)
    public void setDifficulty(Difficulty value) {
        difficultyProvided = true;
        difficulty = value;
    }

    @JsonSetter(value = "categoryId", nulls = Nulls.SET)
    public void setCategoryId(Long value) {
        categoryIdProvided = true;
        categoryId = value;
    }

    @JsonSetter(value = "resetPolicyId", nulls = Nulls.SET)
    public void setResetPolicyId(Long value) {
        resetPolicyIdProvided = true;
        resetPolicyId = value;
    }

    @JsonSetter(value = "customDays", nulls = Nulls.SET)
    public void setCustomDays(Set<DayOfWeek> value) {
        customDaysProvided = true;
        customDays = value;
    }

    @JsonSetter(value = "targetCount", nulls = Nulls.SET)
    public void setTargetCount(Integer value) {
        targetCountProvided = true;
        targetCount = value;
    }

    @JsonSetter(value = "targetTimeMinutes", nulls = Nulls.SET)
    public void setTargetTimeMinutes(Integer value) {
        targetTimeMinutesProvided = true;
        targetTimeMinutes = value;
    }

    @JsonSetter(value = "active", nulls = Nulls.SET)
    public void setActive(Boolean value) {
        activeProvided = true;
        active = value;
    }
}
