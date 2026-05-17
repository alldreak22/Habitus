package com.habitus.api.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.HabitRequest;
import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.dto.response.HabitResponse;
import com.habitus.api.entity.Habit;
import com.habitus.api.entity.HabitFrequencyDay;
import com.habitus.api.entity.HabitReminderTime;
import com.habitus.api.entity.User;
import com.habitus.api.exception.ApiException;
import com.habitus.api.exception.NotFoundException;
import com.habitus.api.mapper.ApiMapper;
import com.habitus.api.repository.DailyHabitCompletionRepository;
import com.habitus.api.repository.HabitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitService {

    private static final List<String> VALID_FREQUENCY_TYPES = List.of("EVERY_DAY", "WEEKDAYS", "WEEKENDS", "CUSTOM");
    private static final List<String> VALID_STATUSES = List.of("ACTIVE", "INACTIVE", "ARCHIVED");

    private final HabitRepository habitRepository;
    private final DailyHabitCompletionRepository completionRepository;
    private final ApiMapper mapper;

    @Transactional
    public HabitResponse criar(User user, HabitRequest requisicao) {
        Habit habit = new Habit();
        habit.setUser(user);
        aplicarRequisicao(habit, requisicao);
        habit.setStatus("ACTIVE");
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
        habit.setStatus("INACTIVE");
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
            .orElseThrow(() -> new NotFoundException("H\u00e1bito n\u00e3o encontrado"));
    }

    private void aplicarRequisicao(Habit habit, HabitRequest requisicao) {
        String title = primeiroTexto(requisicao.title(), requisicao.name());
        if (title == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Titulo do habito e obrigatorio");
        }

        String frequencyType = primeiroTexto(requisicao.frequencyType(), requisicao.targetFrequency());
        if (frequencyType == null) {
            frequencyType = "EVERY_DAY";
        }
        frequencyType = normalizarFrequencyType(frequencyType);
        if (!VALID_FREQUENCY_TYPES.contains(frequencyType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de frequencia invalido");
        }

        String status = primeiroTexto(requisicao.status(), habit.getStatus());
        if (status == null) {
            status = "ACTIVE";
        }
        status = status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Status de habito invalido");
        }

        habit.setTitle(title);
        habit.setIcon(requisicao.icon());
        habit.setColor(requisicao.color());
        habit.setDescription(requisicao.description());
        habit.setReminder(Boolean.TRUE.equals(requisicao.reminder()));
        habit.setFrequencyType(frequencyType);
        habit.setStatus(status);

        aplicarHorarios(habit, requisicao.reminderTimes(), requisicao.suggestedTimes());
        aplicarDiasCustomizados(habit, requisicao.frequencyDays());
    }

    private void aplicarHorarios(Habit habit, List<String> reminderTimes, String suggestedTimes) {
        habit.getReminderTimes().clear();

        List<String> times = reminderTimes;
        if ((times == null || times.isEmpty()) && suggestedTimes != null && !suggestedTimes.isBlank()) {
            times = List.of(suggestedTimes.split(","));
        }
        if (times == null) {
            return;
        }

        for (String time : times) {
            if (time == null || time.isBlank()) {
                continue;
            }
            try {
                HabitReminderTime reminderTime = new HabitReminderTime();
                reminderTime.setHabit(habit);
                reminderTime.setReminderTime(LocalTime.parse(time.trim()));
                habit.getReminderTimes().add(reminderTime);
            } catch (DateTimeParseException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Horario de lembrete invalido: " + time);
            }
        }
    }

    private void aplicarDiasCustomizados(Habit habit, List<Integer> frequencyDays) {
        habit.getFrequencyDays().clear();
        if (frequencyDays == null) {
            return;
        }

        for (Integer day : frequencyDays.stream().distinct().toList()) {
            if (day == null || day < 1 || day > 7) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Dia da semana deve estar entre 1 e 7");
            }
            HabitFrequencyDay frequencyDay = new HabitFrequencyDay();
            frequencyDay.setHabit(habit);
            frequencyDay.setDayOfWeek(day);
            habit.getFrequencyDays().add(frequencyDay);
        }
    }

    private String primeiroTexto(String primeiro, String segundo) {
        if (primeiro != null && !primeiro.isBlank()) {
            return primeiro.trim();
        }
        if (segundo != null && !segundo.isBlank()) {
            return segundo.trim();
        }
        return null;
    }

    private String normalizarFrequencyType(String frequencyType) {
        String normalized = frequencyType.trim().toUpperCase();
        return switch (normalized) {
            case "DAILY" -> "EVERY_DAY";
            case "WEEKDAY" -> "WEEKDAYS";
            case "WEEKEND" -> "WEEKENDS";
            default -> normalized;
        };
    }
}
