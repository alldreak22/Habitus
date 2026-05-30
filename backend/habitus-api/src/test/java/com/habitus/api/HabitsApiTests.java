package com.habitus.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HabitsApiTests extends BaseApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void habitosPodemSerCriadosListadosAtualizadosBuscadosEExcluidosComPayloadDoFrontend() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabitoComPayloadFrontend(usuario.token(), "Beber agua", true);

        mockMvc.perform(get("/api/habits").header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(habitoId))
            .andExpect(jsonPath("$[0].active").value(true));

        mockMvc.perform(get("/api/habits/{id}", habitoId).header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Beber agua"));

        mockMvc.perform(put("/api/habits/{id}", habitoId)
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Beber 2L de agua",
                      "title": "Beber 2L de agua",
                      "icon": "water_drop",
                      "color": "#3A86FF",
                      "description": "Atualizado",
                      "targetFrequency": "EVERY_DAY",
                      "timesPerDay": 4,
                      "suggestedTimes": "08:00,12:00,16:00,20:00",
                      "reminder": true,
                      "frequencyType": "EVERY_DAY",
                      "status": "ACTIVE",
                      "reminderTimes": ["08:00", "12:00", "16:00", "20:00"],
                      "frequencyDays": []
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Beber 2L de agua"))
            .andExpect(jsonPath("$.icon").value("water_drop"))
            .andExpect(jsonPath("$.targetFrequency").value("DAILY"))
            .andExpect(jsonPath("$.timesPerDay").value(4));

        mockMvc.perform(delete("/api/habits/{id}", habitoId).header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habits/{id}", habitoId).header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isNotFound());
    }

    @Test
    void habitoPodeSerCriadoSemHorarioComoNoFrontend() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabitoComPayloadFrontend(usuario.token(), "Alongar", false);

        mockMvc.perform(get("/api/habits/{id}", habitoId).header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(habitoId))
            .andExpect(jsonPath("$.name").value("Alongar"))
            .andExpect(jsonPath("$.targetFrequency").value("DAILY"))
            .andExpect(jsonPath("$.timesPerDay").value(1))
            .andExpect(jsonPath("$.reminderTimes", hasSize(0)));
    }

    @Test
    void habitoCustomizadoPersisteDiasFrequenciaEHorariosEmMinutos() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();

        mockMvc.perform(post("/api/habits")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Treino personalizado",
                      "title": "Treino personalizado",
                      "icon": "fitness_center",
                      "color": "#E63946",
                      "description": "Seg, qua e sex",
                      "targetFrequency": "CUSTOM",
                      "timesPerDay": 2,
                      "suggestedTimes": "08:00,18:30",
                      "reminder": true,
                      "frequencyType": "CUSTOM",
                      "status": "ACTIVE",
                      "reminderTimes": ["08:00", "18:30:45"],
                      "frequencyDays": [1, 3, 5]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.frequencyType").value("CUSTOM"))
            .andExpect(jsonPath("$.frequencyDays", contains(1, 3, 5)))
            .andExpect(jsonPath("$.reminderTimes", contains("08:00", "18:30")))
            .andExpect(jsonPath("$.suggestedTimes").value("08:00,18:30"));

        Integer frequencyDayCount = jdbcTemplate.queryForObject(
            "select count(*) from habit_frequency_days",
            Integer.class
        );
        String firstReminderTime = jdbcTemplate.queryForObject(
            "select reminder_time from habit_reminder_times order by reminder_time limit 1",
            String.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(3, frequencyDayCount);
        org.junit.jupiter.api.Assertions.assertEquals("08:00", firstReminderTime);
    }

    @Test
    void validacaoDeHabitoRejeitaCorpoInvalido() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();

        mockMvc.perform(post("/api/habits")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "description": "Sem nome",
                      "targetFrequency": "",
                      "timesPerDay": 0
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.name", notNullValue()))
            .andExpect(jsonPath("$.fields.targetFrequency", notNullValue()))
            .andExpect(jsonPath("$.fields.timesPerDay", notNullValue()));
    }

    @Test
    void usuariosNaoAcessamHabitosDeOutroUsuario() throws Exception {
        UsuarioTeste dono = registrarUsuarioUnico();
        UsuarioTeste outro = registrarUsuarioUnico();
        long habitoId = criarHabito(dono.token(), "Privado");

        mockMvc.perform(get("/api/habits/{id}", habitoId).header("Authorization", outro.cabecalhoAutorizacao()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Hábito não encontrado"));
    }
}
