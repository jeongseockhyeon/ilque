package com.ilque.domain.calendar.repository;

import com.ilque.domain.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("""
            select event from CalendarEvent event
             where event.user.userId = :userId and event.active = true
               and event.startAt < :to
               and ((event.endAt is not null and event.endAt >= :from)
                    or (event.endAt is null and event.startAt >= :from))
             order by event.startAt asc, event.calendarEventId asc
            """)
    List<CalendarEvent> findOverlappingActiveEvents(@Param("userId") Long userId,
                                                     @Param("from") Instant from,
                                                     @Param("to") Instant to);

    Optional<CalendarEvent> findByCalendarEventIdAndUser_UserId(Long id, Long userId);

    List<CalendarEvent> findAllByUser_UserIdAndActiveTrueOrderByStartAtAscCalendarEventIdAsc(Long userId);
}
