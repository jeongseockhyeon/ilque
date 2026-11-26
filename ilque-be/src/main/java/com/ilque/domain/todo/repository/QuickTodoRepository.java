package com.ilque.domain.todo.repository;

import com.ilque.domain.todo.entity.QuickTodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuickTodoRepository extends JpaRepository<QuickTodo, Long> {

    List<QuickTodo> findAllByUser_UserIdAndActiveTrueAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCompletedAtAscCreatedAtAsc(
            Long userId, Instant from, Instant to);

    Optional<QuickTodo> findByQuickTodoIdAndUser_UserIdAndActiveTrue(Long quickTodoId, Long userId);
}
