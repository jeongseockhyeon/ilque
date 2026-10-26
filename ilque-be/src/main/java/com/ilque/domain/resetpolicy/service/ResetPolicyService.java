package com.ilque.domain.resetpolicy.service;

import com.ilque.domain.resetpolicy.dto.ResetPolicyRequest;
import com.ilque.domain.resetpolicy.dto.ResetPolicyPatchRequest;
import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetPolicyValidator;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.resetpolicy.repository.ResetPolicyRepository;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResetPolicyService {
    private final ResetPolicyRepository repository;
    public List<ResetPolicy> findAll() { return repository.findAll(); }
    public ResetPolicy findById(Long id) { return repository.findById(id).orElseThrow(ResourceNotFoundException::new); }

    @Transactional
    public ResetPolicy create(ResetPolicyRequest request) {
        validate(request);
        return repository.save(toEntity(request));
    }

    @Transactional
    public ResetPolicy update(Long id, ResetPolicyRequest request) {
        validate(request);
        ResetPolicy current = findById(id);
        current.update(request.getName(), request.getResetType(), request.getResetTime(), request.getTimezone(),
                request.getWeekStartDay(), request.getActive(), request.getInterval(), request.getIntervalUnit());
        return current;
    }

    @Transactional
    public ResetPolicy patch(Long id, ResetPolicyPatchRequest request) {
        if (request == null) throw new BadRequestException("요청 본문은 필수입니다.");
        ResetPolicy current = findById(id);
        String name = request.isNameProvided() ? request.getName() : current.getName();
        ResetType resetType = request.isResetTypeProvided() ? request.getResetType() : current.getResetType();
        java.time.LocalTime resetTime = request.isResetTimeProvided() ? request.getResetTime() : current.getResetTime();
        String timezone = request.isTimezoneProvided() ? request.getTimezone() : current.getTimezone();
        java.time.DayOfWeek weekStartDay = request.isWeekStartDayProvided() ? request.getWeekStartDay() : current.getWeekStartDay();
        Boolean active = request.isActiveProvided() ? request.getActive() : current.getActive();
        Integer interval = request.isIntervalProvided() ? request.getInterval() : current.getInterval();
        IntervalUnit intervalUnit = request.isIntervalUnitProvided() ? request.getIntervalUnit() : current.getIntervalUnit();

        if ((request.isResetTimeProvided() && resetTime == null)
                || (request.isTimezoneProvided() && timezone == null)
                || (request.isResetTypeProvided() && resetType == null)
                || (request.isActiveProvided() && active == null)) {
            throw new BadRequestException("명시적 null을 허용하지 않는 필드입니다.");
        }
        if (resetType != ResetType.CUSTOM
                && (request.isIntervalProvided() || request.isIntervalUnitProvided()))
            throw new BadRequestException("해당 resetType에서는 CUSTOM 전용 필드를 사용할 수 없습니다.");
        if ((resetType == ResetType.DAILY && request.isWeekStartDayProvided())
                || (resetType == ResetType.CUSTOM && intervalUnit == IntervalUnit.DAY && request.isWeekStartDayProvided()))
            throw new BadRequestException("해당 resetType에서는 weekStartDay를 사용할 수 없습니다.");
        try {
            ResetPolicyValidator.validate(name, resetType, resetTime, timezone, weekStartDay, active, interval, intervalUnit);
            current.update(name, resetType, resetTime, timezone, weekStartDay, active, interval, intervalUnit);
            return current;
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private ResetPolicy toEntity(ResetPolicyRequest request) {
        return ResetPolicy.builder().name(request.getName()).resetType(request.getResetType())
                .resetTime(request.getResetTime()).timezone(request.getTimezone())
                .weekStartDay(request.getWeekStartDay()).active(request.getActive())
                .interval(request.getInterval()).intervalUnit(request.getIntervalUnit()).build();
    }

    private void validate(ResetPolicyRequest request) {
        if (request == null || request.getResetType() == null || request.getTimezone() == null
                || request.getActive() == null) throw new BadRequestException("필수 필드가 누락되었습니다.");
        if (request.getResetType() != ResetType.CUSTOM
                && (request.isIntervalProvided() || request.isIntervalUnitProvided()))
            throw new BadRequestException("해당 resetType에서는 CUSTOM 전용 필드를 사용할 수 없습니다.");
        if ((request.getResetType() == ResetType.DAILY && request.isWeekStartDayProvided())
                || (request.getResetType() == ResetType.CUSTOM
                && request.getIntervalUnit() == IntervalUnit.DAY && request.isWeekStartDayProvided()))
            throw new BadRequestException("해당 resetType에서는 weekStartDay를 사용할 수 없습니다.");
        try {
            ResetPolicyValidator.validate(request.getName(), request.getResetType(), request.getResetTime(),
                    request.getTimezone(), request.getWeekStartDay(), request.getActive(), request.getInterval(),
                    request.getIntervalUnit());
        } catch (IllegalArgumentException ex) { throw new BadRequestException(ex.getMessage()); }
    }
}
