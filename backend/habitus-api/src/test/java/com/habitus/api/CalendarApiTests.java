package com.habitus.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarApiTests extends BaseApiIntegrationTest {

    @Test
    void calendarioMensalListaHabitosPorFrequenciaSemRequisicoesPorDia() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long diarioId = criarHabitoComFrequencia(usuario.token(), "Beber agua", "#7C3AED", "EVERY_DAY", "[]");
        long fimDeSemanaId = criarHabitoComFrequencia(usuario.token(), "Pedalar", "#2563EB", "WEEKENDS", "[]");
        long customId = criarHabitoComFrequencia(usuario.token(), "Alongar", "#16A34A", "CUSTOM", "[2]");

        mockMvc.perform(post("/api/calendar/month")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026,\"month\":6}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.days", hasSize(30)))
            .andExpect(jsonPath("$.days[0].date").value("2026-06-01"))
            .andExpect(jsonPath("$.days[0].markers[*].habitId", hasItem((int) diarioId)))
            .andExpect(jsonPath("$.days[0].markers[*].habitId", not(hasItem((int) fimDeSemanaId))))
            .andExpect(jsonPath("$.days[1].markers[*].habitId", hasItem((int) customId)))
            .andExpect(jsonPath("$.days[5].markers[*].habitId", hasItem((int) fimDeSemanaId)));
    }

    @Test
    void salvarDiaAgregadoCriaEntradaManualESincronizaHabitos() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long primeiroHabitoId = criarHabitoComFrequencia(usuario.token(), "Ler", "#7C3AED", "EVERY_DAY", "[]");
        long segundoHabitoId = criarHabitoComFrequencia(usuario.token(), "Meditar", "#2563EB", "EVERY_DAY", "[]");

        mockMvc.perform(put("/api/calendar/days/{date}", "2026-06-02")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Dia editado",
                      "habits": [
                        { "habitId": %d, "completed": true },
                        { "habitId": %d, "completed": false }
                      ]
                    }
                    """.formatted(primeiroHabitoId, segundoHabitoId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.manual").value(true))
            .andExpect(jsonPath("$.description").value("Dia editado"))
            .andExpect(jsonPath("$.habits", hasSize(2)))
            .andExpect(jsonPath("$.completed").value(false));

        mockMvc.perform(put("/api/calendar/days/{date}", "2026-06-02")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Dia revisado",
                      "habits": [
                        { "habitId": %d, "completed": true }
                      ]
                    }
                    """.formatted(primeiroHabitoId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Dia revisado"))
            .andExpect(jsonPath("$.habits", hasSize(1)))
            .andExpect(jsonPath("$.habits[0].id").value(primeiroHabitoId))
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void calendarioDiaUnicoListaHabitosAutomaticosPorFrequencia() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long diarioId = criarHabitoComFrequencia(usuario.token(), "Beber agua", "#7C3AED", "EVERY_DAY", "[]");
        long fimDeSemanaId = criarHabitoComFrequencia(usuario.token(), "Pedalar", "#2563EB", "WEEKENDS", "[]");

        mockMvc.perform(get("/api/calendar/days/{date}", "2026-06-01")
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-06-01"))
            .andExpect(jsonPath("$.manual").value(false))
            .andExpect(jsonPath("$.markers[*].habitId", hasItem((int) diarioId)))
            .andExpect(jsonPath("$.markers[*].habitId", not(hasItem((int) fimDeSemanaId))));
    }

    @Test
    void calendarioDiaUnicoRetornaEdicaoManualSalva() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabitoComFrequencia(usuario.token(), "Ler", "#7C3AED", "EVERY_DAY", "[]");

        mockMvc.perform(put("/api/calendar/days/{date}", "2026-06-02")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Dia manual",
                      "habits": [
                        { "habitId": %d, "completed": true }
                      ]
                    }
                    """.formatted(habitoId)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/calendar/days/{date}", "2026-06-02")
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.manual").value(true))
            .andExpect(jsonPath("$.description").value("Dia manual"))
            .andExpect(jsonPath("$.habits", hasSize(1)))
            .andExpect(jsonPath("$.habits[0].completed").value(true));
    }

    @Test
    void diaManualSubstituiFrequenciaAutomaticaNoMes() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long diarioId = criarHabitoComFrequencia(usuario.token(), "Diario", "#7C3AED", "EVERY_DAY", "[]");
        long fimDeSemanaId = criarHabitoComFrequencia(usuario.token(), "Fim de semana", "#2563EB", "WEEKENDS", "[]");

        mockMvc.perform(put("/api/calendar/days/{date}", "2026-06-06")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Sabado manual",
                      "habits": [
                        { "habitId": %d, "completed": true }
                      ]
                    }
                    """.formatted(diarioId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/calendar/month")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026,\"month\":6}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.days[5].manual").value(true))
            .andExpect(jsonPath("$.days[5].habits", hasSize(1)))
            .andExpect(jsonPath("$.days[5].markers[*].habitId", hasItem((int) diarioId)))
            .andExpect(jsonPath("$.days[5].markers[*].habitId", not(hasItem((int) fimDeSemanaId))));
    }

    @Test
    void calendarioIsolaDadosPorUsuario() throws Exception {
        UsuarioTeste primeiro = registrarUsuarioUnico();
        UsuarioTeste segundo = registrarUsuarioUnico();
        criarHabitoComFrequencia(segundo.token(), "Outro usuario", "#2563EB", "EVERY_DAY", "[]");

        mockMvc.perform(post("/api/calendar/month")
                .header("Authorization", primeiro.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026,\"month\":6}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.days[0].habits", hasSize(0)));
    }

    @Test
    void historicoDoHabitoUsaConclusaoSalvaPeloCalendario() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabitoComFrequencia(usuario.token(), "Estudar", "#7C3AED", "EVERY_DAY", "[]");

        mockMvc.perform(put("/api/calendar/days/{date}", "2026-06-02")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "",
                      "habits": [
                        { "habitId": %d, "completed": true }
                      ]
                    }
                    """.formatted(habitoId)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/habits/{id}/history", habitoId)
                .header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].habitId").value(habitoId))
            .andExpect(jsonPath("$[0].completed").value(true));
    }

    private long criarHabitoComFrequencia(
        String token,
        String nome,
        String cor,
        String frequencyType,
        String frequencyDays
    ) throws Exception {
        var resultado = mockMvc.perform(post("/api/habits")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "title": "%s",
                      "icon": "water_drop",
                      "color": "%s",
                      "description": "Descricao",
                      "targetFrequency": "%s",
                      "timesPerDay": 1,
                      "suggestedTimes": "",
                      "reminder": false,
                      "frequencyType": "%s",
                      "status": "ACTIVE",
                      "reminderTimes": [],
                      "frequencyDays": %s
                    }
                    """.formatted(nome, nome, cor, frequencyType, frequencyType, frequencyDays)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).at("/id").asLong();
    }
}
