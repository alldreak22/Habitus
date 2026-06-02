package com.habitus.api.mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.dto.response.HabitResponse;
import com.habitus.api.dto.response.UserResponse;
import com.habitus.api.entity.DailyHabitCompletion;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.HabitFrequencyDay;
import com.habitus.api.entity.HabitReminderTime;
import com.habitus.api.entity.User;

@Component
public class ApiMapper {

    private static final DateTimeFormatter REMINDER_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getNick(),
            user.getPicture(),
            user.getCreatedAt()
        );
    }

    public HabitResponse toHabitResponse(Habit habit) {
        List<String> reminderTimes = habit.getReminderTimes()
            .stream()
            .map(HabitReminderTime::getReminderTime)
            .sorted()
            .map((time) -> time.format(REMINDER_TIME_FORMAT))
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
            habit.getActive() != null ? habit.getActive() : "ACTIVE".equals(habit.getStatus()),
            habit.getReminder(),
            habit.getFrequencyType(),
            habit.getStatus(),
            reminderTimes,
            frequencyDays
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
