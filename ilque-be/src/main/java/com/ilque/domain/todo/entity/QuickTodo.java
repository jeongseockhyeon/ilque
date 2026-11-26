package com.ilque.domain.todo.entity;

import com.ilque.domain.user.entity.User;
import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quick_todo")
public class QuickTodo extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quickTodoId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Boolean active = true;

    @Column
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private QuickTodo(String title, User user) {
        this.title = validateTitle(title);
        if (user == null) {
            throw new BadRequestException("user는 필수입니다.");
        }
        this.user = user;
        this.active = true;
    }

    public static QuickTodo create(String title, User user) {
        return new QuickTodo(title, user);
    }

    public void complete(Instant completedAt) {
        if (!Boolean.TRUE.equals(active)) {
            throw new BadRequestException("비활성 Quick Todo는 완료할 수 없습니다.");
        }
        if (this.completedAt != null) {
            throw new BadRequestException("완료된 Quick Todo입니다.");
        }
        if (completedAt == null) {
            throw new BadRequestException("completedAt은 필수입니다.");
        }
        this.completedAt = completedAt;
    }

    public void deactivate() {
        this.active = false;
    }

    @PrePersist
    private void validateBeforePersist() {
        title = validateTitle(title);
        if (user == null) {
            throw new BadRequestException("user는 필수입니다.");
        }
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("title은 필수이며 공백일 수 없습니다.");
        }
        if (!title.equals(title.trim())) {
            throw new BadRequestException("title 앞뒤 공백은 허용되지 않습니다.");
        }
        if (title.length() > 100) {
            throw new BadRequestException("title은 100자 이하여야 합니다.");
        }
        return title;
    }
}
