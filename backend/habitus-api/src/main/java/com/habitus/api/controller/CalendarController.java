package com.habitus.api.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.dto.request.CalendarDaySaveRequest;
import com.habitus.api.dto.request.CalendarMonthRequest;
import com.habitus.api.dto.response.CalendarDayResponse;
import com.habitus.api.dto.response.CalendarMonthResponse;
import com.habitus.api.service.CalendarService;
import com.habitus.api.service.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Validated
public class CalendarController {

    private final CalendarService calendarService;
    private final CurrentUserService currentUserService;

    @PostMapping("/month")
    public ResponseEntity<CalendarMonthResponse> buscarMes(@Valid @RequestBody CalendarMonthRequest requisicao) {
        return ResponseEntity.ok(calendarService.buscarMes(currentUserService.obterUsuarioAtual(), requisicao));
    }

    @GetMapping("/days/{date}")
    public ResponseEntity<CalendarDayResponse> buscarDia(@PathVariable LocalDate date) {
        return ResponseEntity.ok(calendarService.buscarDia(currentUserService.obterUsuarioAtual(), date));
    }

    @PutMapping("/days/{date}")
    public ResponseEntity<CalendarDayResponse> salvarDia(
        @PathVariable LocalDate date,
        @Valid @RequestBody CalendarDaySaveRequest requisicao
    ) {
        return ResponseEntity.ok(calendarService.salvarDia(currentUserService.obterUsuarioAtual(), date, requisicao));
    }
}
