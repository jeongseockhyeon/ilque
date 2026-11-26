package com.ilque.domain.calendar.entity;

import com.ilque.domain.category.entity.Category;
import com.ilque.domain.user.entity.User;
import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "calendar_event")
public class CalendarEvent extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calendarEventId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarEventType eventType;

    @Column(nullable = false)
    private Instant startAt;

    @Column
    private Instant endAt;

    @Column(nullable = false)
    private Boolean allDay;

    @Column
    private Instant reminderAt;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private CalendarEvent(String title, String description, CalendarEventType eventType,
                          Instant startAt, Instant endAt, Boolean allDay, Instant reminderAt,
                          Category category, User user) {
        this.user = requireUser(user);
        update(title, description, eventType, startAt, endAt, allDay, reminderAt, category);
        this.active = true;
    }

    public static CalendarEvent create(String title, String description, CalendarEventType eventType,
                                        Instant startAt, Instant endAt, Boolean allDay,
                                        Instant reminderAt, Category category, User user) {
        return new CalendarEvent(title, description, eventType, startAt, endAt, allDay,
                reminderAt, category, user);
    }

    public void update(String title, String description, CalendarEventType eventType,
                       Instant startAt, Instant endAt, Boolean allDay,
                       Instant reminderAt, Category category) {
        validate(title, description, eventType, startAt, endAt, allDay);
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allDay = allDay;
        this.reminderAt = reminderAt;
        this.category = category;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @PrePersist
    @PreUpdate
    private void validateBeforePersist() {
        validate(title, description, eventType, startAt, endAt, allDay);
        requireUser(user);
    }

    private static void validate(String title, String description, CalendarEventType eventType,
                                 Instant startAt, Instant endAt, Boolean allDay) {
        if (title == null || title.isBlank() || !title.equals(title.trim()) || title.length() > 100) {
            throw new BadRequestException("title은 공백이 아닌 100자 이하의 문자열이어야 합니다.");
        }
        if (description != null && (!description.equals(description.trim()) || description.length() > 1000)) {
            throw new BadRequestException("description은 앞뒤 공백 없이 1,000자 이하여야 합니다.");
        }
        if (eventType == null) throw new BadRequestException("eventType은 필수입니다.");
        if (startAt == null) throw new BadRequestException("startAt은 필수입니다.");
        if (endAt != null && endAt.isBefore(startAt)) {
            throw new BadRequestException("endAt은 startAt보다 빠를 수 없습니다.");
        }
        if (allDay == null) throw new BadRequestException("allDay는 필수입니다.");
    }

    private static User requireUser(User user) {
        if (user == null) throw new BadRequestException("user는 필수입니다.");
        return user;
    }
}
