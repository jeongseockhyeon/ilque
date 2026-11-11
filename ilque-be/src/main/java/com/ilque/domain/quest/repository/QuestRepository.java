package com.ilque.domain.quest.repository;

import com.ilque.domain.quest.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findAllByActiveTrue();

    Optional<Quest> findByQuestIdAndActiveTrue(Long questId);
}
