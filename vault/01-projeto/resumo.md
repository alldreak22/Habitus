# Resumo

O Habitus é uma aplicação local de diário de hábitos.

A ideia central é juntar cadastro de hábitos, acompanhamento em calendário e registro de conclusões por dia.

## Fluxo Principal

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend apresenta as telas da aplicação. O backend concentra autenticação, regras de negócio e persistência. O SQLite armazena os dados locais.

## Stack Atual

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

Serviço futuro:

- C# em `services/habitus-stats`

## Estrutura Principal

- `frontend/habitus-web`: aplicação web
- `backend/habitus-api`: API principal
- `services/habitus-stats`: espaço reservado para serviço futuro de estatísticas, relatórios e cálculos
- `scripts`: scripts auxiliares
- `vault`: documentação e controle do projeto

## Estado Atual

- a API principal possui endpoints para autenticação, usuário atual, versão, hábitos e calendário agregado
- o frontend possui telas principais e services integrados com a API
- o fluxo de login/cadastro usa API real e token bearer simples
- calendário, hábitos e perfil usam dados reais da API
- a tela `/recuperar-senha` existe, mas ainda não possui fluxo real com backend
- a tela de evolução ainda deve receber métricas futuras
- o serviço C# de estatísticas ainda não foi implementado
- scripts são auxiliares e não fazem parte do fluxo principal

## Documentos Principais

- `vault/01-projeto/arquitetura.md`
- `vault/01-projeto/endpoints.md`
- `vault/01-projeto/integracao.md`
