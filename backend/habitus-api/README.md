# Habitus API

API principal do Habitus, implementada em Java 21 com Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Spring Security Crypto, Maven e SQLite.

## Responsabilidades

- Cadastro e login de usuários.
- Identificação do usuário atual por token bearer simples.
- Atualização de perfil, foto e senha.
- CRUD de hábitos do usuário autenticado.
- Calendário agregado por mês e por dia.
- Registro de conclusões de hábitos e horários em dias do calendário.
- Exposição da versão da aplicação.

## Requisitos

- Java 21.
- Maven.

## Configuração

Crie um arquivo `.env` a partir de `.env.example` quando precisar alterar portas, versão ou banco.

Variáveis principais:

```properties
APP_NAME=habitus-api
APP_DISPLAY_NAME=Habitus
APP_VERSION=1.0.1
SERVER_PORT=8080
DATABASE_URL=jdbc:sqlite:data/habitus.db
JPA_DDL_AUTO=update
```

Por padrão, o banco SQLite é criado em:

```txt
backend/habitus-api/data/habitus.db
```

O diretório `data/` e arquivos `*.db`, `*.sqlite` e `*.sqlite3` não devem ser versionados.

## Como Rodar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Testes

```bash
mvn test
```

## Autenticação

A versão atual usa um token simples retornado pelo cadastro ou login. Envie esse token nas rotas protegidas:

```http
Authorization: Bearer <token>
```

Não há JWT assinado, provedor externo de identidade ou sessão server-side.

## Fluxo Rápido para Postman/Insomnia

Registrar usuário:

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Andre",
  "nick": "andre",
  "email": "andre@example.com",
  "password": "12345678"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "login": "andre",
  "password": "12345678"
}
```

Consultar usuário atual:

```http
GET /api/users/me
Authorization: Bearer <token>
```

Criar hábito:

```http
POST /api/habits
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Beber água",
  "icon": "water_drop",
  "color": "#2f80ed",
  "description": "Beber água ao longo do dia",
  "targetFrequency": "EVERY_DAY",
  "timesPerDay": 3,
  "reminder": true,
  "reminderTimes": ["08:00", "14:00", "20:00"],
  "frequencyDays": [1, 2, 3, 4, 5],
  "status": "ACTIVE"
}
```

Carregar calendário mensal:

```http
POST /api/calendar/month
Authorization: Bearer <token>
Content-Type: application/json

{
  "year": 2026,
  "month": 6
}
```

Salvar edição de um dia:

```http
PUT /api/calendar/days/2026-06-08
Authorization: Bearer <token>
Content-Type: application/json

{
  "description": "Dia produtivo.",
  "habits": [
    {
      "habitId": 1,
      "completed": true,
      "timeSlots": [
        { "time": "08:00", "completed": true }
      ]
    }
  ]
}
```

## Endpoints Principais

- `GET /api/version`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/users/me`
- `PUT /api/users/me/password`
- `POST /api/habits`
- `GET /api/habits`
- `GET /api/habits/{id}`
- `PUT /api/habits/{id}`
- `DELETE /api/habits/{id}`
- `GET /api/habits/{id}/history`
- `POST /api/calendar/month`
- `GET /api/calendar/days/{date}`
- `PUT /api/calendar/days/{date}`

## Pendências Conhecidas

- Não há endpoint de recuperação de senha implementado.
- Relatórios, cálculos e métricas ficam para o futuro serviço/API C# em `services/habitus-stats`.

## CORS

A API permite chamadas para `/api/**` a partir de:

- `http://localhost:5173`
- `http://localhost:3000`
