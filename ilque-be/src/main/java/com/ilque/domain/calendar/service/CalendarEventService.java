package com.ilque.domain.calendar.service;

import com.ilque.domain.calendar.dto.CalendarEventPatchRequest;
import com.ilque.domain.calendar.dto.CalendarEventRequest;
import com.ilque.domain.calendar.dto.CalendarEventResponse;
import com.ilque.domain.calendar.entity.CalendarEvent;
import com.ilque.domain.calendar.repository.CalendarEventRepository;
import com.ilque.domain.category.entity.Category;
import com.ilque.domain.category.repository.CategoryRepository;
import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.service.UserService;
import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarEventService {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("Asia/Seoul");
    private static final int UPCOMING_LIMIT = 5;

    private final CalendarEventRepository repository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public List<CalendarEventResponse> findAllActive() {
        Long userId = requireLocalUser().getUserId();
        return repository.findAllByUser_UserIdAndActiveTrueOrderByStartAtAscCalendarEventIdAsc(userId)
                .stream().map(CalendarEventResponse::from).toList();
    }

    public CalendarEventResponse findById(Long id) {
        return CalendarEventResponse.from(findOwnedEvent(id));
    }

    public List<CalendarEventResponse> findByDate(LocalDate date) {
        if (date == null) throw new BadRequestException("date는 필수입니다.");
        return findBetween(date, date.plusDays(1));
    }

    public List<CalendarEventResponse> findBetween(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        Instant fromInstant = from.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
        Instant toInstant = to.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
        return repository.findOverlappingActiveEvents(requireLocalUser().getUserId(), fromInstant, toInstant)
                .stream().map(CalendarEventResponse::from).toList();
    }

    public List<CalendarEventResponse> findUpcoming(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        Instant fromInstant = from.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
        Instant now = Instant.now();
        return repository.findOverlappingActiveEvents(requireLocalUser().getUserId(), fromInstant, toInstant)
                .stream()
                .filter(event -> event.getEndAt() == null || !event.getEndAt().isBefore(now))
                .limit(UPCOMING_LIMIT)
                .map(CalendarEventResponse::from)
                .toList();
    }

    @Transactional
    public CalendarEventResponse create(CalendarEventRequest request) {
        if (request == null) throw new BadRequestException("요청 본문은 필수입니다.");
        Category category = resolveCategory(request.categoryId());
        User user = userService.initializeLocalUser();
        CalendarEvent event = CalendarEvent.create(request.title(), request.description(), request.eventType(),
                request.startAt(), request.endAt(), request.allDay(), request.reminderAt(), category, user);
        return CalendarEventResponse.from(repository.save(event));
    }

    @Transactional
    public CalendarEventResponse patch(Long id, CalendarEventPatchRequest request) {
        if (request == null) throw new BadRequestException("요청 본문은 필수입니다.");
        CalendarEvent event = findOwnedEvent(id);
        String title = request.isTitleProvided() ? request.getTitle() : event.getTitle();
        String description = request.isDescriptionProvided() ? request.getDescription() : event.getDescription();
        var type = request.isEventTypeProvided() ? request.getEventType() : event.getEventType();
        Instant startAt = request.isStartAtProvided() ? request.getStartAt() : event.getStartAt();
        Instant endAt = request.isEndAtProvided() ? request.getEndAt() : event.getEndAt();
        Boolean allDay = request.isAllDayProvided() ? request.getAllDay() : event.getAllDay();
        Instant reminderAt = request.isReminderAtProvided() ? request.getReminderAt() : event.getReminderAt();
        Category category = request.isCategoryIdProvided()
                ? resolveCategory(request.getCategoryId()) : event.getCategory();

        event.update(title, description, type, startAt, endAt, allDay, reminderAt, category);
        if (request.isActiveProvided()) {
            if (request.getActive() == null) throw new BadRequestException("active는 null일 수 없습니다.");
            if (request.getActive()) event.activate(); else event.deactivate();
        }
        return CalendarEventResponse.from(event);
    }

    @Transactional
    public CalendarEventResponse softDelete(Long id) {
        CalendarEvent event = findOwnedEvent(id);
        event.deactivate();
        return CalendarEventResponse.from(event);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 Category를 찾을 수 없습니다."));
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("비활성 Category는 CalendarEvent에 사용할 수 없습니다.");
        }
        return category;
    }

    private CalendarEvent findOwnedEvent(Long id) {
        return repository.findByCalendarEventIdAndUser_UserId(id, requireLocalUser().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 CalendarEvent를 찾을 수 없습니다."));
    }

    private User requireLocalUser() {
        return userService.findLocalUser()
                .orElseThrow(() -> new ResourceNotFoundException("Local User를 찾을 수 없습니다."));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new BadRequestException("from과 to는 필수입니다.");
        if (to.isBefore(from)) throw new BadRequestException("to는 from보다 빠를 수 없습니다.");
    }
}
