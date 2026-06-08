package com.habitus.api.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    name = "day_entry_habit_time_completions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"day_entry_habit_id", "completion_time"})
)
public class DailyHabitTimeCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_entry_habit_id", nullable = false)
    private DailyHabitPlan dailyHabitPlan;

    @Column(name = "completion_time", nullable = false)
    private LocalTime completionTime;

    @Column(nullable = false)
    private Boolean completed = false;

    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        updateCompletedAt();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
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
