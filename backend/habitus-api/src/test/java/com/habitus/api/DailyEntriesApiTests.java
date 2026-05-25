package com.habitus.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyEntriesApiTests extends BaseApiIntegrationTest {

    @Test
    void entradasDiariasPodemSerCriadasBuscadasPorDataEAtualizadas() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long entradaId = criarEntradaDiaria(usuario.token(), "2026-05-13");

        mockMvc.perform(get("/api/daily-entries/date/{date}", "2026-05-13")
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(entradaId))
            .andExpect(jsonPath("$.entryDate").value("2026-05-13"))
            .andExpect(jsonPath("$.markdownContent").value("## Meu dia"));

        mockMvc.perform(put("/api/daily-entries/{id}", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "entryDate": "2026-05-14",
                      "markdownContent": "## Dia seguinte",
                      "planningNotes": "Priorizar treino"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.entryDate").value("2026-05-14"))
            .andExpect(jsonPath("$.planningNotes").value("Priorizar treino"));
    }

    @Test
    void entradasDiariasRejeitamDataDuplicadaParaMesmoUsuario() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        criarEntradaDiaria(usuario.token(), "2026-05-13");

        mockMvc.perform(post("/api/daily-entries")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEntradaDiaria("2026-05-13")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Entrada diária já existe para esta data"));
    }

    @Test
    void entradasDiariasPermitemMesmaDataParaUsuariosDiferentes() throws Exception {
        UsuarioTeste primeiro = registrarUsuarioUnico();
        UsuarioTeste segundo = registrarUsuarioUnico();
        criarEntradaDiaria(primeiro.token(), "2026-05-13");

        mockMvc.perform(post("/api/daily-entries")
                .header("Authorization", segundo.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEntradaDiaria("2026-05-13")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.entryDate").value("2026-05-13"));
    }
}
