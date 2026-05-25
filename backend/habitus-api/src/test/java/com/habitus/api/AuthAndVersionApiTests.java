package com.habitus.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAndVersionApiTests extends BaseApiIntegrationTest {

    @Test
    void versaoRetornaNomeVersaoENomeDeExibicao() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Habitus"))
            .andReturn();

        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        String versao = json.path("version").asText();
        String nome = json.path("name").asText();
        String nomeComVersao = json.path("displayName").asText();

        org.junit.jupiter.api.Assertions.assertFalse(versao == null || versao.isBlank());
        org.junit.jupiter.api.Assertions.assertEquals("%s (v%s)".formatted(nome, versao), nomeComVersao);
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
            .andExpect(jsonPath("$.message").value("Token ausente"));
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
    void usuarioAtualPodeSerAtualizadoEPersistidoNoBanco() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();

        mockMvc.perform(put("/api/users/me")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Usuario Atualizado",
                      "email": "usuario.atualizado@example.com",
                      "nick": "user_atualizado",
                      "picture": "data:image/png;base64,AAAA"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Usuario Atualizado"))
            .andExpect(jsonPath("$.email").value("usuario.atualizado@example.com"))
            .andExpect(jsonPath("$.nick").value("user_atualizado"))
            .andExpect(jsonPath("$.picture").value("data:image/png;base64,AAAA"));

        mockMvc.perform(get("/api/users/me").header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Usuario Atualizado"))
            .andExpect(jsonPath("$.email").value("usuario.atualizado@example.com"))
            .andExpect(jsonPath("$.nick").value("user_atualizado"))
            .andExpect(jsonPath("$.picture").value("data:image/png;base64,AAAA"));
    }
}
