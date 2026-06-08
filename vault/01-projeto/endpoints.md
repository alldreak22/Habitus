# Endpoints

## Autenticação

- `POST /api/auth/register`: cadastra um usuário com `name`, `nick`, `email` e `password`, e retorna os dados de autenticação.
- `POST /api/auth/login`: autentica um usuário com `login` (nick ou e-mail) e `password`, e retorna os dados de autenticação.
- `GET /api/users/me`: retorna os dados do usuário autenticado.
- `PUT /api/users/me`: atualiza os dados do usuário autenticado, incluindo `picture`.
- `PUT /api/users/me/password`: altera a senha do usuário autenticado com `currentPassword` e `newPassword`.

Não há endpoint de recuperação de senha implementado.

## Versão

- `GET /api/version`: retorna o nome, a versão e o identificador completo da aplicação.

## Hábitos

- `POST /api/habits`: cria um hábito para o usuário autenticado.
- `GET /api/habits`: lista os hábitos do usuário autenticado.
- `GET /api/habits/{id}`: retorna um hábito do usuário autenticado pelo id.
- `PUT /api/habits/{id}`: atualiza um hábito do usuário autenticado pelo id.
- `DELETE /api/habits/{id}`: exclui um hábito do usuário autenticado pelo id.
- `GET /api/habits/{id}/history`: lista o histórico de conclusões de um hábito do usuário autenticado.

## Calendário

- `POST /api/calendar/month`: retorna todos os dias de um mês com bolinhas, chips de horário quando existirem, hábitos do dia e entradas manuais, usando `year` e `month`.
- `GET /api/calendar/days/{date}`: retorna um único dia do calendário com resumo, bolinhas/chips de horário e hábitos do dia.
- `PUT /api/calendar/days/{date}`: salva a edição manual de um dia, com `description` e lista de `habits` contendo `habitId`, `completed` e, quando houver horários, `timeSlots` com `time` (`HH:mm`) e `completed`.

## Métricas e Relatórios

Não há endpoints de métricas ou relatórios implementados na API principal. Esse escopo fica para o futuro serviço/API C# em `services/habitus-stats`.
