package com.ilque.domain.dashboard.controller;

import com.ilque.domain.dashboard.dto.DashboardResponse;
import com.ilque.domain.dashboard.service.DashboardService;
import com.ilque.global.response.ApiResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponseEnvelope<DashboardResponse> findDashboard() {
        return ApiResponseEnvelope.success(dashboardService.findDashboard(Instant.now()));
    }
}
