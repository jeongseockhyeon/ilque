package com.ilque.global;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 공통 Envelope 및 전역 예외 처리 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    private static final String ISO_8601_UTC =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z";

    @Autowired
    MockMvc mockMvc;

    @Test
    void 헬스체크는_성공_Envelope를_반환한다() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.error", nullValue()))
                .andExpect(jsonPath("$.timestamp", matchesPattern(ISO_8601_UTC)));
    }

    @Test
    void 검증_실패는_400과_VALIDATION_ERROR를_반환한다() throws Exception {
        mockMvc.perform(post("/api/test-support/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("title"))
                .andExpect(jsonPath("$.error.details[0].message").value("제목은 필수입니다."))
                .andExpect(jsonPath("$.timestamp", matchesPattern(ISO_8601_UTC)));
    }

    @Test
    void 잘못된_요청은_400과_BAD_REQUEST를_반환한다() throws Exception {
        mockMvc.perform(get("/api/test-support/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("잘못된 상태 전이입니다."));
    }

    @Test
    void 존재하지_않는_리소스는_404와_RESOURCE_NOT_FOUND를_반환한다() throws Exception {
        mockMvc.perform(get("/api/test-support/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 존재하지_않는_경로는_404와_RESOURCE_NOT_FOUND를_반환한다() throws Exception {
        mockMvc.perform(get("/api/존재하지-않는-경로"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 충돌은_409와_RESOURCE_CONFLICT를_반환한다() throws Exception {
        mockMvc.perform(get("/api/test-support/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_CONFLICT"));
    }

    @Test
    void 예상하지_못한_예외는_500과_INTERNAL_SERVER_ERROR를_반환하고_내부_정보를_노출하지_않는다()
            throws Exception {
        mockMvc.perform(get("/api/test-support/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.details", nullValue()))
                .andExpect(content().string(not(containsString("jdbc:sqlite"))))
                .andExpect(content().string(not(containsString("at com.ilque"))));
    }
}
