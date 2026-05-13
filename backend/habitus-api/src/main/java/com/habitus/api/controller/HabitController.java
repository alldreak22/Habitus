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
    public HabitResponse criar(@Valid @RequestBody HabitRequest requisicao) {
        User user = currentUserService.obterUsuarioAtual();
        return habitService.criar(user, requisicao);
    }

    @GetMapping
    public List<HabitResponse> listar() {
        User user = currentUserService.obterUsuarioAtual();
        return habitService.listar(user);
    }

    @GetMapping("/{id}")
    public HabitResponse buscar(@PathVariable Long id) {
        User user = currentUserService.obterUsuarioAtual();
        return habitService.buscar(user, id);
    }

    @PutMapping("/{id}")
    public HabitResponse atualizar(@PathVariable Long id, @Valid @RequestBody HabitRequest requisicao) {
        User user = currentUserService.obterUsuarioAtual();
        return habitService.atualizar(user, id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        User user = currentUserService.obterUsuarioAtual();
        habitService.desativar(user, id);
    }

    @GetMapping("/{id}/history")
    public List<DailyHabitCompletionResponse> historico(@PathVariable Long id) {
        User user = currentUserService.obterUsuarioAtual();
        return habitService.historico(user, id);
    }
}
