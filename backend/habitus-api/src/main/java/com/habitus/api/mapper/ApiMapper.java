package com.habitus.api.mapper;

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
import com.habitus.api.entity.User;

@Component
public class ApiMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public HabitResponse toHabitResponse(Habit habit) {
        return new HabitResponse(
            habit.getId(),
            habit.getName(),
            habit.getDescription(),
            habit.getTargetFrequency(),
            habit.getTimesPerDay(),
            habit.getSuggestedTimes(),
            habit.getActive()
        );
    }

    public DailyEntryResponse toDailyEntryResponse(DailyEntry entry) {
        List<DailyHabitPlanResponse> plannedHabits = entry.getPlannedHabits()
            .stream()
            .map(this::toDailyHabitPlanResponse)
            .toList();
        List<DailyHabitCompletionResponse> completedHabits = entry.getCompletedHabits()
            .stream()
            .map(this::toDailyHabitCompletionResponse)
            .toList();

        return new DailyEntryResponse(
            entry.getId(),
            entry.getEntryDate(),
            entry.getMarkdownContent(),
            entry.getPlanningNotes(),
            plannedHabits,
            completedHabits
        );
    }

    public DailyHabitPlanResponse toDailyHabitPlanResponse(DailyHabitPlan plan) {
        return new DailyHabitPlanResponse(
            plan.getHabit().getId(),
            plan.getHabit().getName(),
            plan.getPlanned()
        );
    }

    public DailyHabitCompletionResponse toDailyHabitCompletionResponse(DailyHabitCompletion completion) {
        return new DailyHabitCompletionResponse(
            completion.getHabit().getId(),
            completion.getHabit().getName(),
            completion.getCompleted(),
            completion.getCompletedAt(),
            completion.getNotes()
        );
    }
}
