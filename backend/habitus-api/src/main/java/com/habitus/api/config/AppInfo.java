package com.habitus.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppInfo {

    private final String nome;
    private final String versao;

    public AppInfo(
        @Value("${app.display-name}") String nome,
        @Value("${app.version}") String versao
    ) {
        this.nome = nome;
        this.versao = versao;
    }

    public String nome() {
        return nome;
    }

    public String versao() {
        return versao;
    }

    public String nomeComVersao() {
        return "%s (v%s)".formatted(nome, versao);
    }
}
