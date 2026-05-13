package com.habitus.api.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.habitus.api.dto.request.DailyEntryRequest;
import com.habitus.api.dto.response.DailyEntryResponse;
import com.habitus.api.entity.DailyEntry;
import com.habitus.api.entity.User;
import com.habitus.api.exception.ApiException;
import com.habitus.api.exception.NotFoundException;
import com.habitus.api.mapper.ApiMapper;
import com.habitus.api.repository.DailyEntryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyEntryService {

    private final DailyEntryRepository dailyEntryRepository;
    private final ApiMapper mapper;

    @Transactional
    public DailyEntryResponse criar(User user, DailyEntryRequest requisicao) {
        if (dailyEntryRepository.existsByUserIdAndEntryDate(user.getId(), requisicao.entryDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "Entrada diária já existe para esta data");
        }

        DailyEntry entry = new DailyEntry();
        entry.setUser(user);
        aplicarRequisicao(entry, requisicao);
        return mapper.toDailyEntryResponse(dailyEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public DailyEntryResponse buscarPorData(User user, LocalDate date) {
        return mapper.toDailyEntryResponse(buscarPorUsuarioEData(user, date));
    }

    @Transactional
    public DailyEntryResponse atualizar(User user, Long id, DailyEntryRequest requisicao) {
        DailyEntry entry = buscarEntradaDoUsuario(user, id);

        if (!entry.getEntryDate().equals(requisicao.entryDate())
            && dailyEntryRepository.existsByUserIdAndEntryDate(user.getId(), requisicao.entryDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "Entrada diária já existe para esta data");
        }

        aplicarRequisicao(entry, requisicao);
        return mapper.toDailyEntryResponse(dailyEntryRepository.save(entry));
    }

    DailyEntry buscarEntradaDoUsuario(User user, Long entryId) {
        return dailyEntryRepository.findByIdAndUserId(entryId, user.getId())
            .orElseThrow(() -> new NotFoundException("Entrada diária não encontrada"));
    }

    private DailyEntry buscarPorUsuarioEData(User user, LocalDate date) {
        return dailyEntryRepository.findByUserIdAndEntryDate(user.getId(), date)
            .orElseThrow(() -> new NotFoundException("Entrada diária não encontrada"));
    }

    private void aplicarRequisicao(DailyEntry entry, DailyEntryRequest requisicao) {
        entry.setEntryDate(requisicao.entryDate());
        entry.setMarkdownContent(requisicao.markdownContent());
        entry.setPlanningNotes(requisicao.planningNotes());
    }
}
