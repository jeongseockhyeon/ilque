package com.ilque.domain.resetpolicy.entity;

import com.ilque.global.base.Base;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "reset_policy")
public class ResetPolicy extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resetPolicyId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResetType resetType;

    @Column(nullable = false)
    private LocalTime resetTime;

    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    private DayOfWeek weekStartDay;

    @Column(nullable = false)
    private Boolean active;

}
