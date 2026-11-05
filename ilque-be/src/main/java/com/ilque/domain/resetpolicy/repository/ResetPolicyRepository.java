package com.ilque.domain.resetpolicy.repository;

import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPolicyRepository extends JpaRepository<ResetPolicy, Long> {
    java.util.Optional<ResetPolicy> findFirstByResetTypeAndResetTimeAndTimezoneAndWeekStartDayAndIntervalAndIntervalUnitAndActiveTrue(
            com.ilque.domain.resetpolicy.entity.ResetType resetType, java.time.LocalTime resetTime,
            String timezone, java.time.DayOfWeek weekStartDay, Integer interval,
            com.ilque.domain.resetpolicy.entity.IntervalUnit intervalUnit);
}
