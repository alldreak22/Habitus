package com.habitus.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitus.api.repository.DailyEntryRepository;
import com.habitus.api.repository.DailyHabitCompletionRepository;
import com.habitus.api.repository.DailyHabitPlanRepository;
import com.habitus.api.repository.HabitRepository;
import com.habitus.api.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite:target/test-data/habitus-api-test.db",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
abstract class BaseApiIntegrationTest {

    private static final AtomicInteger SEQUENCIA_USUARIO = new AtomicInteger();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private DailyHabitCompletionRepository completionRepository;

    @Autowired
    private DailyHabitPlanRepository planRepository;

    @Autowired
    private DailyEntryRepository dailyEntryRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void criarDiretorioDeDadosDoTeste() throws Exception {
        Files.createDirectories(Path.of("target/test-data"));
    }

    @BeforeEach
    void limparBanco() {
        completionRepository.deleteAll();
        planRepository.deleteAll();
        dailyEntryRepository.deleteAll();
        habitRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected UsuarioTeste registrarUsuarioUnico() throws Exception {
        int sequencia = SEQUENCIA_USUARIO.incrementAndGet();
        return registrar("Usuario " + sequencia, "user%s@example.com".formatted(sequencia), "123456");
    }

    protected UsuarioTeste registrar(String nome, String email, String senha) throws Exception {
        String nick = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
        MvcResult resultado = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "nick": "%s",
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(nome, nick, email, senha)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return new UsuarioTeste(
            json.at("/user/id").asLong(),
            json.at("/user/email").asText(),
            json.at("/token").asText()
        );
    }

    protected long criarHabito(String token, String nome) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/habits")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "description": "Descricao",
                      "targetFrequency": "daily",
                      "timesPerDay": 1,
                      "suggestedTimes": "08:00"
                    }
                    """.formatted(nome)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).at("/id").asLong();
    }

    protected long criarHabitoComPayloadFrontend(String token, String nome, boolean comHorario) throws Exception {
        String suggestedTimes = comHorario ? "\"08:00\"" : "\"\"";
        String reminderTimes = comHorario ? "[\"08:00\"]" : "[]";

        MvcResult resultado = mockMvc.perform(post("/api/habits")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "title": "%s",
                      "icon": "water_drop",
                      "color": "#3A86FF",
                      "description": "Descricao",
                      "targetFrequency": "EVERY_DAY",
                      "timesPerDay": 1,
                      "suggestedTimes": %s,
                      "reminder": false,
                      "frequencyType": "EVERY_DAY",
                      "status": "ACTIVE",
                      "reminderTimes": %s,
                      "frequencyDays": []
                    }
                    """.formatted(nome, nome, suggestedTimes, reminderTimes)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).at("/id").asLong();
    }

    protected long criarEntradaDiaria(String token, String data) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/daily-entries")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEntradaDiaria(data)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).at("/id").asLong();
    }

    protected String jsonEntradaDiaria(String data) {
        return """
            {
              "entryDate": "%s",
              "markdownContent": "## Meu dia",
              "planningNotes": "Priorizar estudo"
            }
            """.formatted(data);
    }

    protected record UsuarioTeste(Long id, String email, String token) {
        String cabecalhoAutorizacao() {
            return "Bearer " + token;
        }
    }
}
