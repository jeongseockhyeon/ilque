package com.ilque.domain.quest.dto;

import com.ilque.domain.category.dto.CategoryResponse;
import com.ilque.domain.quest.entity.Difficulty;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.domain.quest.entity.RecurrenceType;
import com.ilque.domain.resetpolicy.dto.ResetPolicyResponse;

import java.time.DayOfWeek;
import java.util.Set;

public record QuestDetailResponse(
        Long questId,
        String title,
        String description,
        QuestType questType,
        RecurrenceType recurrenceType,
        Difficulty difficulty,
        Long categoryId,
        CategoryResponse category,
        Long resetPolicyId,
        ResetPolicyResponse resetPolicy,
        Set<DayOfWeek> customDays,
        Integer targetCount,
        Integer targetTimeMinutes,
        Integer baseExp,
        Integer expPerMinute,
        Boolean active
) {
    public static QuestDetailResponse from(Quest quest) {
        Set<DayOfWeek> customDays = quest.getCustomDays() == null
                ? Set.of()
                : Set.copyOf(quest.getCustomDays());

        return new QuestDetailResponse(
                quest.getQuestId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getQuestType(),
                quest.getRecurrenceType(),
                quest.getDifficulty(),
                quest.getCategory() == null ? null : quest.getCategory().getCategoryId(),
                quest.getCategory() == null ? null : CategoryResponse.from(quest.getCategory()),
                quest.getResetPolicy() == null ? null : quest.getResetPolicy().getResetPolicyId(),
                quest.getResetPolicy() == null ? null : ResetPolicyResponse.from(quest.getResetPolicy()),
                customDays,
                quest.getTargetCount(),
                quest.getTargetTimeMinutes(),
                quest.getBaseExp(),
                quest.getExpPerMinute(),
                quest.getActive());
    }
}
