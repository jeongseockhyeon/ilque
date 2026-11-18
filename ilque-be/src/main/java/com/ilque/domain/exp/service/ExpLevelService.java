package com.ilque.domain.exp.service;

import com.ilque.domain.user.entity.User;
import com.ilque.global.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpLevelService {

    @Transactional
    public void addEarnedExp(User user, Integer earnedExp) {
        if (user == null) {
            throw new BadRequestException("user는 필수입니다.");
        }
        if (earnedExp == null || earnedExp < 0) {
            throw new BadRequestException("earnedExp는 0 이상이어야 합니다.");
        }

        user.addExp(earnedExp);
    }

    public long requiredExp(int level) {
        if (level < 1) {
            throw new BadRequestException("level은 1 이상이어야 합니다.");
        }
        return 100L + ((long) (level - 1) * 50L);
    }
}
