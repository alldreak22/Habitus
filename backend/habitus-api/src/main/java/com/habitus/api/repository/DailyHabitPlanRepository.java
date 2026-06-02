package com.habitus.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.DailyHabitPlan;

public interface DailyHabitPlanRepository extends JpaRepository<DailyHabitPlan, Long> {
    List<DailyHabitPlan> findByDailyEntryIdOrderByCreatedAtAsc(Long dailyEntryId);

    List<DailyHabitPlan> findByDailyEntryIdInOrderByCreatedAtAsc(List<Long> dailyEntryIds);
}
