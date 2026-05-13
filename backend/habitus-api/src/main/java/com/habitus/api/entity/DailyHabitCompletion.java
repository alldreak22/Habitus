package com.habitus.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "daily_habit_completions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"daily_entry_id", "habit_id"})
)
public class DailyHabitCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_entry_id", nullable = false)
    private DailyEntry dailyEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(nullable = false)
    private Boolean completed;

    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    void prePersist() {
        updateCompletedAt();
    }

    @PreUpdate
    void preUpdate() {
        updateCompletedAt();
    }

    private void updateCompletedAt() {
        if (Boolean.TRUE.equals(completed) && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
        if (!Boolean.TRUE.equals(completed)) {
            completedAt = null;
        }
    }
}
