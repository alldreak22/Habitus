package com.habitus.api.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.CalendarDayHabitSaveRequest;
import com.habitus.api.dto.request.CalendarDayHabitTimeSlotSaveRequest;
import com.habitus.api.dto.request.CalendarDaySaveRequest;
import com.habitus.api.dto.request.CalendarMonthRequest;
import com.habitus.api.dto.response.CalendarDayHabitResponse;
import com.habitus.api.dto.response.CalendarDayResponse;
import com.habitus.api.dto.response.CalendarHabitMarkerResponse;
import com.habitus.api.dto.response.CalendarHabitTimeSlotResponse;
import com.habitus.api.dto.response.CalendarMonthResponse;
import com.habitus.api.entity.DailyHabitTimeCompletion;
import com.habitus.api.entity.DailyEntry;
import com.habitus.api.entity.DailyHabitPlan;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.HabitFrequencyDay;
import com.habitus.api.entity.HabitReminderTime;
import com.habitus.api.entity.User;
import com.habitus.api.exception.ApiException;
import com.habitus.api.exception.NotFoundException;
import com.habitus.api.repository.DailyEntryRepository;
import com.habitus.api.repository.DailyHabitPlanRepository;
import com.habitus.api.repository.HabitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final HabitRepository habitRepository;
    private final DailyEntryRepository dailyEntryRepository;
    private final DailyHabitPlanRepository planRepository;

    @Transactional(readOnly = true)
    public CalendarMonthResponse buscarMes(User user, CalendarMonthRequest requisicao) {
        YearMonth month = YearMonth.of(requisicao.year(), requisicao.month());
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Habit> activeHabits = habitRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId());
        List<DailyEntry> entries = dailyEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(
            user.getId(),
            start,
            end
        );
        Map<LocalDate, DailyEntry> entriesByDate = entries.stream()
            .collect(Collectors.toMap(DailyEntry::getEntryDate, Function.identity()));
        Map<Long, List<DailyHabitPlan>> plansByEntryId = buscarPlanosPorEntrada(entries);

        List<CalendarDayResponse> days = new ArrayList<>();
        for (int day = 1; day <= month.lengthOfMonth(); day += 1) {
            LocalDate date = month.atDay(day);
            DailyEntry entry = entriesByDate.get(date);
            List<DailyHabitPlan> plans = entry == null
                ? List.of()
                : plansByEntryId.getOrDefault(entry.getId(), List.of());
            days.add(montarDia(date, entry, activeHabits, plans));
        }

        return new CalendarMonthResponse(requisicao.year(), requisicao.month(), days);
    }

    @Transactional(readOnly = true)
    public CalendarDayResponse buscarDia(User user, LocalDate date) {
        List<Habit> activeHabits = habitRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId());
        DailyEntry entry = dailyEntryRepository.findByUserIdAndEntryDate(user.getId(), date).orElse(null);
        List<DailyHabitPlan> plans = entry == null
            ? List.of()
            : planRepository.findByDailyEntryIdOrderByCreatedAtAsc(entry.getId());

        return montarDia(date, entry, activeHabits, plans);
    }

    @Transactional
    public CalendarDayResponse salvarDia(User user, LocalDate date, CalendarDaySaveRequest requisicao) {
        DailyEntry entry = dailyEntryRepository.findByUserIdAndEntryDate(user.getId(), date)
            .orElseGet(() -> {
                DailyEntry novaEntrada = new DailyEntry();
                novaEntrada.setUser(user);
                novaEntrada.setEntryDate(date);
                return novaEntrada;
            });
        entry.setActivityDescription(requisicao.description());
        DailyEntry savedEntry = dailyEntryRepository.save(entry);

        Map<Long, RequestedHabit> requestedHabits = normalizarHabitos(requisicao.habits());
        List<DailyHabitPlan> currentPlans = planRepository.findByDailyEntryIdOrderByCreatedAtAsc(savedEntry.getId());
        Map<Long, DailyHabitPlan> currentPlansByHabitId = currentPlans.stream()
            .collect(Collectors.toMap((plan) -> plan.getHabit().getId(), Function.identity()));

        for (DailyHabitPlan currentPlan : currentPlans) {
            Long habitId = currentPlan.getHabit().getId();
            if (!requestedHabits.containsKey(habitId)) {
                planRepository.delete(currentPlan);
            }
        }

        for (RequestedHabit requestedHabit : requestedHabits.values()) {
            Habit habit = habitRepository.findByIdAndUserId(requestedHabit.habitId(), user.getId())
                .filter((currentHabit) -> Boolean.TRUE.equals(currentHabit.getActive()))
                .orElseThrow(() -> new NotFoundException("Habito nao encontrado"));
            DailyHabitPlan plan = currentPlansByHabitId.getOrDefault(habit.getId(), new DailyHabitPlan());
            plan.setDailyEntry(savedEntry);
            plan.setHabit(habit);
            plan.setPlanned(true);
            aplicarConclusoesDeHorario(plan, habit, requestedHabit);
            plan.setCompleted(completedAgregado(requestedHabit));
            plan.setCompletedAt(Boolean.TRUE.equals(plan.getCompleted()) ? LocalDateTime.now() : null);
            planRepository.save(plan);
        }

        List<Habit> activeHabits = habitRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId());
        List<DailyHabitPlan> updatedPlans = planRepository.findByDailyEntryIdOrderByCreatedAtAsc(savedEntry.getId());
        return montarDia(date, savedEntry, activeHabits, updatedPlans);
    }

    private Map<Long, List<DailyHabitPlan>> buscarPlanosPorEntrada(List<DailyEntry> entries) {
        List<Long> entryIds = entries.stream().map(DailyEntry::getId).toList();
        if (entryIds.isEmpty()) {
            return Map.of();
        }

        return planRepository.findByDailyEntryIdInOrderByCreatedAtAsc(entryIds)
            .stream()
            .collect(Collectors.groupingBy((plan) -> plan.getDailyEntry().getId()));
    }

    private CalendarDayResponse montarDia(
        LocalDate date,
        DailyEntry entry,
        List<Habit> activeHabits,
        List<DailyHabitPlan> plans
    ) {
        List<CalendarDayHabitResponse> habits = entry == null
            ? montarHabitosAutomaticos(date, activeHabits)
            : montarHabitosManuais(plans, activeHabits);
        boolean completed = !habits.isEmpty() && habits.stream().allMatch((habit) -> Boolean.TRUE.equals(habit.completed()));
        List<CalendarHabitMarkerResponse> markers = habits.stream()
            .flatMap((habit) -> montarMarcadores(habit).stream())
            .toList();

        return new CalendarDayResponse(
            date,
            entry != null,
            entry == null ? null : entry.getId(),
            entry == null ? "" : entry.getActivityDescription(),
            completed,
            markers,
            habits
        );
    }

    private List<CalendarDayHabitResponse> montarHabitosAutomaticos(LocalDate date, List<Habit> activeHabits) {
        return activeHabits.stream()
            .filter((habit) -> habitoAconteceNoDia(habit, date))
            .map((habit) -> montarHabito(habit, false))
            .toList();
    }

    private List<CalendarDayHabitResponse> montarHabitosManuais(List<DailyHabitPlan> plans, List<Habit> activeHabits) {
        Map<Long, DailyHabitPlan> plansByHabitId = plans.stream()
            .filter((plan) -> Boolean.TRUE.equals(plan.getPlanned()))
            .filter((plan) -> Boolean.TRUE.equals(plan.getHabit().getActive()))
            .collect(Collectors.toMap(
                (plan) -> plan.getHabit().getId(),
                Function.identity(),
                (first, second) -> first,
                LinkedHashMap::new
            ));

        return activeHabits.stream()
            .map((habit) -> plansByHabitId.get(habit.getId()))
            .filter((plan) -> plan != null)
            .map(this::montarHabito)
            .toList();
    }

    private CalendarDayHabitResponse montarHabito(Habit habit, boolean completed) {
        List<CalendarHabitTimeSlotResponse> timeSlots = montarHorariosDoHabito(habit, Map.of());
        return new CalendarDayHabitResponse(
            habit.getId(),
            habit.getTitle(),
            habit.getIcon(),
            habit.getColor(),
            habit.getDescription(),
            completed,
            timeSlots
        );
    }

    private CalendarDayHabitResponse montarHabito(DailyHabitPlan plan) {
        Map<LocalTime, Boolean> completedByTime = plan.getTimeCompletions()
            .stream()
            .collect(Collectors.toMap(
                DailyHabitTimeCompletion::getCompletionTime,
                (completion) -> Boolean.TRUE.equals(completion.getCompleted()),
                (first, second) -> first,
                LinkedHashMap::new
            ));
        List<CalendarHabitTimeSlotResponse> timeSlots = montarHorariosDoHabito(plan.getHabit(), completedByTime);
        boolean completed = timeSlots.isEmpty()
            ? Boolean.TRUE.equals(plan.getCompleted())
            : timeSlots.stream().allMatch((slot) -> Boolean.TRUE.equals(slot.completed()));

        return new CalendarDayHabitResponse(
            plan.getHabit().getId(),
            plan.getHabit().getTitle(),
            plan.getHabit().getIcon(),
            plan.getHabit().getColor(),
            plan.getHabit().getDescription(),
            completed,
            timeSlots
        );
    }

    private List<CalendarHabitTimeSlotResponse> montarHorariosDoHabito(Habit habit, Map<LocalTime, Boolean> completedByTime) {
        return habit.getReminderTimes()
            .stream()
            .map(HabitReminderTime::getReminderTime)
            .sorted()
            .map((time) -> new CalendarHabitTimeSlotResponse(
                time.format(TIME_FORMAT),
                Boolean.TRUE.equals(completedByTime.get(time))
            ))
            .toList();
    }

    private List<CalendarHabitMarkerResponse> montarMarcadores(CalendarDayHabitResponse habit) {
        if (habit.timeSlots() == null || habit.timeSlots().isEmpty()) {
            return List.of(new CalendarHabitMarkerResponse(
                habit.id(),
                habit.name(),
                habit.color(),
                null,
                Boolean.TRUE.equals(habit.completed())
            ));
        }

        return habit.timeSlots()
            .stream()
            .map((timeSlot) -> new CalendarHabitMarkerResponse(
                habit.id(),
                habit.name(),
                habit.color(),
                timeSlot.time(),
                Boolean.TRUE.equals(timeSlot.completed())
            ))
            .toList();
    }

    private boolean habitoAconteceNoDia(Habit habit, LocalDate date) {
        String frequencyType = String.valueOf(habit.getFrequencyType()).trim().toUpperCase();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if ("EVERY_DAY".equals(frequencyType) || "DAILY".equals(frequencyType)) {
            return true;
        }

        if ("WEEKDAYS".equals(frequencyType)) {
            return dayOfWeek.getValue() >= 1 && dayOfWeek.getValue() <= 5;
        }

        if ("WEEKENDS".equals(frequencyType)) {
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }

        if ("CUSTOM".equals(frequencyType)) {
            int apiDayOfWeek = dayOfWeek.getValue();
            return habit.getFrequencyDays()
                .stream()
                .map(HabitFrequencyDay::getDayOfWeek)
                .anyMatch((day) -> day == apiDayOfWeek);
        }

        return false;
    }

    private Map<Long, RequestedHabit> normalizarHabitos(List<CalendarDayHabitSaveRequest> habits) {
        if (habits == null || habits.isEmpty()) {
            return Map.of();
        }

        Map<Long, RequestedHabit> normalized = new LinkedHashMap<>();
        for (CalendarDayHabitSaveRequest habit : habits) {
            normalized.putIfAbsent(
                habit.habitId(),
                new RequestedHabit(
                    habit.habitId(),
                    Boolean.TRUE.equals(habit.completed()),
                    normalizarHorarios(habit.timeSlots())
                )
            );
        }
        return normalized;
    }

    private List<RequestedTimeSlot> normalizarHorarios(List<CalendarDayHabitTimeSlotSaveRequest> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return List.of();
        }

        Map<LocalTime, RequestedTimeSlot> normalized = new LinkedHashMap<>();
        for (CalendarDayHabitTimeSlotSaveRequest timeSlot : timeSlots) {
            LocalTime time = parseTime(timeSlot.time());
            normalized.putIfAbsent(time, new RequestedTimeSlot(time, Boolean.TRUE.equals(timeSlot.completed())));
        }
        return new ArrayList<>(normalized.values());
    }

    private void aplicarConclusoesDeHorario(DailyHabitPlan plan, Habit habit, RequestedHabit requestedHabit) {
        List<RequestedTimeSlot> requestedTimeSlots = requestedHabit.timeSlots();
        Map<LocalTime, RequestedTimeSlot> requestedByTime = requestedTimeSlots.stream()
            .collect(Collectors.toMap(
                RequestedTimeSlot::time,
                Function.identity(),
                (first, second) -> first,
                LinkedHashMap::new
            ));
        plan.getTimeCompletions()
            .removeIf((completion) -> !requestedByTime.containsKey(completion.getCompletionTime()));

        if (requestedTimeSlots.isEmpty()) {
            return;
        }

        List<LocalTime> habitTimes = habit.getReminderTimes()
            .stream()
            .map(HabitReminderTime::getReminderTime)
            .toList();
        Map<LocalTime, DailyHabitTimeCompletion> currentByTime = plan.getTimeCompletions()
            .stream()
            .collect(Collectors.toMap(
                DailyHabitTimeCompletion::getCompletionTime,
                Function.identity(),
                (first, second) -> first,
                LinkedHashMap::new
            ));

        for (RequestedTimeSlot requestedTimeSlot : requestedTimeSlots) {
            if (!habitTimes.contains(requestedTimeSlot.time())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Horario nao pertence ao habito");
            }

            DailyHabitTimeCompletion completion = currentByTime.getOrDefault(
                requestedTimeSlot.time(),
                new DailyHabitTimeCompletion()
            );
            completion.setDailyHabitPlan(plan);
            completion.setCompletionTime(requestedTimeSlot.time());
            completion.setCompleted(requestedTimeSlot.completed());
            if (completion.getId() == null) {
                plan.getTimeCompletions().add(completion);
            }
        }
    }

    private boolean completedAgregado(RequestedHabit requestedHabit) {
        if (requestedHabit.timeSlots().isEmpty()) {
            return requestedHabit.completed();
        }
        return requestedHabit.timeSlots()
            .stream()
            .allMatch((timeSlot) -> Boolean.TRUE.equals(timeSlot.completed()));
    }

    private LocalTime parseTime(String time) {
        try {
            return LocalTime.parse(time.trim()).withSecond(0).withNano(0);
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Horario invalido: " + time);
        }
    }

    private record RequestedHabit(
        Long habitId,
        Boolean completed,
        List<RequestedTimeSlot> timeSlots
    ) {
    }

    private record RequestedTimeSlot(
        LocalTime time,
        Boolean completed
    ) {
    }

}
