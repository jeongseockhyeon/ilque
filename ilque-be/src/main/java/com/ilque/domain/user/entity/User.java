package com.ilque.domain.user.entity;

import com.ilque.global.base.Base;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private Long totalExp = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    public void addExp(long earnedExp) {
        if (earnedExp < 0) {
            throw new IllegalArgumentException("earnedExp는 0 이상이어야 합니다.");
        }
        this.totalExp += earnedExp;
        while (this.totalExp >= requiredExp(this.level)) {
            this.level++;
        }
    }

    private long requiredExp(int currentLevel) {
        return 100L + ((long) (currentLevel - 1) * 50L);
    }
}
