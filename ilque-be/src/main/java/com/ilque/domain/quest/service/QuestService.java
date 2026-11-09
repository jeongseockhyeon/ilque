package com.ilque.domain.quest.service;

import com.ilque.domain.category.entity.Category;
import com.ilque.domain.category.repository.CategoryRepository;
import com.ilque.domain.quest.dto.QuestRequest;
import com.ilque.domain.quest.dto.QuestResetPolicyRequest;
import com.ilque.domain.quest.dto.QuestResponse;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.RecurrenceType;
import com.ilque.domain.quest.repository.QuestRepository;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetPolicyValidator;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.resetpolicy.repository.ResetPolicyRepository;
import com.ilque.domain.user.service.UserService;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {
    private final QuestRepository questRepository;
    private final CategoryRepository categoryRepository;
    private final ResetPolicyRepository resetPolicyRepository;
    private final UserService userService;

    public Quest findById(Long id){
        return questRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public QuestResponse findResponseById(Long id) {
        return QuestResponse.from(findById(id));
    }

    @Transactional
    public Quest create(QuestRequest request) {
        if (request == null) throw new BadRequestException("요청 본문은 필수입니다.");
        Category category = request.categoryId() == null ? null : categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 Category를 찾을 수 없습니다."));
        if (category != null && Boolean.FALSE.equals(category.getActive())) {
            throw new BadRequestException("비활성 Category는 Quest에 사용할 수 없습니다.");
        }
        ResetPolicy resetPolicy = resolveResetPolicy(request);
        Quest quest = Quest.create(request.title(), request.description(), request.questType(),
                request.recurrenceType(), request.customDays(), request.difficulty(), request.targetCount(),
                request.targetTimeMinutes(), category, resetPolicy, userService.initializeLocalUser());
        return questRepository.save(quest);
    }

    @Transactional
    public QuestResponse createResponse(QuestRequest request) {
        return QuestResponse.from(create(request));
    }

    private ResetPolicy resolveResetPolicy(QuestRequest request) {
        if (request.recurrenceType() == RecurrenceType.ONCE) {
            if (request.resetPolicyId() != null || request.resetPolicy() != null) {
                throw new BadRequestException("ONCE Quest에는 ResetPolicy를 지정할 수 없습니다.");
            }
            return null;
        }
        if (request.resetPolicyId() != null && request.resetPolicy() != null) {
            throw new BadRequestException("resetPolicyId와 resetPolicy는 함께 사용할 수 없습니다.");
        }
        if (request.resetPolicyId() != null) {
            ResetPolicy policy = resetPolicyRepository.findById(request.resetPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException("해당 ResetPolicy를 찾을 수 없습니다."));
            if (!Boolean.TRUE.equals(policy.getActive())) {
                throw new BadRequestException("비활성 ResetPolicy는 Quest에 사용할 수 없습니다.");
            }
            return policy;
        }
        if (request.resetPolicy() == null) {
            throw new BadRequestException("반복 Quest에는 resetPolicyId 또는 resetPolicy가 필요합니다.");
        }
        return findOrCreateTemporaryPolicy(request.resetPolicy());
    }

    private ResetPolicy findOrCreateTemporaryPolicy(QuestResetPolicyRequest request) {
        if (request.isResetTimeProvided() && request.getResetTime() == null) {
            throw new BadRequestException("resetTime은 null일 수 없습니다.");
        }
        if (request.getResetType() != ResetType.CUSTOM
                && (request.isIntervalProvided() || request.isIntervalUnitProvided())) {
            throw new BadRequestException("해당 resetType에서는 CUSTOM 전용 필드를 사용할 수 없습니다.");
        }
        if ((request.getResetType() == ResetType.DAILY && request.isWeekStartDayProvided())
                || (request.getResetType() == ResetType.CUSTOM
                && request.getIntervalUnit() == com.ilque.domain.resetpolicy.entity.IntervalUnit.DAY
                && request.isWeekStartDayProvided())) {
            throw new BadRequestException("해당 resetType에서는 weekStartDay를 사용할 수 없습니다.");
        }
        LocalTime resetTime = request.getResetTime() == null ? LocalTime.MIDNIGHT : request.getResetTime();
        try {
            ResetPolicyValidator.validate(request.getName(), request.getResetType(), resetTime, request.getTimezone(),
                    request.getWeekStartDay(), true, request.getInterval(), request.getIntervalUnit());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        return resetPolicyRepository
                .findFirstByResetTypeAndResetTimeAndTimezoneAndWeekStartDayAndIntervalAndIntervalUnitAndActiveTrue(
                        request.getResetType(), resetTime, request.getTimezone(), request.getWeekStartDay(),
                        request.getInterval(), request.getIntervalUnit())
                .orElseGet(() -> resetPolicyRepository.save(ResetPolicy.builder()
                        .name(request.getName()).resetType(request.getResetType()).resetTime(resetTime)
                        .timezone(request.getTimezone()).weekStartDay(request.getWeekStartDay()).active(true)
                        .interval(request.getInterval()).intervalUnit(request.getIntervalUnit()).build()));
    }
}
