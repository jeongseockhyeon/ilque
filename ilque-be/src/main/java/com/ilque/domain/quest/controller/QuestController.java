package com.ilque.domain.quest.controller;

import com.ilque.domain.quest.dto.QuestRequest;
import com.ilque.domain.quest.dto.QuestPatchRequest;
import com.ilque.domain.quest.dto.QuestResponse;
import com.ilque.domain.quest.service.QuestService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {
    private final QuestService questService;

    @GetMapping("/{questId}")
    public ApiResponseEnvelope<QuestResponse> findById(@PathVariable Long questId){
        return ApiResponseEnvelope.success(questService.findResponseById(questId));
    }

    @GetMapping
    public ApiResponseEnvelope<List<QuestResponse>> findAll(){
        return ApiResponseEnvelope.success(questService.findAll());
    }

    @GetMapping("/inactive")
    public ApiResponseEnvelope<List<QuestResponse>> findAllInactive(){
        return ApiResponseEnvelope.success(questService.findAllInactive());
    }

    @PostMapping
    public ResponseEntity<ApiResponseEnvelope<QuestResponse>> create(@Valid @RequestBody QuestRequest request) {
        return ResponseEntity.ok(ApiResponseEnvelope.success(questService.createResponse(request)));
    }

    @PatchMapping("/{questId}")
    public ApiResponseEnvelope<QuestResponse> patch(@PathVariable Long questId,
                                                    @Valid @RequestBody QuestPatchRequest request) {
        return ApiResponseEnvelope.success(questService.patchResponse(questId, request));
    }

    @DeleteMapping("/{questId}")
    public ApiResponseEnvelope<QuestResponse> softDelete(@PathVariable Long questId) {
        return ApiResponseEnvelope.success(questService.softDelete(questId));
    }

    @DeleteMapping("/{questId}/hard")
    public ApiResponseEnvelope<Void> hardDelete(@PathVariable Long questId) {
        questService.hardDelete(questId);
        return ApiResponseEnvelope.success(null);
    }
}
