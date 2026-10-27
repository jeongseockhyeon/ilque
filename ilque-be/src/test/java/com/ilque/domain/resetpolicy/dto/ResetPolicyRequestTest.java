package com.ilque.domain.resetpolicy.dto;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPolicyRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void omittedResetTimeUsesDefaultWithoutMarkingItProvided() throws Exception {
        ResetPolicyRequest request = objectMapper.readValue(
                "{\"name\":\"daily\",\"resetType\":\"DAILY\",\"timezone\":\"UTC\",\"active\":true}",
                ResetPolicyRequest.class);

        assertThat(request.getResetTime()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(request.isResetTimeProvided()).isFalse();
    }

    @Test
    void explicitNullAndValueAreBothMarkedAsProvided() throws Exception {
        ResetPolicyRequest nullRequest = objectMapper.readValue(
                "{\"name\":\"daily\",\"resetType\":\"DAILY\",\"resetTime\":null,\"timezone\":\"UTC\",\"active\":true}",
                ResetPolicyRequest.class);
        ResetPolicyRequest valueRequest = objectMapper.readValue(
                "{\"name\":\"daily\",\"resetType\":\"DAILY\",\"resetTime\":\"05:00\",\"timezone\":\"UTC\",\"active\":true}",
                ResetPolicyRequest.class);

        assertThat(nullRequest.getResetTime()).isNull();
        assertThat(nullRequest.isResetTimeProvided()).isTrue();
        assertThat(valueRequest.getResetTime()).isEqualTo(LocalTime.of(5, 0));
        assertThat(valueRequest.isResetTimeProvided()).isTrue();
    }

    @Test
    void directConstructorTreatsNullAsExplicitNullLikeJson() {
        ResetPolicyRequest request = new ResetPolicyRequest(
                "daily", com.ilque.domain.resetpolicy.entity.ResetType.DAILY,
                null, "UTC", null, true, null, null);

        assertThat(request.getResetTime()).isNull();
        assertThat(request.isResetTimeProvided()).isTrue();
        assertThat(request.isWeekStartDayProvided()).isTrue();
        assertThat(request.isIntervalProvided()).isTrue();
        assertThat(request.isIntervalUnitProvided()).isTrue();
    }
}
