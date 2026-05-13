# Habitus API

API principal do Habitus, implementada em Java 21 com Spring Boot, Spring Web, Spring Data JPA, Bean Validation, Maven e SQLite.

## Como rodar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080` e cria automaticamente o banco local em:

```text
data/habitus.db
```

O diretório `data/` e arquivos `*.db`, `*.sqlite` e `*.sqlite3` não devem ser versionados.

## Autenticação inicial

Esta primeira versão usa um token simples retornado pelo cadastro/login. Envie o token nas rotas protegidas:

```http
Authorization: Bearer <token>
```

Esse formato foi feito para ser substituído por JWT depois.

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

### Criar hábito

```http
POST /api/habits
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Tomar pelo menos 1L de agua",
  "description": "Beber agua ao longo do dia",
  "targetFrequency": "DAILY",
  "timesPerDay": 3,
  "suggestedTimes": "08:00,14:00,20:00"
}
```

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
  "notes": "Concluido durante a tarde."
}
```

### Consultar entrada por data

```http
GET /api/daily-entries/date/2026-05-13
Authorization: Bearer <token>
```

## Endpoints principais

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
