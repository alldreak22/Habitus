package com.habitus.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyHabitsApiTests extends BaseApiIntegrationTest {

    @Test
    void habitosPlanejadosPodemSerAdicionadosListadosEExcluidos() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabito(usuario.token(), "Ler");
        long entradaId = criarEntradaDiaria(usuario.token(), "2026-05-13");

        mockMvc.perform(post("/api/daily-entries/{entryId}/planned-habits", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"habitId\": %d}".formatted(habitoId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.habitId").value(habitoId))
            .andExpect(jsonPath("$.planned").value(true));

        mockMvc.perform(get("/api/daily-entries/{entryId}/planned-habits", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].habitName").value("Ler"));

        mockMvc.perform(delete("/api/daily-entries/{entryId}/planned-habits/{habitId}", entradaId, habitoId)
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/daily-entries/{entryId}/planned-habits", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void habitosConcluidosPodemSerCriadosAtualizadosListadosEConsultadosNoHistorico() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabito(usuario.token(), "Meditar");
        long entradaId = criarEntradaDiaria(usuario.token(), "2026-05-13");

        mockMvc.perform(post("/api/daily-entries/{entryId}/completed-habits", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "habitId": %d,
                      "completed": true,
                      "notes": "Feito de manha"
                    }
                    """.formatted(habitoId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.habitId").value(habitoId))
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.notes").value("Feito de manha"));

        mockMvc.perform(put("/api/daily-entries/{entryId}/completed-habits/{habitId}", entradaId, habitoId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "habitId": %d,
                      "completed": false,
                      "notes": "Remarcado"
                    }
                    """.formatted(habitoId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(false))
            .andExpect(jsonPath("$.notes").value("Remarcado"));

        mockMvc.perform(get("/api/daily-entries/{entryId}/completed-habits", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].habitName").value("Meditar"));

        mockMvc.perform(get("/api/habits/{id}/history", habitoId)
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].habitId").value(habitoId));
    }

    @Test
    void atualizacaoDeHabitoConcluidoRejeitaHabitoDiferenteNoCorpo() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long primeiroHabitoId = criarHabito(usuario.token(), "Caminhar");
        long segundoHabitoId = criarHabito(usuario.token(), "Estudar");
        long entradaId = criarEntradaDiaria(usuario.token(), "2026-05-13");

        mockMvc.perform(post("/api/daily-entries/{entryId}/completed-habits", entradaId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "habitId": %d,
                      "completed": true,
                      "notes": "Ok"
                    }
                    """.formatted(primeiroHabitoId)))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/daily-entries/{entryId}/completed-habits/{habitId}", entradaId, primeiroHabitoId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "habitId": %d,
                      "completed": true,
                      "notes": "Invalido"
                    }
                    """.formatted(segundoHabitoId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Hábito da rota e do corpo devem ser iguais"));
    }
}
