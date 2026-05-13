package com.habitus.api.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.DailyEntry;

public interface DailyEntryRepository extends JpaRepository<DailyEntry, Long> {
    boolean existsByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    Optional<DailyEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    Optional<DailyEntry> findByIdAndUserId(Long id, Long userId);
}
