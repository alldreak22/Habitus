# Endpoints

## Autenticacao

- `POST /api/auth/register`: cadastra um usuario com `name`, `nick`, `email` e `password`, e retorna os dados de autenticacao.
- `POST /api/auth/login`: autentica um usuario com `login` (nick ou e-mail) e `password`, e retorna os dados de autenticacao.
- `GET /api/users/me`: retorna os dados do usuario autenticado.
- `PUT /api/users/me`: atualiza os dados do usuario autenticado, incluindo `picture`.
- `PUT /api/users/me/password`: altera a senha do usuario autenticado com `currentPassword` e `newPassword`.

## Versao

- `GET /api/version`: retorna o nome, a versao e o identificador completo da aplicacao.

## Habitos

- `POST /api/habits`: cria um habito para o usuario autenticado.
- `GET /api/habits`: lista os habitos do usuario autenticado.
- `GET /api/habits/{id}`: retorna um habito do usuario autenticado pelo id.
- `PUT /api/habits/{id}`: atualiza um habito do usuario autenticado pelo id.
- `DELETE /api/habits/{id}`: exclui um habito do usuario autenticado pelo id.
- `GET /api/habits/{id}/history`: lista o historico de conclusoes de um habito do usuario autenticado.

## Calendario

- `POST /api/calendar/month`: retorna todos os dias de um mes com bolinhas, habitos do dia e entradas manuais, usando `year` e `month`.
- `GET /api/calendar/days/{date}`: retorna um unico dia do calendario com resumo, bolinhas e habitos do dia.
- `PUT /api/calendar/days/{date}`: salva a edicao manual de um dia, com `description` e lista de `habits` contendo `habitId` e `completed`.
