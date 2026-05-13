package com.habitus.api.controller;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.dto.request.DailyEntryRequest;
import com.habitus.api.dto.response.DailyEntryResponse;
import com.habitus.api.entity.User;
import com.habitus.api.service.CurrentUserService;
import com.habitus.api.service.DailyEntryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-entries")
public class DailyEntryController {

    private final DailyEntryService dailyEntryService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DailyEntryResponse criar(@Valid @RequestBody DailyEntryRequest requisicao) {
        User user = currentUserService.obterUsuarioAtual();
        return dailyEntryService.criar(user, requisicao);
    }

    @GetMapping("/date/{date}")
    public DailyEntryResponse buscarPorData(@PathVariable LocalDate date) {
        User user = currentUserService.obterUsuarioAtual();
        return dailyEntryService.buscarPorData(user, date);
    }

    @PutMapping("/{id}")
    public DailyEntryResponse atualizar(@PathVariable Long id, @Valid @RequestBody DailyEntryRequest requisicao) {
        User user = currentUserService.obterUsuarioAtual();
        return dailyEntryService.atualizar(user, id, requisicao);
    }
}
