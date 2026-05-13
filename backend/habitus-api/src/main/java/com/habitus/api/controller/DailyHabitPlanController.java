package com.habitus.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.dto.request.DailyHabitPlanRequest;
import com.habitus.api.dto.response.DailyHabitPlanResponse;
import com.habitus.api.entity.User;
import com.habitus.api.service.CurrentUserService;
import com.habitus.api.service.DailyHabitPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-entries/{entryId}/planned-habits")
public class DailyHabitPlanController {

    private final DailyHabitPlanService planService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DailyHabitPlanResponse create(
        @PathVariable Long entryId,
        @Valid @RequestBody DailyHabitPlanRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        return planService.create(user, entryId, request);
    }

    @GetMapping
    public List<DailyHabitPlanResponse> list(@PathVariable Long entryId) {
        User user = currentUserService.getCurrentUser();
        return planService.list(user, entryId);
    }

    @DeleteMapping("/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long entryId, @PathVariable Long habitId) {
        User user = currentUserService.getCurrentUser();
        planService.delete(user, entryId, habitId);
    }
}
