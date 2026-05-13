package com.habitus.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.dto.request.DailyHabitCompletionRequest;
import com.habitus.api.dto.response.DailyHabitCompletionResponse;
import com.habitus.api.entity.User;
import com.habitus.api.service.CurrentUserService;
import com.habitus.api.service.DailyHabitCompletionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-entries/{entryId}/completed-habits")
public class DailyHabitCompletionController {

    private final DailyHabitCompletionService completionService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DailyHabitCompletionResponse create(
        @PathVariable Long entryId,
        @Valid @RequestBody DailyHabitCompletionRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        return completionService.create(user, entryId, request);
    }

    @GetMapping
    public List<DailyHabitCompletionResponse> list(@PathVariable Long entryId) {
        User user = currentUserService.getCurrentUser();
        return completionService.list(user, entryId);
    }

    @PutMapping("/{habitId}")
    public DailyHabitCompletionResponse update(
        @PathVariable Long entryId,
        @PathVariable Long habitId,
        @Valid @RequestBody DailyHabitCompletionRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        return completionService.update(user, entryId, habitId, request);
    }
}
