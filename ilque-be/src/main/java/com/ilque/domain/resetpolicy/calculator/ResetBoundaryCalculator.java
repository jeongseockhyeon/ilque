package com.ilque.domain.resetpolicy.calculator;

import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetType;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ResetBoundaryCalculator {

    public record PeriodBoundary(Instant start, Instant end) {
    }

    public Instant calculateFirstBoundary(Instant creationInstant, ResetPolicy policy) {
        if (creationInstant == null || policy == null) {
            throw new IllegalArgumentException("생성 시각과 정책은 필수입니다.");
        }
        ZoneId zoneId = ZoneId.of(policy.getTimezone());
        LocalDate creationDate = creationInstant.atZone(zoneId).toLocalDate();
        LocalDate boundaryDate;

        if (policy.getResetType() == ResetType.DAILY) {
            return firstDailyBoundary(creationInstant, creationDate, zoneId, policy);
        } else if (policy.getResetType() == ResetType.WEEKLY) {
            return firstWeeklyBoundary(creationInstant, creationDate, zoneId, policy);
        } else if (policy.getIntervalUnit() == IntervalUnit.DAY) {
            boundaryDate = creationDate.plusDays(policy.getInterval());
        } else if (policy.getIntervalUnit() == IntervalUnit.WEEK) {
            LocalDate weekStart = startOfWeek(creationDate, policy.getWeekStartDay());
            boundaryDate = weekStart.plusWeeks(policy.getInterval());
        } else {
            throw new IllegalArgumentException("CUSTOM intervalUnit은 필수입니다.");
        }

        // LocalDateTime.atZone은 gap을 다음 유효 시각으로 이동하고,
        // overlap에서는 더 이른 오프셋을 선택한다.
        return LocalDateTime.of(boundaryDate, policy.getResetTime())
                .atZone(zoneId)
                .toInstant();
    }

    private Instant firstDailyBoundary(Instant creationInstant, LocalDate creationDate,
                                       ZoneId zoneId, ResetPolicy policy) {
        Instant candidate = atZone(creationDate, zoneId, policy);
        if (candidate.isBefore(creationInstant)) {
            candidate = atZone(creationDate.plusDays(1), zoneId, policy);
        }
        return candidate;
    }

    private Instant firstWeeklyBoundary(Instant creationInstant, LocalDate creationDate,
                                        ZoneId zoneId, ResetPolicy policy) {
        LocalDate weekStart = startOfWeek(creationDate, policy.getWeekStartDay());
        Instant candidate = atZone(weekStart, zoneId, policy);
        if (candidate.isBefore(creationInstant)) {
            candidate = atZone(weekStart.plusWeeks(1), zoneId, policy);
        }
        return candidate;
    }

    private Instant atZone(LocalDate date, ZoneId zoneId, ResetPolicy policy) {
        return LocalDateTime.of(date, policy.getResetTime()).atZone(zoneId).toInstant();
    }

    public Instant calculateFirstBoundary(Instant creationInstant, ZoneId timezone,
                                          ResetType resetType, java.time.LocalTime resetTime,
                                          DayOfWeek weekStartDay, Integer interval,
                                          IntervalUnit intervalUnit) {
        ResetPolicy policy = ResetPolicy.builder()
                .name("calculation")
                .resetType(resetType)
                .resetTime(resetTime)
                .timezone(timezone == null ? null : timezone.getId())
                .weekStartDay(weekStartDay)
                .active(true)
                .interval(interval)
                .intervalUnit(intervalUnit)
                .build();
        return calculateFirstBoundary(creationInstant, policy);
    }

    public PeriodBoundary calculateCurrentPeriod(Instant creationInstant, Instant referenceInstant,
                                                 ResetPolicy policy) {
        if (creationInstant == null || referenceInstant == null || policy == null) {
            throw new IllegalArgumentException("생성 시각, 기준 시각과 정책은 필수입니다.");
        }
        if (referenceInstant.isBefore(creationInstant)) {
            throw new IllegalArgumentException("기준 시각은 생성 시각보다 이전일 수 없습니다.");
        }

        ZoneId zoneId = ZoneId.of(policy.getTimezone());
        LocalDate referenceDate = referenceInstant.atZone(zoneId).toLocalDate();
        LocalDate startDate;
        LocalDate endDate;

        if (policy.getResetType() == ResetType.DAILY) {
            Instant candidate = atZone(referenceDate, zoneId, policy);
            startDate = candidate.isAfter(referenceInstant)
                    ? referenceDate.minusDays(1)
                    : referenceDate;
            endDate = startDate.plusDays(1);
        } else if (policy.getResetType() == ResetType.WEEKLY) {
            LocalDate weekStart = startOfWeek(referenceDate, policy.getWeekStartDay());
            Instant candidate = atZone(weekStart, zoneId, policy);
            startDate = candidate.isAfter(referenceInstant)
                    ? weekStart.minusWeeks(1)
                    : weekStart;
            endDate = startDate.plusWeeks(1);
        } else {
            throw new IllegalArgumentException("QuestExecution에서는 CUSTOM ResetPolicy를 사용할 수 없습니다.");
        }

        Instant periodStart = atZone(startDate, zoneId, policy);
        if (periodStart.isBefore(creationInstant)) {
            periodStart = creationInstant;
        }
        Instant periodEnd = atZone(endDate, zoneId, policy);
        return new PeriodBoundary(periodStart, periodEnd);
    }

    private LocalDate startOfWeek(LocalDate date, DayOfWeek weekStartDay) {
        if (weekStartDay == null) {
            throw new IllegalArgumentException("weekStartDay는 필수입니다.");
        }
        int daysSinceStart = (date.getDayOfWeek().getValue() - weekStartDay.getValue() + 7) % 7;
        return date.minusDays(daysSinceStart);
    }
}
