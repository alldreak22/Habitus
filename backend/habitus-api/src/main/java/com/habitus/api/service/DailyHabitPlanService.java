package com.habitus.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.DailyHabitPlanRequest;
import com.habitus.api.dto.response.DailyHabitPlanResponse;
import com.habitus.api.entity.DailyEntry;
import com.habitus.api.entity.DailyHabitPlan;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.User;
import com.habitus.api.exception.NotFoundException;
import com.habitus.api.mapper.ApiMapper;
import com.habitus.api.repository.DailyHabitPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyHabitPlanService {

    private final DailyEntryService dailyEntryService;
    private final HabitService habitService;
    private final DailyHabitPlanRepository planRepository;
    private final ApiMapper mapper;

    @Transactional
    public DailyHabitPlanResponse criar(User user, Long entryId, DailyHabitPlanRequest requisicao) {
        DailyEntry entry = dailyEntryService.buscarEntradaDoUsuario(user, entryId);
        Habit habit = habitService.buscarHabitoDoUsuario(user, requisicao.habitId());

        DailyHabitPlan plan = planRepository.findByDailyEntryIdAndHabitId(entry.getId(), habit.getId())
            .orElseGet(DailyHabitPlan::new);
        plan.setDailyEntry(entry);
        plan.setHabit(habit);
        plan.setPlanned(true);

        return mapper.toDailyHabitPlanResponse(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public List<DailyHabitPlanResponse> listar(User user, Long entryId) {
        DailyEntry entry = dailyEntryService.buscarEntradaDoUsuario(user, entryId);
        return planRepository.findByDailyEntryIdOrderByCreatedAtAsc(entry.getId())
            .stream()
            .map(mapper::toDailyHabitPlanResponse)
            .toList();
    }

    @Transactional
    public void excluir(User user, Long entryId, Long habitId) {
        DailyEntry entry = dailyEntryService.buscarEntradaDoUsuario(user, entryId);
        habitService.buscarHabitoDoUsuario(user, habitId);
        if (!planRepository.existsByDailyEntryIdAndHabitId(entry.getId(), habitId)) {
            throw new NotFoundException("H\u00e1bito planejado n\u00e3o encontrado");
        }
        planRepository.deleteByDailyEntryIdAndHabitId(entry.getId(), habitId);
    }
}
