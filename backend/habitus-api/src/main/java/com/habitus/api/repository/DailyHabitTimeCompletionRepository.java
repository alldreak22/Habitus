package com.habitus.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.DailyHabitTimeCompletion;

public interface DailyHabitTimeCompletionRepository extends JpaRepository<DailyHabitTimeCompletion, Long> {
}
