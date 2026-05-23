# Arquitetura

O Habitus e uma aplicacao local de diario de habitos, composta por frontend web, API principal e banco SQLite.

## Fluxo principal

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend renderiza a interface e consome a API HTTP.
O backend concentra autenticacao, validacoes, regras de negocio e persistencia.
O SQLite armazena os dados locais da aplicacao.

## Frontend

Local:

```txt
frontend/habitus-web
```

Stack:

- React
- React Router
- Vite
- JavaScript

Responsabilidades:

- renderizar as telas da aplicacao
- controlar a navegacao client-side
- manter estado de interface
- consumir a API por HTTP
- usar `VITE_API_BASE_URL` como URL base da API

Estrutura principal:

- `src/app`: definicao das rotas da aplicacao
- `src/layouts`: layout principal
- `src/pages`: paginas de calendario, habitos, evolucao, perfil, configuracoes, login e cadastro
- `src/components`: componentes reutilizaveis de interface
- `src/services`: camada de acesso a dados e chamadas HTTP
- `src/content`: conteudos e mocks usados por telas ainda nao integradas
- `src/styles`: estilos globais
- `src/utils`: utilitarios locais

Estado atual:

- `src/services/api.js` centraliza chamadas para a API
- `src/services/calendarService.js`, `habitService.js` e `profileService.js` organizam acesso a dados das telas
- algumas telas ainda usam dados mockados em `src/content`
- login e cadastro existem como rotas e paginas, mas o fluxo completo de autenticacao no frontend ainda nao esta consolidado

## Backend

Local:

```txt
backend/habitus-api
```

Stack:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security Crypto
- Maven
- SQLite
- Lombok

Responsabilidades:

- expor a API REST sob `/api`
- cadastrar e autenticar usuarios
- resolver o usuario autenticado a partir do token bearer
- validar entradas recebidas
- aplicar regras de negocio
- isolar dados por usuario autenticado
- persistir dados no SQLite via JPA
- converter entidades internas para DTOs de resposta
- tratar erros da API de forma centralizada

Estrutura principal:

- `config`: configuracoes da aplicacao, CORS, senha e informacoes de versao
- `controller`: endpoints REST
- `dto/request`: contratos de entrada
- `dto/response`: contratos de saida
- `entity`: entidades persistidas
- `exception`: excecoes e tratamento global de erros
- `mapper`: conversao entre entidades e DTOs
- `repository`: acesso ao banco via Spring Data JPA
- `service`: regras de negocio, autenticacao e transacoes

## Autenticacao

A autenticacao atual e local e simples.

Fluxo:

```txt
email/senha -> API -> token bearer -> chamadas protegidas
```

Caracteristicas:

- senhas sao armazenadas com BCrypt
- o token e gerado a partir do id do usuario
- o token e codificado em Base64 URL-safe
- chamadas protegidas usam `Authorization: Bearer <token>`
- o usuario atual e resolvido no backend pelo `CurrentUserService`

Nao ha JWT assinado, provedor externo de identidade ou sessao server-side.

## Dominio

O dominio atual cobre:

- usuarios
- habitos
- dias de frequencia dos habitos
- horarios de lembrete dos habitos
- entradas diarias
- habitos planejados para uma entrada diaria
- habitos concluidos em uma entrada diaria

Entidades principais:

- `User`
- `Habit`
- `HabitFrequencyDay`
- `HabitReminderTime`
- `DailyEntry`
- `DailyHabitPlan`
- `DailyHabitCompletion`

Regras principais:

- cadastro e login de usuarios
- normalizacao de email
- geracao de nick unico
- criacao, listagem, busca, atualizacao e desativacao de habitos
- validacao de frequencia, status, dias da semana e horarios
- criacao, busca por data e atualizacao de entradas diarias
- planejamento e remocao de habitos em entradas diarias
- criacao, listagem e atualizacao de conclusoes de habitos

## API

A API principal roda no backend Spring Boot e usa `/api` como base.

Areas expostas:

- autenticacao
- usuario atual
- versao da aplicacao
- habitos
- entradas diarias
- habitos planejados em entradas diarias
- habitos concluidos em entradas diarias

O CORS permite chamadas locais a partir de:

- `http://localhost:5173`
- `http://localhost:3000`

Os endpoints existentes ficam documentados em:

```txt
vault/01-projeto/endpoints.md
```

## Banco

Banco:

```txt
SQLite
```

Local padrao:

```txt
backend/habitus-api/data/habitus.db
```

Configuracao:

- datasource definido em `backend/habitus-api/src/main/resources/application.yml`
- URL padrao: `jdbc:sqlite:data/habitus.db`
- driver padrao: `org.sqlite.JDBC`
- dialeto padrao: `org.hibernate.community.dialect.SQLiteDialect`
- schema atualizado pelo Hibernate com `ddl-auto=update`

O banco e acessado apenas pelo backend no fluxo principal.

## Configuracao

Backend:

- `.env` opcional em `backend/habitus-api`
- porta padrao `8080`
- nome padrao `habitus-api`
- nome de exibicao padrao `Habitus`
- versao padrao `1.0.1`

Frontend:

- `.env` opcional em `frontend/habitus-web`
- `VITE_API_BASE_URL` define a base da API
- valor padrao no codigo: `http://localhost:8080/api`

## Servico de estatisticas

Local:

```txt
services/habitus-stats
```

Estado atual:

- diretorio reservado
- sem implementacao versionada
- nao faz parte do fluxo principal atual

## Scripts

Local:

```txt
scripts
```

Uso atual:

- artefatos auxiliares
- referencia de schema SQL
- tarefas fora do fluxo principal da aplicacao

Scripts nao fazem parte do caminho principal entre frontend, backend e banco.

## Limite arquitetural

A arquitetura atual deve permanecer simples:

```txt
Frontend React/Vite
        |
        v
API Spring Boot
        |
        v
SQLite local
```

Nao ha Docker, filas, mensageria, gateway, banco remoto, microsservicos ativos ou camada de estatisticas ativa no fluxo principal.
