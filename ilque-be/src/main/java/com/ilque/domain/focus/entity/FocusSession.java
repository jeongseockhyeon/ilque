package com.ilque.domain.focus.entity;

import com.ilque.domain.execution.entity.QuestExecution;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "focus_session")
public class FocusSession extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long focusSessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_execution_id", nullable = false)
    private QuestExecution questExecution;

    @Column(nullable = false)
    private Instant startedAt;

    @Column
    private Instant endedAt;

    @Column
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FocusSessionStatus status;

    private FocusSession(QuestExecution questExecution, Instant startedAt) {
        if (questExecution == null || startedAt == null) {
            throw new BadRequestException("QuestExecution과 startedAt은 필수입니다.");
        }
        this.questExecution = questExecution;
        this.startedAt = startedAt;
        this.status = FocusSessionStatus.ACTIVE;
    }

    public static FocusSession start(QuestExecution questExecution, Instant startedAt) {
        return new FocusSession(questExecution, startedAt);
    }

    public void stop(Instant endedAt) {
        if (status != FocusSessionStatus.ACTIVE) {
            throw new BadRequestException("ACTIVE 상태의 FocusSession만 종료할 수 있습니다.");
        }
        if (endedAt == null || endedAt.isBefore(startedAt)) {
            throw new BadRequestException("endedAt은 startedAt 이후여야 합니다.");
        }
        this.endedAt = endedAt;
        this.durationMinutes = Math.toIntExact(Duration.between(startedAt, endedAt).toMinutes());
        this.status = FocusSessionStatus.STOPPED;
    }
}
