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
    public DailyEntryResponse create(User user, DailyEntryRequest request) {
        if (dailyEntryRepository.existsByUserIdAndEntryDate(user.getId(), request.entryDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "Daily entry already exists for this date");
        }

        DailyEntry entry = new DailyEntry();
        entry.setUser(user);
        applyRequest(entry, request);
        return mapper.toDailyEntryResponse(dailyEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public DailyEntryResponse getByDate(User user, LocalDate date) {
        return mapper.toDailyEntryResponse(findByUserAndDate(user, date));
    }

    @Transactional
    public DailyEntryResponse update(User user, Long id, DailyEntryRequest request) {
        DailyEntry entry = findUserEntry(user, id);

        if (!entry.getEntryDate().equals(request.entryDate())
            && dailyEntryRepository.existsByUserIdAndEntryDate(user.getId(), request.entryDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "Daily entry already exists for this date");
        }

        applyRequest(entry, request);
        return mapper.toDailyEntryResponse(dailyEntryRepository.save(entry));
    }

    DailyEntry findUserEntry(User user, Long entryId) {
        return dailyEntryRepository.findByIdAndUserId(entryId, user.getId())
            .orElseThrow(() -> new NotFoundException("Daily entry not found"));
    }

    private DailyEntry findByUserAndDate(User user, LocalDate date) {
        return dailyEntryRepository.findByUserIdAndEntryDate(user.getId(), date)
            .orElseThrow(() -> new NotFoundException("Daily entry not found"));
    }

    private void applyRequest(DailyEntry entry, DailyEntryRequest request) {
        entry.setEntryDate(request.entryDate());
        entry.setMarkdownContent(request.markdownContent());
        entry.setPlanningNotes(request.planningNotes());
    }
}
