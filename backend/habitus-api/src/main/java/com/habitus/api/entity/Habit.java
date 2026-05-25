package com.habitus.api.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "habits")
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name;

    private String icon;

    private String color;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean reminder = false;

    @Column(nullable = false)
    private String frequencyType = "EVERY_DAY";

    @Column(nullable = false)
    private String targetFrequency = "DAILY";

    @Column(nullable = false)
    private Integer timesPerDay = 1;

    private String suggestedTimes;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HabitReminderTime> reminderTimes = new ArrayList<>();

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HabitFrequencyDay> frequencyDays = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (reminder == null) {
            reminder = false;
        }
        if (frequencyType == null) {
            frequencyType = "EVERY_DAY";
        }
        if (targetFrequency == null || targetFrequency.isBlank()) {
            targetFrequency = "DAILY";
        }
        if (timesPerDay == null || timesPerDay < 1) {
            timesPerDay = 1;
        }
        if (name == null || name.isBlank()) {
            name = title;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (active == null) {
            active = "ACTIVE".equalsIgnoreCase(status);
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
