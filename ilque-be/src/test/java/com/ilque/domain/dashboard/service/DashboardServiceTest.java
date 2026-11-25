package com.ilque.domain.dashboard.service;

import com.ilque.domain.dashboard.dto.DashboardResponse;
import com.ilque.domain.exp.service.ExpLevelService;
import com.ilque.domain.quest.entity.Quest;
import com.ilque.domain.quest.entity.RecurrenceType;
import com.ilque.domain.quest.repository.QuestRepository;
import com.ilque.domain.resetpolicy.calculator.ResetBoundaryCalculator;
import com.ilque.domain.resetpolicy.entity.ResetPolicy;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.entity.UserStatisticsSummary;
import com.ilque.domain.user.repository.UserStatisticsSummaryRepository;
import com.ilque.domain.user.service.UserService;
import com.ilque.global.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserStatisticsSummaryRepository summaryRepository;
    @Mock
    private QuestRepository questRepository;

    private final ExpLevelService expLevelService = new ExpLevelService();
    private final ResetBoundaryCalculator resetBoundaryCalculator = new ResetBoundaryCalculator();

    @Test
    void returnsProgressStreakAndNearestReset() {
        User user = User.builder().userId(1L).totalExp(250L).level(2).build();
        UserStatisticsSummary summary = mock(UserStatisticsSummary.class);
        Quest quest = mock(Quest.class);
        ResetPolicy policy = dailyPolicy("UTC", LocalTime.of(5, 0));
        Instant createdAt = Instant.parse("2026-11-24T06:00:00Z");
        Instant now = Instant.parse("2026-11-25T06:00:00Z");

        given(userService.findLocalUser()).willReturn(Optional.of(user));
        given(summaryRepository.findByUser_UserId(1L)).willReturn(Optional.of(summary));
        given(summary.getCurrentStreak()).willReturn(5);
        given(summary.getLongestStreak()).willReturn(12);
        given(questRepository.findAllByUser_UserIdAndActiveTrue(1L)).willReturn(List.of(quest));
        given(quest.getRecurrenceType()).willReturn(RecurrenceType.DAILY);
        given(quest.getResetPolicy()).willReturn(policy);
        given(quest.getCreatedAt()).willReturn(createdAt);

        DashboardResponse response = service().findDashboard(now);

        assertThat(response.totalExp()).isEqualTo(250L);
        assertThat(response.level()).isEqualTo(2);
        assertThat(response.currentLevelExp()).isEqualTo(150L);
        assertThat(response.nextLevelRequiredExp()).isEqualTo(150L);
        assertThat(response.currentStreak()).isEqualTo(5);
        assertThat(response.longestStreak()).isEqualTo(12);
        assertThat(response.nextResetAt()).isEqualTo(Instant.parse("2026-11-26T05:00:00Z"));
    }

    @Test
    void usesZeroStreakAndNullResetWhenSummaryOrRecurringQuestIsAbsent() {
        User user = User.builder().userId(1L).totalExp(0L).level(1).build();
        Quest onceQuest = mock(Quest.class);

        given(userService.findLocalUser()).willReturn(Optional.of(user));
        given(summaryRepository.findByUser_UserId(1L)).willReturn(Optional.empty());
        given(questRepository.findAllByUser_UserIdAndActiveTrue(1L)).willReturn(List.of(onceQuest));
        given(onceQuest.getRecurrenceType()).willReturn(RecurrenceType.ONCE);

        DashboardResponse response = service().findDashboard(Instant.parse("2026-11-25T06:00:00Z"));

        assertThat(response.currentStreak()).isZero();
        assertThat(response.longestStreak()).isZero();
        assertThat(response.nextResetAt()).isNull();
    }

    @Test
    void ignoresQuestWithInactiveResetPolicy() {
        User user = User.builder().userId(1L).totalExp(0L).level(1).build();
        Quest quest = mock(Quest.class);
        ResetPolicy policy = ResetPolicy.builder().name("inactive").resetType(ResetType.DAILY)
                .resetTime(LocalTime.MIDNIGHT).timezone("UTC").active(false).build();

        given(userService.findLocalUser()).willReturn(Optional.of(user));
        given(summaryRepository.findByUser_UserId(1L)).willReturn(Optional.empty());
        given(questRepository.findAllByUser_UserIdAndActiveTrue(1L)).willReturn(List.of(quest));
        given(quest.getRecurrenceType()).willReturn(RecurrenceType.DAILY);
        given(quest.getResetPolicy()).willReturn(policy);

        assertThat(service().findDashboard(Instant.parse("2026-11-25T06:00:00Z")).nextResetAt())
                .isNull();
    }

    @Test
    void failsWhenLocalUserIsMissing() {
        given(userService.findLocalUser()).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().findDashboard(Instant.parse("2026-11-25T06:00:00Z")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private DashboardService service() {
        return new DashboardService(userService, summaryRepository, questRepository,
                expLevelService, resetBoundaryCalculator);
    }

    private ResetPolicy dailyPolicy(String timezone, LocalTime resetTime) {
        return ResetPolicy.builder().name("daily").resetType(ResetType.DAILY)
                .resetTime(resetTime).timezone(timezone).active(true).build();
    }
}
