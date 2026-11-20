package com.ilque.domain.achievement.controller;

import com.ilque.domain.achievement.dto.AchievementResponse;
import com.ilque.domain.achievement.service.AchievementService;
import com.ilque.global.response.ApiResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ApiResponseEnvelope<List<AchievementResponse>> findAll() {
        return ApiResponseEnvelope.success(achievementService.findAll());
    }

    @GetMapping("/unlocked")
    public ApiResponseEnvelope<List<AchievementResponse>> findUnlocked() {
        return ApiResponseEnvelope.success(achievementService.findUnlocked());
    }

    @PatchMapping("/{achievementId}/equip")
    public ApiResponseEnvelope<AchievementResponse> equip(@PathVariable Long achievementId) {
        return ApiResponseEnvelope.success(achievementService.equip(achievementId));
    }
}
