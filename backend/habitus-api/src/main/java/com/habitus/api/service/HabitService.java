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
    public HabitResponse criar(User user, HabitRequest requisicao) {
        Habit habit = new Habit();
        habit.setUser(user);
        aplicarRequisicao(habit, requisicao);
        habit.setActive(true);
        return mapper.toHabitResponse(habitRepository.save(habit));
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> listar(User user) {
        return habitRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(mapper::toHabitResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse buscar(User user, Long id) {
        return mapper.toHabitResponse(buscarHabitoDoUsuario(user, id));
    }

    @Transactional
    public HabitResponse atualizar(User user, Long id, HabitRequest requisicao) {
        Habit habit = buscarHabitoDoUsuario(user, id);
        aplicarRequisicao(habit, requisicao);
        return mapper.toHabitResponse(habitRepository.save(habit));
    }

    @Transactional
    public void desativar(User user, Long id) {
        Habit habit = buscarHabitoDoUsuario(user, id);
        habit.setActive(false);
        habitRepository.save(habit);
    }

    @Transactional(readOnly = true)
    public List<DailyHabitCompletionResponse> historico(User user, Long habitId) {
        buscarHabitoDoUsuario(user, habitId);
        return completionRepository.findByHabitIdAndDailyEntryUserIdOrderByDailyEntryEntryDateDesc(habitId, user.getId())
            .stream()
            .map(mapper::toDailyHabitCompletionResponse)
            .toList();
    }

    Habit buscarHabitoDoUsuario(User user, Long habitId) {
        return habitRepository.findByIdAndUserId(habitId, user.getId())
            .orElseThrow(() -> new NotFoundException("Hábito não encontrado"));
    }

    private void aplicarRequisicao(Habit habit, HabitRequest requisicao) {
        habit.setName(requisicao.name().trim());
        habit.setDescription(requisicao.description());
        habit.setTargetFrequency(requisicao.targetFrequency().trim().toUpperCase());
        habit.setTimesPerDay(requisicao.timesPerDay());
        habit.setSuggestedTimes(requisicao.suggestedTimes());
    }
}
