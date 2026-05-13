package com.habitus.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.dto.request.HabitRequest;
import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.dto.response.HabitResponse;
import com.habitus.api.entity.User;
import com.habitus.api.service.CurrentUserService;
import com.habitus.api.service.HabitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse create(@Valid @RequestBody HabitRequest request) {
        User user = currentUserService.getCurrentUser();
        return habitService.create(user, request);
    }

    @GetMapping
    public List<HabitResponse> list() {
        User user = currentUserService.getCurrentUser();
        return habitService.list(user);
    }

    @GetMapping("/{id}")
    public HabitResponse get(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return habitService.get(user, id);
    }

    @PutMapping("/{id}")
    public HabitResponse update(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        User user = currentUserService.getCurrentUser();
        return habitService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        habitService.deactivate(user, id);
    }

    @GetMapping("/{id}/history")
    public List<DailyHabitCompletionResponse> history(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return habitService.history(user, id);
    }
}
