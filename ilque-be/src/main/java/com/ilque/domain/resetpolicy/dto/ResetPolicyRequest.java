package com.ilque.domain.resetpolicy.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.Nulls;
import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import com.ilque.domain.resetpolicy.validation.ValidZoneId;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class ResetPolicyRequest {

    @NotBlank
    private String name;
    @NotNull
    private ResetType resetType;
    private LocalTime resetTime = LocalTime.MIDNIGHT;
    @NotNull
    @ValidZoneId
    private String timezone;
    private DayOfWeek weekStartDay;
    @NotNull
    private Boolean active;
    @Min(1)
    private Integer interval;
    private IntervalUnit intervalUnit;

    private boolean resetTimeProvided;
    private boolean weekStartDayProvided;
    private boolean intervalProvided;
    private boolean intervalUnitProvided;

    public ResetPolicyRequest() {
    }

    @JsonIgnore
    public ResetPolicyRequest(String name, ResetType resetType, LocalTime resetTime, String timezone,
                              DayOfWeek weekStartDay, Boolean active, Integer interval,
                              IntervalUnit intervalUnit) {
        this.name = name;
        this.resetType = resetType;
        this.resetTime = resetTime;
        this.timezone = timezone;
        this.weekStartDay = weekStartDay;
        this.active = active;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.resetTimeProvided = true;
        this.weekStartDayProvided = true;
        this.intervalProvided = true;
        this.intervalUnitProvided = true;
    }

    @JsonSetter(value = "resetTime", nulls = Nulls.SET)
    public void setResetTime(LocalTime resetTime) {
        this.resetTimeProvided = true;
        this.resetTime = resetTime;
    }

    @JsonSetter(value = "weekStartDay", nulls = Nulls.SET)
    public void setWeekStartDay(DayOfWeek weekStartDay) {
        this.weekStartDayProvided = true;
        this.weekStartDay = weekStartDay;
    }

    @JsonSetter(value = "interval", nulls = Nulls.SET)
    public void setInterval(Integer interval) {
        this.intervalProvided = true;
        this.interval = interval;
    }

    @JsonSetter(value = "intervalUnit", nulls = Nulls.SET)
    public void setIntervalUnit(IntervalUnit intervalUnit) {
        this.intervalUnitProvided = true;
        this.intervalUnit = intervalUnit;
    }
}
