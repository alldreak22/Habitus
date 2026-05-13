package com.habitus.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.DailyHabitCompletion;

public interface DailyHabitCompletionRepository extends JpaRepository<DailyHabitCompletion, Long> {
    List<DailyHabitCompletion> findByDailyEntryIdOrderByIdAsc(Long dailyEntryId);

    List<DailyHabitCompletion> findByHabitIdAndDailyEntryUserIdOrderByDailyEntryEntryDateDesc(Long habitId, Long userId);

    Optional<DailyHabitCompletion> findByDailyEntryIdAndHabitId(Long dailyEntryId, Long habitId);
}
