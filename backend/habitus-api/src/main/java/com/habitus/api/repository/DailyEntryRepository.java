package com.habitus.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.DailyEntry;

public interface DailyEntryRepository extends JpaRepository<DailyEntry, Long> {
    Optional<DailyEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    List<DailyEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(Long userId, LocalDate start, LocalDate end);
}
