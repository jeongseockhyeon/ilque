package com.ilque.domain.calendar.dto;

import com.ilque.domain.calendar.entity.CalendarEvent;
import com.ilque.domain.calendar.entity.CalendarEventType;

import java.time.Instant;

public record CalendarEventResponse(Long calendarEventId, String title, String description,
                                    CalendarEventType eventType, Instant startAt, Instant endAt,
                                    Boolean allDay, Long categoryId, Instant reminderAt,
                                    Boolean active, Instant createdAt, Instant updatedAt) {
    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(event.getCalendarEventId(), event.getTitle(), event.getDescription(),
                event.getEventType(), event.getStartAt(), event.getEndAt(), event.getAllDay(),
                event.getCategory() == null ? null : event.getCategory().getCategoryId(),
                event.getReminderAt(), event.getActive(), event.getCreatedAt(), event.getUpdatedAt());
    }
}
