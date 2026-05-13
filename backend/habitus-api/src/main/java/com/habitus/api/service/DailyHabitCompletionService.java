package com.habitus.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.DailyHabitCompletionRequest;
import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.entity.DailyEntry;
import com.habitus.api.entity.DailyHabitCompletion;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.User;
import com.habitus.api.exception.ApiException;
import com.habitus.api.exception.NotFoundException;
import com.habitus.api.mapper.ApiMapper;
import com.habitus.api.repository.DailyHabitCompletionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyHabitCompletionService {

    private final DailyEntryService dailyEntryService;
    private final HabitService habitService;
    private final DailyHabitCompletionRepository completionRepository;
    private final ApiMapper mapper;

    @Transactional
    public DailyHabitCompletionResponse create(User user, Long entryId, DailyHabitCompletionRequest request) {
        DailyEntry entry = dailyEntryService.findUserEntry(user, entryId);
        Habit habit = habitService.findUserHabit(user, request.habitId());

        DailyHabitCompletion completion = completionRepository.findByDailyEntryIdAndHabitId(entry.getId(), habit.getId())
            .orElseGet(DailyHabitCompletion::new);
        completion.setDailyEntry(entry);
        completion.setHabit(habit);
        completion.setCompleted(request.completed());
        completion.setNotes(request.notes());

        return mapper.toDailyHabitCompletionResponse(completionRepository.save(completion));
    }

    @Transactional(readOnly = true)
    public List<DailyHabitCompletionResponse> list(User user, Long entryId) {
        DailyEntry entry = dailyEntryService.findUserEntry(user, entryId);
        return completionRepository.findByDailyEntryIdOrderByIdAsc(entry.getId())
            .stream()
            .map(mapper::toDailyHabitCompletionResponse)
            .toList();
    }

    @Transactional
    public DailyHabitCompletionResponse update(User user, Long entryId, Long habitId, DailyHabitCompletionRequest request) {
        DailyEntry entry = dailyEntryService.findUserEntry(user, entryId);
        habitService.findUserHabit(user, habitId);

        if (!habitId.equals(request.habitId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Habit in path and body must match");
        }

        DailyHabitCompletion completion = completionRepository.findByDailyEntryIdAndHabitId(entry.getId(), habitId)
            .orElseThrow(() -> new NotFoundException("Completed habit not found"));
        completion.setCompleted(request.completed());
        completion.setNotes(request.notes());

        return mapper.toDailyHabitCompletionResponse(completionRepository.save(completion));
    }
}
