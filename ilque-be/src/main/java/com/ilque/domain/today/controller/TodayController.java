package com.ilque.domain.today.controller;

import com.ilque.domain.today.dto.TodayResponse;
import com.ilque.domain.today.service.TodayService;
import com.ilque.global.response.ApiResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final TodayService todayService;

    @GetMapping
    public ApiResponseEnvelope<TodayResponse> findToday() {
        return ApiResponseEnvelope.success(todayService.findToday(Instant.now()));
    }
}
