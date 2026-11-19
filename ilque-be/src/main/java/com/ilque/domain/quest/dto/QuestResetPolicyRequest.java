package com.ilque.domain.quest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.resetpolicy.validation.ValidZoneId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** 반복 Quest 생성 시 함께 만들 수 있는 ResetPolicy 설정입니다. */
@Getter
@Setter
public class QuestResetPolicyRequest {
    @NotBlank private String name;
    @NotNull private ResetType resetType;
    private LocalTime resetTime = LocalTime.MIDNIGHT;
    @NotBlank @ValidZoneId private String timezone;
    private DayOfWeek weekStartDay;
    @Min(1) private Integer interval;
    private IntervalUnit intervalUnit;

    private boolean resetTimeProvided;
    private boolean weekStartDayProvided;
    private boolean intervalProvided;
    private boolean intervalUnitProvided;

    public QuestResetPolicyRequest() { }

    @JsonSetter(value = "resetTime", nulls = Nulls.SET)
    public void setResetTime(LocalTime value) { resetTimeProvided = true; resetTime = value; }

    @JsonSetter(value = "weekStartDay", nulls = Nulls.SET)
    public void setWeekStartDay(DayOfWeek value) { weekStartDayProvided = true; weekStartDay = value; }

    @JsonSetter(value = "interval", nulls = Nulls.SET)
    public void setInterval(Integer value) { intervalProvided = true; interval = value; }

    @JsonSetter(value = "intervalUnit", nulls = Nulls.SET)
    public void setIntervalUnit(IntervalUnit value) { intervalUnitProvided = true; intervalUnit = value; }

    @JsonIgnore public boolean isResetTimeProvided() { return resetTimeProvided; }
    @JsonIgnore public boolean isWeekStartDayProvided() { return weekStartDayProvided; }
    @JsonIgnore public boolean isIntervalProvided() { return intervalProvided; }
    @JsonIgnore public boolean isIntervalUnitProvided() { return intervalUnitProvided; }
}
