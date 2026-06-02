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
                      "nick": "andre",
                      "email": "ANDRE@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.id", notNullValue()))
            .andExpect(jsonPath("$.user.name").value("Andre"))
            .andExpect(jsonPath("$.user.nick").value("andre"))
            .andExpect(jsonPath("$.user.email").value("andre@example.com"))
            .andExpect(jsonPath("$.user.createdAt", notNullValue()))
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
                      "nick": "outro_andre",
                      "email": "ANDRE@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflito"))
            .andExpect(jsonPath("$.message").value("E-mail ja cadastrado"));
    }

    @Test
    void registrarRejeitaNickDuplicadoIgnorandoMaiusculas() throws Exception {
        registrar("Andre", "andre@example.com", "123456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Outro Andre",
                      "nick": "ANDRE",
                      "email": "outro@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflito"))
            .andExpect(jsonPath("$.message").value("Usuario ja cadastrado"));
    }

    @Test
    void registrarValidaCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "nick": "",
                      "email": "invalid-email",
                      "password": "123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Requisicao invalida"))
            .andExpect(jsonPath("$.message").value("Dados da requisicao invalidos"))
            .andExpect(jsonPath("$.fields.name").value("Nome e obrigatorio"))
            .andExpect(jsonPath("$.fields.nick").value("Usuario e obrigatorio"))
            .andExpect(jsonPath("$.fields.email").value("E-mail invalido"))
            .andExpect(jsonPath("$.fields.password").value("Senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    void loginRetornaTokenParaCredenciaisValidas() throws Exception {
        registrar("Andre", "andre@example.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login": "andre@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.email").value("andre@example.com"))
            .andExpect(jsonPath("$.user.createdAt", notNullValue()))
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginRetornaTokenParaNickValido() throws Exception {
        registrar("Andre", "andre@example.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login": "andre",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.email").value("andre@example.com"))
            .andExpect(jsonPath("$.user.nick").value("andre"))
            .andExpect(jsonPath("$.user.createdAt", notNullValue()))
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
                      "login": "andre@example.com",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Login ou senha invalidos"));
    }

    @Test
    void rotasProtegidasExigemTokenBearer() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Nao autorizado"))
            .andExpect(jsonPath("$.message").value("Token ausente"));
    }

    @Test
    void usuarioAtualRetornaUsuarioDoToken() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();

        mockMvc.perform(get("/api/users/me").header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(usuario.id()))
            .andExpect(jsonPath("$.email").value(usuario.email()))
            .andExpect(jsonPath("$.createdAt", notNullValue()));
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
            .andExpect(jsonPath("$.picture").value("data:image/png;base64,AAAA"))
            .andExpect(jsonPath("$.createdAt", notNullValue()));

        mockMvc.perform(get("/api/users/me").header("Authorization", usuario.cabecalhoAutorizacao()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Usuario Atualizado"))
            .andExpect(jsonPath("$.email").value("usuario.atualizado@example.com"))
            .andExpect(jsonPath("$.nick").value("user_atualizado"))
            .andExpect(jsonPath("$.picture").value("data:image/png;base64,AAAA"))
            .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void usuarioAtualPodeAlterarSenhaELoginPassaAUsarSenhaNova() throws Exception {
        UsuarioTeste usuario = registrar("Usuario Senha", "senha@example.com", "123456");

        mockMvc.perform(put("/api/users/me/password")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "123456",
                      "newPassword": "12345678"
                    }
                    """))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login": "senha@example.com",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login": "senha@example.com",
                      "password": "12345678"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void alterarSenhaRejeitaSenhaAtualIncorreta() throws Exception {
        UsuarioTeste usuario = registrar("Usuario Senha", "senha@example.com", "123456");

        mockMvc.perform(put("/api/users/me/password")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "senha-errada",
                      "newPassword": "12345678"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Senha atual invalida"));
    }

    @Test
    void alterarSenhaRejeitaSenhaNovaIgualAtual() throws Exception {
        UsuarioTeste usuario = registrar("Usuario Senha", "senha@example.com", "12345678");

        mockMvc.perform(put("/api/users/me/password")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "12345678",
                      "newPassword": "12345678"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Nova senha nao pode ser igual a senha atual"));
    }

    @Test
    void alterarSenhaValidaTamanhoMinimoDaSenhaNova() throws Exception {
        UsuarioTeste usuario = registrarUsuarioUnico();

        mockMvc.perform(put("/api/users/me/password")
                .header("Authorization", usuario.cabecalhoAutorizacao())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "123456",
                      "newPassword": "1234567"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.newPassword").value("Nova senha deve ter pelo menos 8 caracteres"));
    }
}
