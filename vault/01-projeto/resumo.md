# Resumo

O Habitus e uma aplicacao local de diario de habitos.

A ideia central e juntar acompanhamento de habitos com registro diario.

## Fluxo principal

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend apresenta as telas da aplicacao.
O backend concentra autenticacao, regras de negocio e persistencia.
O SQLite armazena os dados locais.

## Stack atual

Frontend:

- React
- React Router
- Vite
- JavaScript

Backend:

- Java
- Spring Boot
- Maven
- SQLite

## Estrutura principal

- `frontend/habitus-web`: aplicacao web
- `backend/habitus-api`: API principal
- `services/habitus-stats`: espaco reservado para servico futuro de estatisticas, relatorios e calculos gerais
- `scripts`: scripts auxiliares
- `vault`: documentacao e controle do projeto

## Estado atual

- a API principal ja possui endpoints para autenticacao, usuario atual, versao, habitos, entradas diarias, habitos planejados e habitos concluidos
- o frontend ja possui telas principais, cliente HTTP base e services organizados
- parte do frontend ainda usa mocks e `localStorage`
- a integracao completa entre frontend e API ainda nao esta finalizada
- o fluxo completo de login/autenticacao no frontend ainda nao esta finalizado
- o servico de estatisticas ainda nao foi implementado
- os scripts ainda sao auxiliares e nao fazem parte do fluxo principal

## Documentos principais

- `vault/01-projeto/arquitetura.md`
- `vault/01-projeto/endpoints.md`
- `vault/01-projeto/integracao.md`
