package com.ilque.domain.resetpolicy.controller;

import com.ilque.domain.resetpolicy.dto.ResetPolicyRequest;
import com.ilque.domain.resetpolicy.dto.ResetPolicyPatchRequest;
import com.ilque.domain.resetpolicy.dto.ResetPolicyResponse;
import com.ilque.domain.resetpolicy.service.ResetPolicyService;
import com.ilque.global.response.ApiResponseEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reset-policies")
@RequiredArgsConstructor
public class ResetPolicyController {
    private final ResetPolicyService service;
    @GetMapping
    public ApiResponseEnvelope<List<ResetPolicyResponse>> findAll() {
        return ApiResponseEnvelope.success(service.findAll().stream().map(ResetPolicyResponse::from).toList());
    }
    @PostMapping
    public ResponseEntity<ApiResponseEnvelope<ResetPolicyResponse>> create(@Valid @RequestBody ResetPolicyRequest request) {
        return ResponseEntity.ok(ApiResponseEnvelope.success(ResetPolicyResponse.from(service.create(request))));
    }
    @PatchMapping("/{resetPolicyId}")
    public ApiResponseEnvelope<ResetPolicyResponse> update(@PathVariable Long resetPolicyId,
                                                           @Valid @RequestBody ResetPolicyPatchRequest request) {
        return ApiResponseEnvelope.success(ResetPolicyResponse.from(service.patch(resetPolicyId, request)));
    }
}
