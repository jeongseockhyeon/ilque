package com.ilque.domain.resetpolicy.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ResetPolicyValidator {

    private ResetPolicyValidator() {
    }

    public static void validate(
            String name,
            ResetType resetType,
            LocalTime resetTime,
            String timezone,
            DayOfWeek weekStartDay,
            Boolean active,
            Integer interval,
            IntervalUnit intervalUnit
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("정책 이름은 필수입니다.");
        }
        if (resetType == null) {
            throw new IllegalArgumentException("resetType은 필수입니다.");
        }
        if (resetTime == null) {
            throw new IllegalArgumentException("resetTime은 null일 수 없습니다.");
        }
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("timezone은 필수입니다.");
        }
        try {
            ZoneId.of(timezone);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("유효하지 않은 timezone입니다.", ex);
        }
        if (active == null) {
            throw new IllegalArgumentException("active는 필수입니다.");
        }

        switch (resetType) {
            case DAILY -> requireUnused(weekStartDay, interval, intervalUnit);
            case WEEKLY -> {
                if (weekStartDay == null) {
                    throw new IllegalArgumentException("WEEKLY에는 weekStartDay가 필요합니다.");
                }
                requireUnused(interval, intervalUnit);
            }
            case CUSTOM -> {
                if (interval == null || interval < 1) {
                    throw new IllegalArgumentException("CUSTOM interval은 1 이상이어야 합니다.");
                }
                if (intervalUnit == null) {
                    throw new IllegalArgumentException("CUSTOM에는 intervalUnit이 필요합니다.");
                }
                if (intervalUnit == IntervalUnit.WEEK && weekStartDay == null) {
                    throw new IllegalArgumentException("CUSTOM + WEEK에는 weekStartDay가 필요합니다.");
                }
                if (intervalUnit == IntervalUnit.DAY && weekStartDay != null) {
                    throw new IllegalArgumentException("CUSTOM + DAY에서는 weekStartDay를 사용하지 않습니다.");
                }
            }
        }
    }

    private static void requireUnused(Object... values) {
        for (Object value : values) {
            if (value != null) {
                throw new IllegalArgumentException("해당 resetType에서는 사용하지 않는 필드입니다.");
            }
        }
    }
}
