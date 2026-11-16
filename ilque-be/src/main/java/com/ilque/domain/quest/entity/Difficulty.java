package com.ilque.domain.quest.entity;

import lombok.Getter;

@Getter
public enum Difficulty {
    EASY(10, 1),
    NORMAL(20, 1),
    HARD(40, 2),
    EPIC(80, 3);

    private final int baseExp;
    private final int expPerMinute;

    Difficulty(int baseExp, int expPerMinute) {
        this.baseExp = baseExp;
        this.expPerMinute = expPerMinute;
    }
}
