package com.ilque.domain.resetpolicy.controller;

import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.resetpolicy.service.ResetPolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResetPolicyController.class)
class ResetPolicyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResetPolicyService service;

    @Test
    void listReturnsResponseDtoWithoutEntityFields() throws Exception {
        given(service.findAll()).willReturn(List.of(policy()));

        mockMvc.perform(get("/api/reset-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].resetPolicyId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("daily"))
                .andExpect(jsonPath("$.data[0].createdAt").doesNotExist());
    }

    @Test
    void createReturnsResponseDto() throws Exception {
        given(service.create(org.mockito.ArgumentMatchers.any())).willReturn(policy());
        mockMvc.perform(post("/api/reset-policies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"daily\",\"resetType\":\"DAILY\",\"timezone\":\"UTC\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("daily"))
                .andExpect(jsonPath("$.data.createdAt").doesNotExist());
    }

    @Test
    void patchReturnsResponseDto() throws Exception {
        given(service.patch(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).willReturn(policy());
        mockMvc.perform(patch("/api/reset-policies/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("daily"));
    }

    @Test
    void invalidRequestReturnsValidationErrorDetails() throws Exception {
        mockMvc.perform(post("/api/reset-policies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("active"));
    }

    private ResetPolicy policy() {
        return ResetPolicy.builder().resetPolicyId(1L).name("daily").resetType(ResetType.DAILY)
                .resetTime(LocalTime.MIDNIGHT).timezone("UTC").active(true).build();
    }
}
