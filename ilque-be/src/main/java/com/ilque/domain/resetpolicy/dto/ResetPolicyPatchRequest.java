package com.ilque.domain.resetpolicy.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.ilque.domain.resetpolicy.entity.IntervalUnit;
import com.ilque.domain.resetpolicy.entity.ResetType;
import com.ilque.domain.resetpolicy.validation.ValidZoneId;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
public class ResetPolicyPatchRequest {
    private String name;
    private ResetType resetType;
    private LocalTime resetTime;
    @ValidZoneId
    private String timezone;
    private DayOfWeek weekStartDay;
    private Boolean active;
    @Min(1)
    private Integer interval;
    private IntervalUnit intervalUnit;
    private boolean nameProvided;
    private boolean resetTypeProvided;
    private boolean resetTimeProvided;
    private boolean timezoneProvided;
    private boolean weekStartDayProvided;
    private boolean activeProvided;
    private boolean intervalProvided;
    private boolean intervalUnitProvided;

    @JsonSetter(value = "name", nulls = Nulls.SET)
    public void setName(String value) { nameProvided = true; name = value; }
    @JsonSetter(value = "resetType", nulls = Nulls.SET)
    public void setResetType(ResetType value) { resetTypeProvided = true; resetType = value; }
    @JsonSetter(value = "resetTime", nulls = Nulls.SET)
    public void setResetTime(LocalTime value) { resetTimeProvided = true; resetTime = value; }
    @JsonSetter(value = "timezone", nulls = Nulls.SET)
    public void setTimezone(String value) { timezoneProvided = true; timezone = value; }
    @JsonSetter(value = "weekStartDay", nulls = Nulls.SET)
    public void setWeekStartDay(DayOfWeek value) { weekStartDayProvided = true; weekStartDay = value; }
    @JsonSetter(value = "active", nulls = Nulls.SET)
    public void setActive(Boolean value) { activeProvided = true; active = value; }
    @JsonSetter(value = "interval", nulls = Nulls.SET)
    public void setInterval(Integer value) { intervalProvided = true; interval = value; }
    @JsonSetter(value = "intervalUnit", nulls = Nulls.SET)
    public void setIntervalUnit(IntervalUnit value) { intervalUnitProvided = true; intervalUnit = value; }
}
