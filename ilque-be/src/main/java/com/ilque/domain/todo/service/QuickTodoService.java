package com.ilque.domain.todo.service;

import com.ilque.domain.todo.dto.QuickTodoRequest;
import com.ilque.domain.todo.dto.QuickTodoPatchRequest;
import com.ilque.domain.todo.dto.QuickTodoResponse;
import com.ilque.domain.todo.entity.QuickTodo;
import com.ilque.domain.todo.repository.QuickTodoRepository;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuickTodoService {

    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("Asia/Seoul");
    private static final int DASHBOARD_LIMIT = 5;

    private final QuickTodoRepository quickTodoRepository;
    private final UserService userService;

    public List<QuickTodoResponse> findDashboardTodos(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("date는 필수입니다.");
        }
        User user = requireLocalUser();
        Instant from = date.atStartOfDay(DEFAULT_TIME_ZONE).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(DEFAULT_TIME_ZONE).toInstant();

        return quickTodoRepository
                .findAllByUser_UserIdAndActiveTrueAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCompletedAtAscCreatedAtAsc(
                        user.getUserId(), from, to)
                .stream()
                .sorted(Comparator
                        .comparing((QuickTodo todo) -> todo.getCompletedAt() != null)
                        .thenComparing(todo -> todo.getCompletedAt() == null
                                ? todo.getCreatedAt() : todo.getCompletedAt(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(DASHBOARD_LIMIT)
                .map(QuickTodoResponse::from)
                .toList();
    }

    @Transactional
    public QuickTodoResponse create(QuickTodoRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 본문은 필수입니다.");
        }
        User user = userService.initializeLocalUser();
        return QuickTodoResponse.from(quickTodoRepository.save(QuickTodo.create(request.title(), user)));
    }

    @Transactional
    public QuickTodoResponse complete(Long quickTodoId) {
        User user = requireLocalUser();
        QuickTodo todo = quickTodoRepository
                .findByQuickTodoIdAndUser_UserIdAndActiveTrue(quickTodoId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("활성 Quick Todo를 찾을 수 없습니다."));
        if (todo.getCompletedAt() == null) {
            todo.complete(Instant.now());
        }
        return QuickTodoResponse.from(todo);
    }

    @Transactional
    public QuickTodoResponse updateTitle(Long quickTodoId, QuickTodoPatchRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 본문은 필수입니다.");
        }
        User user = requireLocalUser();
        QuickTodo todo = quickTodoRepository
                .findByQuickTodoIdAndUser_UserIdAndActiveTrue(quickTodoId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("활성 Quick Todo를 찾을 수 없습니다."));
        todo.updateTitle(request.title());
        return QuickTodoResponse.from(todo);
    }

    private User requireLocalUser() {
        return userService.findLocalUser()
                .orElseThrow(() -> new ResourceNotFoundException("Local User를 찾을 수 없습니다."));
    }
}
