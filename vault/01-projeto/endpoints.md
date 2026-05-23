# Endpoints

## Autenticacao

- `POST /api/auth/register`: cadastra um usuario e retorna os dados de autenticacao.
- `POST /api/auth/login`: autentica um usuario e retorna os dados de autenticacao.
- `GET /api/users/me`: retorna os dados do usuario autenticado.

## Versao

- `GET /api/version`: retorna o nome, a versao e o identificador completo da aplicacao.

## Habitos

- `POST /api/habits`: cria um habito para o usuario autenticado.
- `GET /api/habits`: lista os habitos do usuario autenticado.
- `GET /api/habits/{id}`: retorna um habito do usuario autenticado pelo id.
- `PUT /api/habits/{id}`: atualiza um habito do usuario autenticado pelo id.
- `DELETE /api/habits/{id}`: desativa um habito do usuario autenticado pelo id.
- `GET /api/habits/{id}/history`: lista o historico de conclusoes de um habito do usuario autenticado.

## Entradas diarias

- `POST /api/daily-entries`: cria uma entrada diaria para o usuario autenticado.
- `GET /api/daily-entries/date/{date}`: retorna uma entrada diaria do usuario autenticado pela data.
- `PUT /api/daily-entries/{id}`: atualiza uma entrada diaria do usuario autenticado pelo id.

## Habitos planejados

- `POST /api/daily-entries/{entryId}/planned-habits`: adiciona um habito planejado a uma entrada diaria do usuario autenticado.
- `GET /api/daily-entries/{entryId}/planned-habits`: lista os habitos planejados de uma entrada diaria do usuario autenticado.
- `DELETE /api/daily-entries/{entryId}/planned-habits/{habitId}`: remove um habito planejado de uma entrada diaria do usuario autenticado.

## Habitos concluidos

- `POST /api/daily-entries/{entryId}/completed-habits`: registra a conclusao de um habito em uma entrada diaria do usuario autenticado.
- `GET /api/daily-entries/{entryId}/completed-habits`: lista os habitos concluidos de uma entrada diaria do usuario autenticado.
- `PUT /api/daily-entries/{entryId}/completed-habits/{habitId}`: atualiza a conclusao de um habito em uma entrada diaria do usuario autenticado.
