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

## Calendario

Endpoints usados para integracao do calendario:

- `POST /calendar/month`: carrega todos os dias de um mes em uma unica requisicao
- `GET /calendar/days/{date}`: carrega um unico dia para resumos pontuais
- `PUT /calendar/days/{date}`: salva a edicao manual de um dia em uma unica requisicao

Contrato principal de `POST /calendar/month`:

- `year`
- `month`

Contrato principal de `PUT /calendar/days/{date}`:

- `description`
- `habits`

Cada item de `habits`:

- `habitId`
- `completed`

O frontend nao deve montar o calendario fazendo chamadas por dia. A tela de calendario deve carregar o mes por `/calendar/month` e salvar alteracoes por `/calendar/days/{date}`.
Resumos pontuais, como o resumo do dia atual na tela de habitos, devem usar cache do frontend e fallback para `/calendar/days/{date}`.

## Estado atual do frontend

Servicos atuais:

- `src/services/api.js`: cliente HTTP central para a API
- `src/services/calendarService.js`: usa endpoints agregados de calendario
- `src/services/habitService.js`: usa endpoints reais de habitos
- `src/services/profileService.js`: usa endpoints reais de perfil e cache local apenas para configuracoes/token

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
