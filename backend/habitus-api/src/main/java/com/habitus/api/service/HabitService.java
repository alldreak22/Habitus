package com.habitus.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.HabitRequest;
import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.dto.response.HabitResponse;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.User;
import com.habitus.api.exception.NotFoundException;
import com.habitus.api.mapper.ApiMapper;
import com.habitus.api.repository.DailyHabitCompletionRepository;
import com.habitus.api.repository.HabitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final DailyHabitCompletionRepository completionRepository;
    private final ApiMapper mapper;

    @Transactional
    public HabitResponse create(User user, HabitRequest request) {
        Habit habit = new Habit();
        habit.setUser(user);
        applyRequest(habit, request);
        habit.setActive(true);
        return mapper.toHabitResponse(habitRepository.save(habit));
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> list(User user) {
        return habitRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(mapper::toHabitResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse get(User user, Long id) {
        return mapper.toHabitResponse(findUserHabit(user, id));
    }

    @Transactional
    public HabitResponse update(User user, Long id, HabitRequest request) {
        Habit habit = findUserHabit(user, id);
        applyRequest(habit, request);
        return mapper.toHabitResponse(habitRepository.save(habit));
    }

    @Transactional
    public void deactivate(User user, Long id) {
        Habit habit = findUserHabit(user, id);
        habit.setActive(false);
        habitRepository.save(habit);
    }

    @Transactional(readOnly = true)
    public List<DailyHabitCompletionResponse> history(User user, Long habitId) {
        findUserHabit(user, habitId);
        return completionRepository.findByHabitIdAndDailyEntryUserIdOrderByDailyEntryEntryDateDesc(habitId, user.getId())
            .stream()
            .map(mapper::toDailyHabitCompletionResponse)
            .toList();
    }

    Habit findUserHabit(User user, Long habitId) {
        return habitRepository.findByIdAndUserId(habitId, user.getId())
            .orElseThrow(() -> new NotFoundException("Habit not found"));
    }

    private void applyRequest(Habit habit, HabitRequest request) {
        habit.setName(request.name().trim());
        habit.setDescription(request.description());
        habit.setTargetFrequency(request.targetFrequency().trim().toUpperCase());
        habit.setTimesPerDay(request.timesPerDay());
        habit.setSuggestedTimes(request.suggestedTimes());
    }
}
