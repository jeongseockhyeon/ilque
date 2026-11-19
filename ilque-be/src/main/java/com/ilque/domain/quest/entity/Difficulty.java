package com.ilque.domain.quest.entity;

import lombok.Getter;

@Getter
public enum Difficulty {
    EASY(10, 1, 20),
    NORMAL(20, 1, 30),
    HARD(40, 2, 50),
    EPIC(80, 3, 100);

    private final int baseExp;
    private final int expPerMinute;
    private final int completionBonus;

    Difficulty(int baseExp, int expPerMinute, int completionBonus) {
        this.baseExp = baseExp;
        this.expPerMinute = expPerMinute;
        this.completionBonus = completionBonus;
    }
}
