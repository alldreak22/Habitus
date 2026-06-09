# Integração

## Fluxo

```txt
frontend/habitus-web -> backend/habitus-api -> SQLite
```

O frontend consome a API HTTP do backend. O backend aplica as regras de negócio e persiste os dados no SQLite.

## Base da API

No frontend, a URL base da API fica em:

```txt
frontend/habitus-web/src/services/api.js
```

Valor usado:

```txt
VITE_API_BASE_URL
```

Valor padrão:

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

Função central:

```txt
apiRequest(path, options)
```

Responsabilidades:

- montar a URL final usando `API_BASE_URL`
- enviar `Content-Type: application/json`
- anexar token bearer quando existir sessão autenticada
- repassar headers adicionais recebidos em `options.headers`
- lançar erro quando a resposta HTTP não for OK
- retornar `null` para respostas `204`
- retornar JSON para as demais respostas OK

## Autenticação

Endpoints:

- `POST /auth/register`: cadastra usuário
- `POST /auth/login`: autentica usuário com `login` (nick ou e-mail) e `password`
- `GET /users/me`: busca o usuário autenticado
- `PUT /users/me`: atualiza perfil e foto
- `PUT /users/me/password`: altera senha autenticada

Fluxo esperado:

```txt
nick ou email/senha -> login ou cadastro -> token -> Authorization: Bearer <token>
```

O backend espera o token no header:

```txt
Authorization: Bearer <token>
```

O token atual é simples, gerado pelo backend a partir do id do usuário e codificado em Base64 URL-safe.

Não há endpoint real para recuperação de senha. A rota `/recuperar-senha` existe no frontend apenas como tela.

## Hábitos

Endpoints usados para integração de hábitos:

- `POST /habits`: cria hábito
- `GET /habits`: lista hábitos
- `GET /habits/{id}`: busca hábito por id
- `PUT /habits/{id}`: atualiza hábito
- `DELETE /habits/{id}`: exclui hábito
- `GET /habits/{id}/history`: lista histórico do hábito

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

## Calendário

Endpoints usados para integração do calendário:

- `POST /calendar/month`: carrega todos os dias de um mês em uma única requisição
- `GET /calendar/days/{date}`: carrega um único dia para resumos pontuais
- `PUT /calendar/days/{date}`: salva a edição manual de um dia em uma única requisição

Contrato principal de `POST /calendar/month`:

- `year`
- `month`

Contrato principal de `PUT /calendar/days/{date}`:

- `description`
- `habits`

Cada item de `habits`:

- `habitId`
- `completed`
- `timeSlots`, quando o hábito tiver horários

Cada item de `timeSlots`:

- `time` em formato `HH:mm`
- `completed`

O frontend não deve montar o calendário fazendo chamadas por dia. A tela de calendário deve carregar o mês por `/calendar/month` e salvar alterações por `/calendar/days/{date}`. Resumos pontuais devem usar cache no frontend e fallback para `/calendar/days/{date}`.

## Estado Atual do Frontend

Serviços atuais:

- `src/services/api.js`: cliente HTTP central para a API
- `src/services/statsService.js`: cliente HTTP para o serviço/API C# de métricas
- `src/services/authService.js`: autenticação, sessão e usuário atual
- `src/services/calendarService.js`: endpoints agregados de calendário
- `src/services/habitService.js`: endpoints reais de hábitos
- `src/services/profileService.js`: endpoints reais de perfil e cache local apenas para configurações/token

## Serviço de Métricas

Relatórios, cálculos e métricas são tratados pelo serviço/API C# em:

```txt
services/habitus-stats
```

No frontend, a URL base do serviço de métricas fica em:

```txt
VITE_STATS_API_BASE_URL
```

Valor padrão:

```txt
http://localhost:5090/api
```

Endpoint usado pela tela de evolução:

- `GET /stats/evolution?days=30`

O serviço C# reutiliza `Authorization: Bearer <token>` e lê o SQLite local em modo somente leitura. O frontend não deve calcular métricas de negócio; deve apenas consumir o contrato retornado pelo serviço.

## CORS

O backend permite chamadas locais a partir de:

- `http://localhost:5173`
- `http://localhost:3000`

Métodos permitidos:

- `GET`
- `POST`
- `PUT`
- `DELETE`
- `OPTIONS`

O serviço C# de métricas também permite chamadas locais a partir de:

- `http://localhost:5173`
- `http://localhost:3000`

## Referências

Endpoints documentados:

```txt
vault/01-projeto/endpoints.md
```

Arquitetura documentada:

```txt
vault/01-projeto/arquitetura.md
```
