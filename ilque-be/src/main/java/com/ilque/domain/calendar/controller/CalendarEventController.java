package com.ilque.domain.calendar.controller;

import com.ilque.domain.calendar.dto.CalendarEventPatchRequest;
import com.ilque.domain.calendar.dto.CalendarEventRequest;
import com.ilque.domain.calendar.dto.CalendarEventResponse;
import com.ilque.domain.calendar.service.CalendarEventService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarEventController {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("Asia/Seoul");
    private final CalendarEventService calendarEventService;

    @GetMapping("/events")
    public ApiResponseEnvelope<List<CalendarEventResponse>> findAll(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ApiResponseEnvelope.success(calendarEventService.findAllActive(from, to));
    }

    @GetMapping("/events/{calendarEventId}")
    public ApiResponseEnvelope<CalendarEventResponse> findById(@PathVariable Long calendarEventId) {
        return ApiResponseEnvelope.success(calendarEventService.findById(calendarEventId));
    }

    @PostMapping("/events")
    public ApiResponseEnvelope<CalendarEventResponse> create(@Valid @RequestBody CalendarEventRequest request) {
        return ApiResponseEnvelope.success(calendarEventService.create(request));
    }

    @PatchMapping("/events/{calendarEventId}")
    public ApiResponseEnvelope<CalendarEventResponse> patch(@PathVariable Long calendarEventId,
                                                            @Valid @RequestBody CalendarEventPatchRequest request) {
        return ApiResponseEnvelope.success(calendarEventService.patch(calendarEventId, request));
    }

    @DeleteMapping("/events/{calendarEventId}")
    public ApiResponseEnvelope<CalendarEventResponse> delete(@PathVariable Long calendarEventId) {
        return ApiResponseEnvelope.success(calendarEventService.softDelete(calendarEventId));
    }

    @GetMapping("/upcoming")
    public ApiResponseEnvelope<List<CalendarEventResponse>> findUpcoming(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate today = LocalDate.now(DEFAULT_TIME_ZONE);
        return ApiResponseEnvelope.success(calendarEventService.findUpcoming(
                from == null ? today : from,
                to == null ? (from == null ? today.plusDays(7) : from.plusDays(7)) : to));
    }

    @GetMapping("/date/{date}")
    public ApiResponseEnvelope<List<CalendarEventResponse>> findByDate(@PathVariable LocalDate date) {
        return ApiResponseEnvelope.success(calendarEventService.findByDate(date));
    }
}
