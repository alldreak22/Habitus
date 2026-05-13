package com.habitus.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.Habit;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Habit> findByIdAndUserId(Long id, Long userId);
}
