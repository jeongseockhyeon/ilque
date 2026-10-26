package com.ilque.domain.resetpolicy.dto;

import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ResetPolicyResponse(
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
    public static ResetPolicyResponse from(ResetPolicy policy) {
        return new ResetPolicyResponse(policy.getResetPolicyId(), policy.getName(), policy.getResetType(),
                policy.getResetTime(), policy.getTimezone(), policy.getWeekStartDay(), policy.getActive(),
                policy.getInterval(), policy.getIntervalUnit());
    }
}
