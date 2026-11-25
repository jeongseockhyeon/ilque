package com.ilque.domain.dashboard.controller;

import com.ilque.domain.dashboard.dto.DashboardResponse;
import com.ilque.domain.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void returnsDashboardResponseInCommonEnvelope() throws Exception {
        given(dashboardService.findDashboard(any(Instant.class))).willReturn(
                new DashboardResponse(250L, 2, 150L, 150L, 5, 12,
                        Instant.parse("2026-11-26T05:00:00Z")));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalExp").value(250))
                .andExpect(jsonPath("$.data.level").value(2))
                .andExpect(jsonPath("$.data.currentLevelExp").value(150))
                .andExpect(jsonPath("$.data.nextLevelRequiredExp").value(150))
                .andExpect(jsonPath("$.data.currentStreak").value(5))
                .andExpect(jsonPath("$.data.longestStreak").value(12))
                .andExpect(jsonPath("$.data.nextResetAt").value("2026-11-26T05:00:00Z"));
    }
}
