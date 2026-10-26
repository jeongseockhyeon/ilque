package com.ilque.domain.resetpolicy.service;

import com.ilque.domain.resetpolicy.dto.ResetPolicyRequest;
import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.resetpolicy.repository.ResetPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ResetPolicyServiceTest {
    @Mock
    private ResetPolicyRepository repository;

    @Test
    void createsValidPolicy() {
        ResetPolicyService service = new ResetPolicyService(repository);
        ResetPolicy saved = ResetPolicy.builder().resetPolicyId(1L).name("daily")
                .resetType(ResetType.DAILY).resetTime(LocalTime.of(5, 0)).timezone("UTC")
                .active(true).build();
        given(repository.save(any())).willReturn(saved);

        ResetPolicy result = service.create(request(ResetType.DAILY, null, null));

        assertThat(result).isSameAs(saved);
        then(repository).should().save(any(ResetPolicy.class));
    }

    @Test
    void patchesOnlyProvidedFields() {
        ResetPolicyService service = new ResetPolicyService(repository);
        ResetPolicy current = ResetPolicy.builder().resetPolicyId(1L).name("old")
                .resetType(ResetType.DAILY).resetTime(LocalTime.of(5, 0)).timezone("UTC")
                .active(true).build();
        given(repository.findById(1L)).willReturn(java.util.Optional.of(current));
        com.ilque.domain.resetpolicy.dto.ResetPolicyPatchRequest patch =
                new com.ilque.domain.resetpolicy.dto.ResetPolicyPatchRequest();
        patch.setName("new");

        ResetPolicy result = service.patch(1L, patch);

        assertThat(result.getName()).isEqualTo("new");
        assertThat(result.getResetTime()).isEqualTo(LocalTime.of(5, 0));
        assertThat(result.getTimezone()).isEqualTo("UTC");
    }

    @Test
    void dailyRejectsExplicitCustomFields() {
        ResetPolicyRequest request = request(ResetType.DAILY, null, null);
        request.setInterval(1);
        ResetPolicyService service = new ResetPolicyService(repository);

        assertThatThrownBy(() -> service.create(request)).hasMessageContaining("CUSTOM 전용");
        verifyNoInteractions(repository);
    }

    @Test
    void weeklyRequiresWeekStartAndRejectsCustomFields() {
        ResetPolicyService service = new ResetPolicyService(repository);
        ResetPolicyRequest missingWeekStart = request(ResetType.WEEKLY, null, null);
        assertThatThrownBy(() -> service.create(missingWeekStart)).isInstanceOf(RuntimeException.class);
        ResetPolicyRequest withCustom = request(ResetType.WEEKLY, 1, IntervalUnit.WEEK);
        assertThatThrownBy(() -> service.create(withCustom)).hasMessageContaining("CUSTOM 전용");
        verifyNoInteractions(repository);
    }

    @Test
    void customDayRejectsExplicitWeekStart() {
        ResetPolicyRequest request = request(ResetType.CUSTOM, 1, IntervalUnit.DAY);
        request.setWeekStartDay(java.time.DayOfWeek.MONDAY);
        ResetPolicyService service = new ResetPolicyService(repository);

        assertThatThrownBy(() -> service.create(request)).hasMessageContaining("weekStartDay");
        verifyNoInteractions(repository);
    }

    private ResetPolicyRequest request(ResetType type, Integer interval, IntervalUnit unit) {
        ResetPolicyRequest request = new ResetPolicyRequest();
        request.setName("test");
        request.setResetType(type);
        request.setResetTime(LocalTime.MIDNIGHT);
        request.setTimezone("UTC");
        request.setActive(true);
        if (interval != null) request.setInterval(interval);
        if (unit != null) request.setIntervalUnit(unit);
        return request;
    }
}
