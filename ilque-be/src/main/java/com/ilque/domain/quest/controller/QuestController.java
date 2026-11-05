package com.ilque.domain.quest.controller;

import com.ilque.domain.quest.dto.QuestRequest;
import com.ilque.domain.quest.dto.QuestResponse;
import com.ilque.domain.quest.service.QuestService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {
    private final QuestService questService;

    @PostMapping
    public ResponseEntity<ApiResponseEnvelope<QuestResponse>> create(@Valid @RequestBody QuestRequest request) {
        return ResponseEntity.ok(ApiResponseEnvelope.success(QuestResponse.from(questService.create(request))));
    }
}
