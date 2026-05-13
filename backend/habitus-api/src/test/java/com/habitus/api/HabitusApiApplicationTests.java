package com.habitus.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitus.api.repository.DailyHabitCompletionRepository;
import com.habitus.api.repository.DailyHabitPlanRepository;
import com.habitus.api.repository.DailyEntryRepository;
import com.habitus.api.repository.HabitRepository;
import com.habitus.api.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite:target/test-data/habitus-api-test.db",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
class HabitusApiApplicationTests {

    private static final AtomicInteger SEQUENCIA_USUARIO = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void versaoRetornaNomeVersaoENomeDeExibicao() throws Exception {
        mockMvc.perform(get("/api/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Habitus"))
            .andExpect(jsonPath("$.version").value("1.0.1"))
            .andExpect(jsonPath("$.displayName").value("Habitus (v1.0.1)"));
    }

    @Test
    void registrarCriaUsuarioERetornaTokenBearer() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Andre",
                      "email": "ANDRE@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.id", notNullValue()))
            .andExpect(jsonPath("$.user.name").value("Andre"))
            .andExpect(jsonPath("$.user.email").value("andre@example.com"))
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void registrarRejeitaEmailDuplicadoIgnorandoMaiusculas() throws Exception {
        registrar("Andre", "andre@example.com", "123456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Outro Andre",
                      "email": "ANDRE@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflito"))
            .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void registrarValidaCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "email": "invalid-email",
                      "password": "123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Requisição inválida"))
            .andExpect(jsonPath("$.message").value("Dados da requisição inválidos"))
            .andExpect(jsonPath("$.fields.name").value("Nome é obrigatório"))
            .andExpect(jsonPath("$.fields.email").value("E-mail inválido"))
            .andExpect(jsonPath("$.fields.password").value("Senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    void loginRetornaTokenParaCredenciaisValidas() throws Exception {
        registrar("Andre", "andre@example.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "andre@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.email").value("andre@example.com"))
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginRejeitaSenhaIncorreta() throws Exception {
        registrar("Andre", "andre@example.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "andre@example.com",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    void rotasProtegidasExigemTokenBearer() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Não autorizado"))
            .andExpect(jsonPath("$.message").value("Token bearer ausente"));
    }

    @Test
    void usuarioAtualRetornaUsuarioDoToken() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();

        mockMvc.perform(get("/api/users/me").header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(usuario.id()))
            .andExpect(jsonPath("$.email").value(usuario.email()));
    }

    @Test
    void habitosPodemSerCriadosListadosAtualizadosBuscadosEDesativados() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();
        long habitoId = criarHabito(usuario.token(), "Beber agua");

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
                      "description": "Atualizado",
                      "targetFrequency": "daily",
                      "timesPerDay": 4,
                      "suggestedTimes": "08:00,12:00,16:00,20:00"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Beber 2L de agua"))
            .andExpect(jsonPath("$.targetFrequency").value("DAILY"))
            .andExpect(jsonPath("$.timesPerDay").value(4));

        mockMvc.perform(delete("/api/habits/{id}", habitoId).header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habits/{id}", habitoId).header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
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
                      "notes": "Inválido"
                    }
                    """.formatted(segundoHabitoId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Hábito da rota e do corpo devem ser iguais"));
    }

    private UsuarioTeste registrarUsuarioUnico() throws Exception {
        int sequencia = SEQUENCIA_USUARIO.incrementAndGet();
        return registrar("Usuario " + sequencia, "user%s@example.com".formatted(sequencia), "123456");
    }

    private UsuarioTeste registrar(String nome, String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(nome, email, senha)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return new UsuarioTeste(
            json.at("/user/id").asLong(),
            json.at("/user/email").asText(),
            json.at("/token").asText()
        );
    }

    private long criarHabito(String token, String nome) throws Exception {
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

    private long criarEntradaDiaria(String token, String data) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/daily-entries")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEntradaDiaria(data)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).at("/id").asLong();
    }

    private String jsonEntradaDiaria(String data) {
        return """
            {
              "entryDate": "%s",
              "markdownContent": "## Meu dia",
              "planningNotes": "Priorizar estudo"
            }
            """.formatted(data);
    }

    private record UsuarioTeste(Long id, String email, String token) {
        String cabecalhoAutorizacao() {
            return "Bearer " + token;
        }
    }
}
