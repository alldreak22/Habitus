package com.habitus.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.DailyHabitPlan;

public interface DailyHabitPlanRepository extends JpaRepository<DailyHabitPlan, Long> {
    List<DailyHabitPlan> findByDailyEntryIdOrderByCreatedAtAsc(Long dailyEntryId);

    Optional<DailyHabitPlan> findByDailyEntryIdAndHabitId(Long dailyEntryId, Long habitId);

    boolean existsByDailyEntryIdAndHabitId(Long dailyEntryId, Long habitId);

    void deleteByDailyEntryIdAndHabitId(Long dailyEntryId, Long habitId);
}
