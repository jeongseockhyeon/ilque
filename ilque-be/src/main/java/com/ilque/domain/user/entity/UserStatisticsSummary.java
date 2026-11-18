package com.ilque.domain.user.entity;

import com.ilque.global.base.Base;
import com.ilque.global.error.BadRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_statistics_summary")
public class UserStatisticsSummary extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userStatisticsSummaryId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Long longTermMissedCount = 0L;

    private UserStatisticsSummary(User user) {
        if (user == null) {
            throw new BadRequestException("user는 필수입니다.");
        }
        this.user = user;
        this.longTermMissedCount = 0L;
    }

    public static UserStatisticsSummary create(User user) {
        return new UserStatisticsSummary(user);
    }

    public void addLongTermMissedCount(long count) {
        if (count < 0) {
            throw new BadRequestException("longTermMissedCount 증분은 0 이상이어야 합니다.");
        }
        this.longTermMissedCount += count;
    }
}
