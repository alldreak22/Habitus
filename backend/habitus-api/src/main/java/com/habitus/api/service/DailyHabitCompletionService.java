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
    public DailyHabitCompletionResponse criar(User user, Long entryId, DailyHabitCompletionRequest requisicao) {
        DailyEntry entry = dailyEntryService.buscarEntradaDoUsuario(user, entryId);
        Habit habit = habitService.buscarHabitoDoUsuario(user, requisicao.habitId());

        DailyHabitCompletion completion = completionRepository.findByDailyEntryIdAndHabitId(entry.getId(), habit.getId())
            .orElseGet(DailyHabitCompletion::new);
        completion.setDailyEntry(entry);
        completion.setHabit(habit);
        completion.setCompleted(requisicao.completed());
        completion.setNotes(requisicao.notes());

        return mapper.toDailyHabitCompletionResponse(completionRepository.save(completion));
    }

    @Transactional(readOnly = true)
    public List<DailyHabitCompletionResponse> listar(User user, Long entryId) {
        DailyEntry entry = dailyEntryService.buscarEntradaDoUsuario(user, entryId);
        return completionRepository.findByDailyEntryIdOrderByIdAsc(entry.getId())
            .stream()
            .map(mapper::toDailyHabitCompletionResponse)
            .toList();
    }

    @Transactional
    public DailyHabitCompletionResponse atualizar(User user, Long entryId, Long habitId, DailyHabitCompletionRequest requisicao) {
        DailyEntry entry = dailyEntryService.buscarEntradaDoUsuario(user, entryId);
        habitService.buscarHabitoDoUsuario(user, habitId);

        if (!habitId.equals(requisicao.habitId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Hábito da rota e do corpo devem ser iguais");
        }

        DailyHabitCompletion completion = completionRepository.findByDailyEntryIdAndHabitId(entry.getId(), habitId)
            .orElseThrow(() -> new NotFoundException("Hábito concluído não encontrado"));
        completion.setCompleted(requisicao.completed());
        completion.setNotes(requisicao.notes());

        return mapper.toDailyHabitCompletionResponse(completionRepository.save(completion));
    }
}
