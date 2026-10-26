package com.ilque.domain.resetpolicy.entity;

import com.ilque.global.base.Base;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reset_policy")
public class ResetPolicy extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resetPolicyId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResetType resetType;

    @Column(nullable = false)
    private LocalTime resetTime;

    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    private DayOfWeek weekStartDay;

    @Column(nullable = false)
    private Boolean active;

    @Column
    private Integer interval;

    @Enumerated(EnumType.STRING)
    @Column
    private IntervalUnit intervalUnit;

    @Builder
    private ResetPolicy(
            Long resetPolicyId,
            String name,
            ResetType resetType,
            LocalTime resetTime,
            String timezone,
            DayOfWeek weekStartDay,
            Boolean active,
            Integer interval,
            IntervalUnit intervalUnit
    ) {
        LocalTime normalizedResetTime = resetTime == null ? LocalTime.MIDNIGHT : resetTime;
        ResetPolicyValidator.validate(name, resetType, normalizedResetTime, timezone,
                weekStartDay, active, interval, intervalUnit);
        this.resetPolicyId = resetPolicyId;
        this.name = name;
        this.resetType = resetType;
        this.resetTime = normalizedResetTime;
        this.timezone = timezone;
        this.weekStartDay = weekStartDay;
        this.active = active;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    @PrePersist
    @PreUpdate
    private void validateBeforePersist() {
        ResetPolicyValidator.validate(name, resetType, resetTime, timezone,
                weekStartDay, active, interval, intervalUnit);
    }

    public void update(String name, ResetType resetType, LocalTime resetTime, String timezone,
                       DayOfWeek weekStartDay, Boolean active, Integer interval,
                       IntervalUnit intervalUnit) {
        LocalTime normalizedResetTime = resetTime == null ? LocalTime.MIDNIGHT : resetTime;
        ResetPolicyValidator.validate(name, resetType, normalizedResetTime, timezone,
                weekStartDay, active, interval, intervalUnit);
        this.name = name;
        this.resetType = resetType;
        this.resetTime = normalizedResetTime;
        this.timezone = timezone;
        this.weekStartDay = weekStartDay;
        this.active = active;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }
}
