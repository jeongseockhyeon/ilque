package com.ilque.domain.calendar.dto;

import com.ilque.domain.calendar.entity.CalendarEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CalendarEventRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 1000) String description,
        @NotNull CalendarEventType eventType,
        @NotNull Instant startAt,
        Instant endAt,
        @NotNull Boolean allDay,
        Instant reminderAt,
        Long categoryId
) {
}
