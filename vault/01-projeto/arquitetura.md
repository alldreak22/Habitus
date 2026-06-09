# Arquitetura

O Habitus é uma aplicação local de diário de hábitos, composta por frontend web, API principal e banco SQLite.

## Fluxo Principal

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend renderiza a interface e consome a API HTTP. O backend concentra autenticação, validações, regras de negócio e persistência. O SQLite armazena os dados locais da aplicação.

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

- renderizar as telas da aplicação
- controlar a navegação client-side
- manter estado de interface
- consumir a API por HTTP
- usar `VITE_API_BASE_URL` como URL base da API

Estrutura principal:

- `src/app`: definição das rotas da aplicação
- `src/layouts`: layout principal
- `src/pages`: páginas de calendário, hábitos, evolução, perfil, configurações, login, cadastro e recuperação de senha
- `src/components`: componentes reutilizáveis de interface
- `src/services`: camada de acesso à API e adaptação de dados
- `src/content`: textos e opções estáticas de interface
- `src/styles`: estilos globais
- `src/utils`: utilitários locais

Estado atual:

- `src/services/api.js` centraliza chamadas para a API
- `authService.js`, `calendarService.js`, `habitService.js` e `profileService.js` organizam acesso aos dados reais
- login, cadastro, hábitos, calendário e perfil estão integrados com a API
- configurações simples seguem locais no frontend
- `/recuperar-senha` existe como tela, mas ainda não tem fluxo real com backend
- a tela de evolução consome métricas reais do serviço/API C#

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
- cadastrar e autenticar usuários
- resolver o usuário autenticado a partir do token bearer
- validar entradas recebidas
- aplicar regras de negócio
- isolar dados por usuário autenticado
- persistir dados no SQLite via JPA
- converter entidades internas para DTOs de resposta
- tratar erros da API de forma centralizada

Estrutura principal:

- `config`: configurações da aplicação, CORS, senha e informações de versão
- `controller`: endpoints REST
- `dto/request`: contratos de entrada
- `dto/response`: contratos de saída
- `entity`: entidades persistidas
- `exception`: exceções e tratamento global de erros
- `mapper`: conversão entre entidades e DTOs
- `repository`: acesso ao banco via Spring Data JPA
- `service`: regras de negócio, autenticação e transações

## Autenticação

A autenticação atual é local e simples.

Fluxo:

```txt
nick ou email/senha -> API -> token bearer -> chamadas protegidas
```

Características:

- senhas são armazenadas com BCrypt
- o token é gerado a partir do id do usuário
- o token é codificado em Base64 URL-safe
- chamadas protegidas usam `Authorization: Bearer <token>`
- o usuário atual é resolvido no backend pelo `CurrentUserService`

Não há JWT assinado, provedor externo de identidade ou sessão server-side. Também não há endpoint de recuperação de senha implementado.

## Domínio

O domínio atual cobre:

- usuários
- hábitos
- dias de frequência dos hábitos
- horários de lembrete dos hábitos
- entradas diárias do calendário
- hábitos planejados para uma entrada diária
- hábitos concluídos em uma entrada diária
- horários concluídos de hábitos planejados em uma entrada diária

Entidades principais:

- `User`
- `Habit`
- `HabitFrequencyDay`
- `HabitReminderTime`
- `DailyEntry`
- `DailyHabitPlan`
- `DailyHabitTimeCompletion`

Regras principais:

- cadastro e login de usuários
- atualização de perfil, foto e senha
- normalização de email
- geração de nick único
- criação, listagem, busca, atualização e exclusão de hábitos
- validação de frequência, status, dias da semana e horários
- calendário mensal agregado
- resumo pontual por dia
- edição manual de dias do calendário
- criação e atualização de conclusões por hábito e por horário

## API

A API principal roda no backend Spring Boot e usa `/api` como base.

Áreas expostas:

- autenticação
- usuário atual
- versão da aplicação
- hábitos
- calendário agregado por mês e por dia

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

Local padrão:

```txt
backend/habitus-api/data/habitus.db
```

Configuração:

- datasource definido em `backend/habitus-api/src/main/resources/application.yml`
- URL padrão: `jdbc:sqlite:data/habitus.db`
- driver padrão: `org.sqlite.JDBC`
- dialeto padrão: `org.hibernate.community.dialect.SQLiteDialect`
- schema atualizado pelo Hibernate com `ddl-auto=update`

O banco é acessado apenas pelo backend no fluxo principal.

## Configuração

Backend:

- `.env` opcional em `backend/habitus-api`
- porta padrão `8080`
- nome padrão `habitus-api`
- nome de exibição padrão `Habitus`
- versão padrão `1.0.1`

Frontend:

- `.env` opcional em `frontend/habitus-web`
- `VITE_API_BASE_URL` define a base da API
- valor padrão no código: `http://localhost:8080/api`

## Serviço de Estatísticas

Local:

```txt
services/habitus-stats
```

Estado atual:

- serviço/API C# implementado com ASP.NET Core
- porta local padrão `5090`
- concentra métricas da tela de evolução
- lê o SQLite local em modo somente leitura
- reutiliza o token bearer simples atual para resolver o usuário
- não substitui a API principal Java para autenticação, hábitos, calendário ou perfil

## Scripts

Local:

```txt
scripts
```

Uso atual:

- artefatos auxiliares
- referência de schema SQL
- reset e seed local de desenvolvimento
- consultas SQLite locais via utilitário Python

Scripts não fazem parte do caminho principal entre frontend, backend e banco.

## Limite Arquitetural Atual

A arquitetura atual permanece simples:

```txt
Frontend React/Vite
        |
        v
API Spring Boot
        |
        v
SQLite local
```

Não há Docker, filas, mensageria, gateway, banco remoto, microsserviços ativos ou camada de estatísticas ativa no fluxo principal.
