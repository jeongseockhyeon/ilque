package com.ilque.domain.quest.dto;

import com.ilque.domain.quest.entity.Difficulty;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.domain.quest.entity.RecurrenceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.util.Set;

public record QuestRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 1000) String description,
        @NotNull QuestType questType,
        @NotNull RecurrenceType recurrenceType,
        @NotNull Difficulty difficulty,
        Long categoryId, Long resetPolicyId,
        @Valid QuestResetPolicyRequest resetPolicy,
        Set<DayOfWeek> customDays,
        Integer targetCount,
        Integer targetTimeMinutes)
{
}
