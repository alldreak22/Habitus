# Habitus API

API principal do Habitus, implementada em Java 21 com Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Spring Security Crypto, Maven e SQLite.

## Responsabilidades

- Cadastro e login de usuários.
- Identificação do usuário atual por token simples.
- CRUD de hábitos.
- Registro de entradas diárias.
- Planejamento de hábitos para uma entrada diária.
- Marcação de hábitos como concluídos.
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

## Como rodar

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

Esse formato é temporário e foi pensado para ser substituído por JWT ou outro mecanismo mais robusto.

## Fluxo rápido para Postman/Insomnia

### Registrar usuário

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Andre",
  "email": "andre@example.com",
  "password": "123456"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "andre@example.com",
  "password": "123456"
}
```

### Consultar usuário atual

```http
GET /api/users/me
Authorization: Bearer <token>
```

### Criar hábito

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

Campos aceitos por compatibilidade:

- `name` e `title` representam o nome/título do hábito.
- `targetFrequency` e `frequencyType` representam a frequência.
- `suggestedTimes` e `reminderTimes` representam horários sugeridos/lembretes.

### Criar entrada diária

```http
POST /api/daily-entries
Authorization: Bearer <token>
Content-Type: application/json

{
  "entryDate": "2026-05-13",
  "markdownContent": "## Meu dia\nTexto em Markdown.",
  "planningNotes": "Priorizar estudo e caminhada."
}
```

### Planejar hábito para o dia

```http
POST /api/daily-entries/1/planned-habits
Authorization: Bearer <token>
Content-Type: application/json

{
  "habitId": 1
}
```

### Marcar hábito como concluído

```http
POST /api/daily-entries/1/completed-habits
Authorization: Bearer <token>
Content-Type: application/json

{
  "habitId": 1,
  "completed": true,
  "notes": "Concluído durante a tarde."
}
```

### Consultar entrada por data

```http
GET /api/daily-entries/date/2026-05-13
Authorization: Bearer <token>
```

## Endpoints principais

- `GET /api/version`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `POST /api/habits`
- `GET /api/habits`
- `GET /api/habits/{id}`
- `PUT /api/habits/{id}`
- `DELETE /api/habits/{id}`
- `GET /api/habits/{id}/history`
- `POST /api/daily-entries`
- `GET /api/daily-entries/date/{date}`
- `PUT /api/daily-entries/{id}`
- `POST /api/daily-entries/{entryId}/planned-habits`
- `GET /api/daily-entries/{entryId}/planned-habits`
- `DELETE /api/daily-entries/{entryId}/planned-habits/{habitId}`
- `POST /api/daily-entries/{entryId}/completed-habits`
- `GET /api/daily-entries/{entryId}/completed-habits`
- `PUT /api/daily-entries/{entryId}/completed-habits/{habitId}`

## CORS

A API permite chamadas para `/api/**` a partir de:

- `http://localhost:5173`
- `http://localhost:3000`
