package com.ilque.domain.execution.entity;

import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.QuestType;
import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "quest_execution",
        uniqueConstraints = @jakarta.persistence.UniqueConstraint(
                name = "uk_quest_execution_quest_period_start",
                columnNames = {"quest_id", "period_start"}
        )
)
public class QuestExecution extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questExecutionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @Column(nullable = false)
    private Long originalQuestId;

    @Column(nullable = false, length = 100)
    private String questTitleSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType questTypeSnapshot;

    @Column(nullable = false)
    private Instant periodStart;

    @Column
    private Instant periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column
    private Instant startedAt;

    @Column
    private Instant completedAt;

    @Column
    private Integer targetCount;

    @Column
    private Integer actualCount;

    @Column
    private Integer targetDuration;

    @Column
    private Integer actualDuration;

    @Column(nullable = false)
    private Integer baseExp;

    @Column(nullable = false)
    private Integer expPerMinute;

    @Column
    private Integer earnedExp;

    private QuestExecution(
            Quest quest,
            Instant periodStart,
            Instant periodEnd
    ) {
        if (quest == null) {
            throw new BadRequestException("Quest는 필수입니다.");
        }
        if (periodStart == null) {
            throw new BadRequestException("periodStart은 필수입니다.");
        }
        if (quest.getQuestId() == null) {
            throw new BadRequestException("저장된 Quest만 Execution을 생성할 수 있습니다.");
        }

        this.quest = quest;
        this.originalQuestId = quest.getQuestId();
        this.questTitleSnapshot = quest.getTitle();
        this.questTypeSnapshot = quest.getQuestType();
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = ExecutionStatus.PENDING;
        this.targetCount = quest.getTargetCount();
        this.targetDuration = quest.getTargetTimeMinutes();
        this.baseExp = quest.getBaseExp();
        this.expPerMinute = quest.getExpPerMinute();
        validateState();
    }

    public static QuestExecution create(Quest quest, Instant periodStart, Instant periodEnd) {
        return new QuestExecution(quest, periodStart, periodEnd);
    }

    public void start(Instant startedAt) {
        requireStatus(ExecutionStatus.PENDING, "PENDING 상태에서만 시작할 수 있습니다.");
        if (startedAt == null) {
            throw new BadRequestException("startedAt은 필수입니다.");
        }
        this.startedAt = startedAt;
        this.status = ExecutionStatus.IN_PROGRESS;
    }

    public void skip() {
        if (status != ExecutionStatus.PENDING && status != ExecutionStatus.IN_PROGRESS) {
            throw new BadRequestException("PENDING 또는 IN_PROGRESS 상태에서만 Skip할 수 있습니다.");
        }
        this.status = ExecutionStatus.SKIPPED;
    }

    public void markRestDay() {
        requireStatus(ExecutionStatus.PENDING, "PENDING 상태에서만 REST_DAY로 지정할 수 있습니다.");
        this.status = ExecutionStatus.REST_DAY;
    }

    public void complete(
            Integer actualCount,
            Integer actualDuration,
            Integer earnedExp,
            Instant completedAt
    ) {
        if (status != ExecutionStatus.PENDING && status != ExecutionStatus.IN_PROGRESS) {
            throw new BadRequestException("PENDING 또는 IN_PROGRESS 상태에서만 완료할 수 있습니다.");
        }
        if (completedAt == null) {
            throw new BadRequestException("completedAt은 필수입니다.");
        }
        this.actualCount = actualCount;
        this.actualDuration = actualDuration;
        this.earnedExp = earnedExp;
        this.completedAt = completedAt;
        this.status = ExecutionStatus.COMPLETED;
    }

    public void markMissed() {
        requireStatus(ExecutionStatus.PENDING, "PENDING 상태에서만 MISSED로 지정할 수 있습니다.");
        this.status = ExecutionStatus.MISSED;
    }

    @PrePersist
    private void validateBeforePersist() {
        validateState();
    }

    private void validateState() {
        if (originalQuestId == null) {
            throw new BadRequestException("originalQuestId는 필수입니다.");
        }
        if (questTitleSnapshot == null || questTitleSnapshot.isBlank()) {
            throw new BadRequestException("questTitleSnapshot은 필수입니다.");
        }
        if (questTypeSnapshot == null) {
            throw new BadRequestException("questTypeSnapshot은 필수입니다.");
        }
        if (periodStart == null) {
            throw new BadRequestException("periodStart은 필수입니다.");
        }
        if (status == null) {
            throw new BadRequestException("status는 필수입니다.");
        }
        if (baseExp == null || baseExp < 0) {
            throw new BadRequestException("baseExp는 0 이상이어야 합니다.");
        }
        if (expPerMinute == null || expPerMinute < 0) {
            throw new BadRequestException("expPerMinute은 0 이상이어야 합니다.");
        }
        if (targetCount != null && targetCount < 1) {
            throw new BadRequestException("targetCount는 1 이상이어야 합니다.");
        }
        if (targetDuration != null && targetDuration < 1) {
            throw new BadRequestException("targetDuration은 1 이상이어야 합니다.");
        }
        if (actualCount != null && actualCount < 0) {
            throw new BadRequestException("actualCount는 0 이상이어야 합니다.");
        }
        if (actualDuration != null && actualDuration < 0) {
            throw new BadRequestException("actualDuration은 0 이상이어야 합니다.");
        }
        if (earnedExp != null && earnedExp < 0) {
            throw new BadRequestException("earnedExp는 0 이상이어야 합니다.");
        }
    }

    private void requireStatus(ExecutionStatus expected, String message) {
        if (status != expected) {
            throw new BadRequestException(message);
        }
    }
}
