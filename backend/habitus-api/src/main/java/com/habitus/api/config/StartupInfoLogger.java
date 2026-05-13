package com.habitus.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StartupInfoLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupInfoLogger.class);

    private final AppInfo appInfo;

    @EventListener(ApplicationReadyEvent.class)
    public void mostrarVersaoAoIniciar() {
        logger.info(appInfo.nomeComVersao());
    }
}
