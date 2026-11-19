package com.ilque.domain.resetpolicy.calculator;

import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class ResetBoundaryCalculatorTest {
    private final ResetBoundaryCalculator calculator = new ResetBoundaryCalculator();

    @Test
    void customDay_usesCalendarDays() {
        ResetPolicy policy = policy(IntervalUnit.DAY, 1, null, "Asia/Seoul", LocalTime.of(5, 0));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T02:00:00Z"), policy))
                .isEqualTo(instant("2026-10-27T05:00:00+09:00"));
        ResetPolicy threeDays = policy(IntervalUnit.DAY, 3, null, "Asia/Seoul", LocalTime.of(5, 0));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T14:00:00Z"), threeDays))
                .isEqualTo(instant("2026-10-29T05:00:00+09:00"));
    }

    @Test
    void customWeek_alignsToConfiguredWeekStart() {
        ResetPolicy monday = policy(IntervalUnit.WEEK, 1, DayOfWeek.MONDAY, "UTC", LocalTime.of(5, 0));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-25T23:00:00Z"), monday))
                .isEqualTo(instant("2026-10-26T05:00:00Z"));
        ResetPolicy twoWeeks = policy(IntervalUnit.WEEK, 2, DayOfWeek.SUNDAY, "UTC", LocalTime.MIDNIGHT);
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-27T12:00:00Z"), twoWeeks))
                .isEqualTo(instant("2026-11-08T00:00:00Z"));
    }

    @Test
    void timezone_changesLocalCreationDate() {
        ResetPolicy utc = policy(IntervalUnit.DAY, 1, null, "UTC", LocalTime.MIDNIGHT);
        ResetPolicy seoul = policy(IntervalUnit.DAY, 1, null, "Asia/Seoul", LocalTime.MIDNIGHT);
        Instant creation = instant("2026-01-01T15:30:00Z");
        assertThat(calculator.calculateFirstBoundary(creation, utc)).isEqualTo(instant("2026-01-02T00:00:00Z"));
        assertThat(calculator.calculateFirstBoundary(creation, seoul)).isEqualTo(instant("2026-01-03T00:00:00+09:00"));
    }

    @Test
    void dstGap_movesForwardAndOverlapChoosesEarlierOffset() {
        ResetPolicy gap = policy(IntervalUnit.DAY, 1, null, "America/New_York", LocalTime.of(2, 30));
        assertThat(calculator.calculateFirstBoundary(instant("2026-03-07T12:00:00Z"), gap))
                .isEqualTo(instant("2026-03-08T07:30:00Z"));
        ResetPolicy overlap = policy(IntervalUnit.DAY, 1, null, "America/New_York", LocalTime.of(1, 30));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-31T12:00:00Z"), overlap))
                .isEqualTo(instant("2026-11-01T05:30:00Z"));
    }

    @Test
    void dailyAndWeeklyUseFirstBoundaryAfterCreation() {
        ResetPolicy daily = policy(null, null, null, "UTC", LocalTime.of(5, 0), com.ilque.domain.resetpolicy.entity.ResetType.DAILY);
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T04:00:00Z"), daily)).isEqualTo(instant("2026-10-26T05:00:00Z"));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T06:00:00Z"), daily)).isEqualTo(instant("2026-10-27T05:00:00Z"));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T05:00:00Z"), daily)).isEqualTo(instant("2026-10-26T05:00:00Z"));

        ResetPolicy weekly = policy(null, null, DayOfWeek.MONDAY, "UTC", LocalTime.of(5, 0), com.ilque.domain.resetpolicy.entity.ResetType.WEEKLY);
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T04:00:00Z"), weekly)).isEqualTo(instant("2026-10-26T05:00:00Z"));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-27T06:00:00Z"), weekly)).isEqualTo(instant("2026-11-02T05:00:00Z"));
        assertThat(calculator.calculateFirstBoundary(instant("2026-10-26T05:00:00Z"), weekly)).isEqualTo(instant("2026-10-26T05:00:00Z"));
    }

    private ResetPolicy policy(IntervalUnit unit, int interval, DayOfWeek weekStart, String zone, LocalTime time) {
        return policy(unit, interval, weekStart, zone, time, com.ilque.domain.resetpolicy.entity.ResetType.CUSTOM);
    }

    private ResetPolicy policy(IntervalUnit unit, Integer interval, DayOfWeek weekStart, String zone, LocalTime time,
                               com.ilque.domain.resetpolicy.entity.ResetType type) {
        return ResetPolicy.builder().name("test").resetType(type)
                .resetTime(time).timezone(zone).weekStartDay(weekStart).active(true)
                .interval(interval).intervalUnit(unit).build();
    }

    private Instant instant(String value) { return OffsetDateTime.parse(value).toInstant(); }
}
