package com.habitus.api.mapper;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.habitus.api.dto.response.DailyEntryResponse;
import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.dto.response.DailyHabitPlanResponse;
import com.habitus.api.dto.response.HabitResponse;
import com.habitus.api.dto.response.UserResponse;
import com.habitus.api.entity.DailyEntry;
import com.habitus.api.entity.DailyHabitCompletion;
import com.habitus.api.entity.DailyHabitPlan;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.HabitFrequencyDay;
import com.habitus.api.entity.HabitReminderTime;
import com.habitus.api.entity.User;

@Component
public class ApiMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getNick(), user.getPicture());
    }

    public HabitResponse toHabitResponse(Habit habit) {
        List<String> reminderTimes = habit.getReminderTimes()
            .stream()
            .map(HabitReminderTime::getReminderTime)
            .sorted()
            .map(String::valueOf)
            .toList();
        List<Integer> frequencyDays = habit.getFrequencyDays()
            .stream()
            .map(HabitFrequencyDay::getDayOfWeek)
            .sorted()
            .toList();

        return new HabitResponse(
            habit.getId(),
            habit.getTitle(),
            habit.getTitle(),
            habit.getIcon(),
            habit.getColor(),
            habit.getDescription(),
            legacyTargetFrequency(habit.getFrequencyType()),
            Math.max(1, reminderTimes.size()),
            String.join(",", reminderTimes),
            "ACTIVE".equals(habit.getStatus()),
            habit.getReminder(),
            habit.getFrequencyType(),
            habit.getStatus(),
            reminderTimes,
            frequencyDays
        );
    }

    public DailyEntryResponse toDailyEntryResponse(DailyEntry entry) {
        List<DailyHabitPlanResponse> plannedHabits = entry.getPlannedHabits()
            .stream()
            .sorted(Comparator.comparing(DailyHabitPlan::getId))
            .map(this::toDailyHabitPlanResponse)
            .toList();
        List<DailyHabitCompletionResponse> completedHabits = entry.getCompletedHabits()
            .stream()
            .sorted(Comparator.comparing(DailyHabitCompletion::getId))
            .map(this::toDailyHabitCompletionResponse)
            .toList();

        return new DailyEntryResponse(
            entry.getId(),
            entry.getEntryDate(),
            entry.getActivityDescription(),
            entry.getPlanningNotes(),
            plannedHabits,
            completedHabits
        );
    }

    public DailyHabitPlanResponse toDailyHabitPlanResponse(DailyHabitPlan plan) {
        return new DailyHabitPlanResponse(
            plan.getHabit().getId(),
            plan.getHabit().getTitle(),
            plan.getPlanned()
        );
    }

    public DailyHabitCompletionResponse toDailyHabitCompletionResponse(DailyHabitCompletion completion) {
        return new DailyHabitCompletionResponse(
            completion.getHabit().getId(),
            completion.getHabit().getTitle(),
            completion.getCompleted(),
            completion.getCompletedAt(),
            completion.getNotes()
        );
    }

    private String legacyTargetFrequency(String frequencyType) {
        if ("EVERY_DAY".equals(frequencyType)) {
            return "DAILY";
        }
        return frequencyType;
    }
}
