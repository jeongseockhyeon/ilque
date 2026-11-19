package com.ilque.domain.user.repository;

import com.ilque.domain.user.entity.UserStatisticsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatisticsSummaryRepository extends JpaRepository<UserStatisticsSummary, Long> {

    Optional<UserStatisticsSummary> findByUser_UserId(Long userId);
}
