package com.habitus.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habitus.api.config.AppInfo;
import com.habitus.api.dto.response.VersionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/version")
public class VersionController {

    private final AppInfo appInfo;

    @GetMapping
    public VersionResponse buscarVersao() {
        return new VersionResponse(appInfo.nome(), appInfo.versao(), appInfo.nomeComVersao());
    }
}
