# Integracao

## Fluxo

```txt
frontend/habitus-web -> backend/habitus-api -> SQLite
```

O frontend consome a API HTTP do backend.
O backend aplica as regras de negocio e persiste os dados no SQLite.

## Base da API

No frontend, a URL base da API fica em:

```txt
frontend/habitus-web/src/services/api.js
```

Valor usado:

```txt
VITE_API_BASE_URL
```

Valor padrao:

```txt
http://localhost:8080/api
```

As chamadas devem usar caminhos relativos a essa base.

Exemplo:

```txt
/habits
```

Resultado:

```txt
http://localhost:8080/api/habits
```

## Cliente HTTP

Funcao central:

```txt
apiRequest(path, options)
```

Responsabilidades:

- montar a URL final usando `API_BASE_URL`
- enviar `Content-Type: application/json`
- repassar headers adicionais recebidos em `options.headers`
- lancar erro quando a resposta HTTP nao for OK
- retornar `null` para respostas `204`
- retornar JSON para as demais respostas OK

## Autenticacao

Endpoints:

- `POST /auth/register`: cadastra usuario
- `POST /auth/login`: autentica usuario com `login` (nick ou e-mail) e `password`
- `GET /users/me`: busca o usuario autenticado

Fluxo esperado:

```txt
nick ou email/senha -> login ou cadastro -> token -> Authorization: Bearer <token>
```

O backend espera o token no header:

```txt
Authorization: Bearer <token>
```

O token atual e simples, gerado pelo backend a partir do id do usuario e codificado em Base64 URL-safe.

## Habitos

Endpoints usados para integracao de habitos:

- `POST /habits`: cria habito
- `GET /habits`: lista habitos
- `GET /habits/{id}`: busca habito por id
- `PUT /habits/{id}`: atualiza habito
- `DELETE /habits/{id}`: desativa habito
- `GET /habits/{id}/history`: lista historico do habito

Contrato principal de entrada:

- `name`
- `title`
- `icon`
- `color`
- `description`
- `targetFrequency`
- `timesPerDay`
- `suggestedTimes`
- `reminder`
- `frequencyType`
- `status`
- `reminderTimes`
- `frequencyDays`

## Entradas diarias

Endpoints usados para integracao de entradas diarias:

- `POST /daily-entries`: cria entrada diaria
- `GET /daily-entries/date/{date}`: busca entrada diaria por data
- `PUT /daily-entries/{id}`: atualiza entrada diaria

Contrato principal de entrada:

- `entryDate`
- `markdownContent`
- `planningNotes`

## Habitos planejados

Endpoints usados para integracao de habitos planejados:

- `POST /daily-entries/{entryId}/planned-habits`: adiciona habito planejado
- `GET /daily-entries/{entryId}/planned-habits`: lista habitos planejados
- `DELETE /daily-entries/{entryId}/planned-habits/{habitId}`: remove habito planejado

Contrato principal de entrada:

- `habitId`

## Habitos concluidos

Endpoints usados para integracao de habitos concluidos:

- `POST /daily-entries/{entryId}/completed-habits`: registra conclusao de habito
- `GET /daily-entries/{entryId}/completed-habits`: lista habitos concluidos
- `PUT /daily-entries/{entryId}/completed-habits/{habitId}`: atualiza conclusao de habito

Contrato principal de entrada:

- `habitId`
- `completed`
- `notes`

## Estado atual do frontend

Servicos atuais:

- `src/services/api.js`: cliente HTTP central para a API
- `src/services/calendarService.js`: usa mocks de calendario, habitos e insights
- `src/services/habitService.js`: usa mock de habitos
- `src/services/profileService.js`: usa mock de perfil e `localStorage`

Dados mockados atuais:

- `src/content/calendarMock.json`
- `src/content/habitsMock.json`
- `src/content/productivityInsights.json`
- `src/content/profileMock.json`

## CORS

O backend permite chamadas locais a partir de:

- `http://localhost:5173`
- `http://localhost:3000`

Metodos permitidos:

- `GET`
- `POST`
- `PUT`
- `DELETE`
- `OPTIONS`

## Referencias

Endpoints documentados:

```txt
vault/01-projeto/endpoints.md
```

Arquitetura documentada:

```txt
vault/01-projeto/arquitetura.md
```
