package com.ilque.domain.quest.dto;

import com.ilque.domain.quest.entity.Difficulty;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.domain.quest.entity.RecurrenceType;
import java.time.DayOfWeek;
import java.util.Set;

public record QuestResponse(Long questId,
                            String title,
                            String description,
                            QuestType questType,
                            RecurrenceType recurrenceType,
                            Difficulty difficulty,
                            Long categoryId,
                            Long resetPolicyId,
                            Set<DayOfWeek> customDays,
                            Integer targetCount,
                            Integer targetTimeMinutes,
                            Integer baseExp,
                            Integer expPerMinute,
                            Boolean active) {
    public static QuestResponse from(Quest quest) {
        Set<DayOfWeek> customDays = quest.getCustomDays() == null
                ? Set.of()
                : Set.copyOf(quest.getCustomDays());

        return new QuestResponse(
                quest.getQuestId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getQuestType(),
                quest.getRecurrenceType(),
                quest.getDifficulty(),
                quest.getCategory() == null ? null : quest.getCategory().getCategoryId(),
                quest.getResetPolicy() == null ? null : quest.getResetPolicy().getResetPolicyId(),
                customDays,
                quest.getTargetCount(),
                quest.getTargetTimeMinutes(),
                quest.getBaseExp(),
                quest.getExpPerMinute(),
                quest.getActive());
    }
}
