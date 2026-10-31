package com.ilque.domain.quest.entity;

import com.ilque.domain.categroy.entity.Category;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.user.entity.User;
import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quest")
public class Quest extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType questType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceType recurrenceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @ElementCollection
    @CollectionTable(
            name = "quest_custom_day",
            joinColumns = @JoinColumn(name = "quest_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"quest_id", "day_of_week"})
    )
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> customDays = new HashSet<>();

    @Column
    private Integer targetCount;

    @Column
    private Integer targetTimeMinutes;

    @Column(nullable = false)
    private Integer baseExp;

    @Column(nullable = false)
    private Integer expPerMinute;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reset_policy_id")
    private ResetPolicy resetPolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Quest(
            String title,
            String description,
            QuestType questType,
            RecurrenceType recurrenceType,
            Set<DayOfWeek> customDays,
            Difficulty difficulty,
            Integer targetCount,
            Integer targetTimeMinutes,
            Category category,
            ResetPolicy resetPolicy,
            User user
    ) {
        this.title = normalizeTitle(title);
        this.description = normalizeDescription(description);
        this.questType = questType;
        this.recurrenceType = recurrenceType;
        this.customDays = copyDays(customDays);
        this.difficulty = requireDifficulty(difficulty);
        this.targetCount = targetCount;
        this.targetTimeMinutes = targetTimeMinutes;
        this.baseExp = difficulty.getBaseExp();
        this.expPerMinute = difficulty.getExpPerMinute();
        this.active = true;
        this.category = category;
        this.resetPolicy = resetPolicy;
        this.user = user;
        validateState();
    }

    public static Quest create(
            String title,
            String description,
            QuestType questType,
            RecurrenceType recurrenceType,
            Set<DayOfWeek> customDays,
            Integer targetCount,
            Integer targetTimeMinutes,
            Category category,
            ResetPolicy resetPolicy,
            User user
    ) {
        return new Quest(title, description, questType, recurrenceType, customDays,
                Difficulty.NORMAL, targetCount, targetTimeMinutes, category, resetPolicy, user);
    }

    public static Quest create(
            String title,
            String description,
            QuestType questType,
            RecurrenceType recurrenceType,
            Set<DayOfWeek> customDays,
            Difficulty difficulty,
            Integer targetCount,
            Integer targetTimeMinutes,
            Category category,
            ResetPolicy resetPolicy,
            User user
    ) {
        return new Quest(title, description, questType, recurrenceType, customDays,
                requireDifficulty(difficulty), targetCount, targetTimeMinutes, category, resetPolicy, user);
    }

    public Set<DayOfWeek> getCustomDays() {
        return Collections.unmodifiableSet(customDays);
    }

    public void updateDefinition(
            String title,
            String description,
            QuestType questType,
            RecurrenceType recurrenceType,
            Set<DayOfWeek> customDays,
            Difficulty difficulty,
            Integer targetCount,
            Integer targetTimeMinutes,
            Category category,
            ResetPolicy resetPolicy
    ) {
        String normalizedTitle = normalizeTitle(title);
        String normalizedDescription = normalizeDescription(description);
        Set<DayOfWeek> copiedDays = copyDays(customDays);
        Difficulty requiredDifficulty = requireDifficulty(difficulty);
        validateState(questType, recurrenceType, copiedDays, requiredDifficulty,
                targetCount, targetTimeMinutes, resetPolicy);

        this.title = normalizedTitle;
        this.description = normalizedDescription;
        this.questType = questType;
        this.recurrenceType = recurrenceType;
        this.customDays = copiedDays;
        this.difficulty = requiredDifficulty;
        this.targetCount = targetCount;
        this.targetTimeMinutes = targetTimeMinutes;
        this.category = category;
        this.resetPolicy = resetPolicy;
        validateState();
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @PrePersist
    @PreUpdate
    private void validateBeforePersist() {
        validateState();
    }

    private void validateState() {
        validateState(questType, recurrenceType, customDays, difficulty,
                targetCount, targetTimeMinutes, resetPolicy);
    }

    private static void validateState(
            QuestType questType,
            RecurrenceType recurrenceType,
            Set<DayOfWeek> customDays,
            Difficulty difficulty,
            Integer targetCount,
            Integer targetTimeMinutes,
            ResetPolicy resetPolicy
    ) {
        if (questType == null) {
            throw new BadRequestException("questType은 필수입니다.");
        }
        if (recurrenceType == null) {
            throw new BadRequestException("recurrenceType은 필수입니다.");
        }
        if (difficulty == null) {
            throw new BadRequestException("difficulty는 필수입니다.");
        }

        switch (questType) {
            case CHECK -> {
                requirePositive(targetCount, "CHECK targetCount");
                if (targetTimeMinutes != null) {
                    throw new BadRequestException("CHECK Quest의 targetTimeMinutes는 null이어야 합니다.");
                }
            }
            case TIME_BASED -> {
                requirePositive(targetTimeMinutes, "TIME_BASED targetTimeMinutes");
                if (targetCount != null) {
                    throw new BadRequestException("TIME_BASED Quest의 targetCount는 null이어야 합니다.");
                }
            }
        }

        if (recurrenceType == RecurrenceType.CUSTOM) {
            if (customDays == null || customDays.isEmpty()) {
                throw new BadRequestException("CUSTOM Quest는 customDays가 하나 이상 필요합니다.");
            }
        } else if (customDays != null && !customDays.isEmpty()) {
            throw new BadRequestException("CUSTOM이 아닌 Quest에서는 customDays를 사용할 수 없습니다.");
        }

        if (recurrenceType == RecurrenceType.ONCE) {
            if (resetPolicy != null) {
                throw new BadRequestException("ONCE Quest의 resetPolicy는 null이어야 합니다.");
            }
        } else if (resetPolicy == null) {
            throw new BadRequestException("반복 Quest는 resetPolicy가 필요합니다.");
        }
    }

    private static Set<DayOfWeek> copyDays(Set<DayOfWeek> days) {
        return days == null ? new HashSet<>() : new HashSet<>(days);
    }

    private static Difficulty requireDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            throw new BadRequestException("difficulty는 null일 수 없습니다.");
        }
        return difficulty;
    }

    private static String normalizeTitle(String title) {
        String normalized = title == null ? null : title.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new BadRequestException("title은 필수이며 공백일 수 없습니다.");
        }
        if (normalized.length() > 100) {
            throw new BadRequestException("title은 100자 이하여야 합니다.");
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 1000) {
            throw new BadRequestException("description은 1,000자 이하여야 합니다.");
        }
        return normalized;
    }

    private static void requirePositive(Integer value, String fieldName) {
        if (value == null || value < 1) {
            throw new BadRequestException(fieldName + "은 1 이상이어야 합니다.");
        }
    }

}
