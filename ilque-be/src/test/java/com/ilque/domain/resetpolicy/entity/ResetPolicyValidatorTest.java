package com.ilque.domain.resetpolicy.entity;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResetPolicyValidatorTest {
    @Test
    void customIntervalMustBePositive() {
        assertThatThrownBy(() -> policy(0, IntervalUnit.DAY, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy(-1, IntervalUnit.DAY, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customWeekRequiresWeekStartDay() {
        assertThatThrownBy(() -> policy(1, IntervalUnit.WEEK, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unusedFieldsAreRejected() {
        assertThatThrownBy(() -> ResetPolicy.builder().name("daily").resetType(ResetType.DAILY)
                .resetTime(LocalTime.MIDNIGHT).timezone("UTC").active(true)
                .interval(1).intervalUnit(IntervalUnit.DAY).build()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy(1, IntervalUnit.DAY, DayOfWeek.MONDAY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void timezoneAndResetTimeAreValidatedAndDefaulted() {
        assertThatThrownBy(() -> policy(1, IntervalUnit.DAY, null, "not-a-zone"))
                .isInstanceOf(IllegalArgumentException.class);
        ResetPolicy policy = ResetPolicy.builder().name("default").resetType(ResetType.CUSTOM)
                .timezone("UTC").active(true).interval(1).intervalUnit(IntervalUnit.DAY).build();
        assertThat(policy.getResetTime()).isEqualTo(LocalTime.MIDNIGHT);
    }

    private ResetPolicy policy(int interval, IntervalUnit unit, DayOfWeek weekStart) {
        return policy(interval, unit, weekStart, "UTC");
    }

    private ResetPolicy policy(int interval, IntervalUnit unit, DayOfWeek weekStart, String zone) {
        return ResetPolicy.builder().name("test").resetType(ResetType.CUSTOM).resetTime(LocalTime.MIDNIGHT)
                .timezone(zone).active(true).interval(interval).intervalUnit(unit).weekStartDay(weekStart).build();
    }
}
