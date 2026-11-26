package com.ilque.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.ilque.domain.calendar.entity.CalendarEventType;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CalendarEventPatchRequest {
    @Size(max = 100) private String title;
    @Size(max = 1000) private String description;
    private CalendarEventType eventType;
    private Instant startAt;
    private Instant endAt;
    private Boolean allDay;
    private Instant reminderAt;
    private Long categoryId;
    private Boolean active;
    private boolean titleProvided, descriptionProvided, eventTypeProvided, startAtProvided;
    private boolean endAtProvided, allDayProvided, reminderAtProvided, categoryIdProvided, activeProvided;

    @JsonSetter(value="title", nulls=Nulls.SET) public void setTitle(String v){titleProvided=true;title=v;}
    @JsonSetter(value="description", nulls=Nulls.SET) public void setDescription(String v){descriptionProvided=true;description=v;}
    @JsonSetter(value="eventType", nulls=Nulls.SET) public void setEventType(CalendarEventType v){eventTypeProvided=true;eventType=v;}
    @JsonSetter(value="startAt", nulls=Nulls.SET) public void setStartAt(Instant v){startAtProvided=true;startAt=v;}
    @JsonSetter(value="endAt", nulls=Nulls.SET) public void setEndAt(Instant v){endAtProvided=true;endAt=v;}
    @JsonSetter(value="allDay", nulls=Nulls.SET) public void setAllDay(Boolean v){allDayProvided=true;allDay=v;}
    @JsonSetter(value="reminderAt", nulls=Nulls.SET) public void setReminderAt(Instant v){reminderAtProvided=true;reminderAt=v;}
    @JsonSetter(value="categoryId", nulls=Nulls.SET) public void setCategoryId(Long v){categoryIdProvided=true;categoryId=v;}
    @JsonSetter(value="active", nulls=Nulls.SET) public void setActive(Boolean v){activeProvided=true;active=v;}
}
