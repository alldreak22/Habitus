# Notas técnicas do Habitus

Este documento concentra decisões e pendências que são maiores do que um README de execução, mas ainda importantes para avaliar a direção do projeto.

## Estado atual

- A aplicação principal está dividida entre `frontend/habitus-web` e `backend/habitus-api`.
- A API Java já persiste usuários, hábitos, entradas diárias, planejamento e conclusão de hábitos em SQLite.
- O front-end já possui navegação e telas principais, mas parte dos dados ainda vem de mocks em `src/content`.
- `services/habitus-stats` existe como espaço reservado, sem implementação versionada.
- `scripts/habitus_schema_completo.sql` funciona como referência de modelagem, mas não é automaticamente o contrato final da API atual.

## Contrato de hábito

O modelo atual da API aceita campos modernos e campos de compatibilidade:

- Nome: `name` e `title`.
- Frequência: `targetFrequency` e `frequencyType`.
- Horários: `suggestedTimes` e `reminderTimes`.

Para avançar sem ambiguidade, vale escolher um contrato principal. A direção mais coerente com a entidade atual é:

```json
{
  "title": "Beber água",
  "icon": "water_drop",
  "color": "#2f80ed",
  "description": "Beber água ao longo do dia",
  "reminder": true,
  "frequencyType": "EVERY_DAY",
  "reminderTimes": ["08:00", "14:00", "20:00"],
  "frequencyDays": [1, 2, 3, 4, 5],
  "status": "ACTIVE"
}
```

Os aliases podem continuar por um tempo para não quebrar o front ou coleções de teste, mas a documentação e novas telas deveriam usar um formato único.

## Integração front-end/API

O front já tem `VITE_API_BASE_URL` e `src/services/api.js`, então a próxima etapa técnica é trocar serviços mockados por chamadas reais:

- `habitService.js` deve consumir `/habits`.
- `calendarService.js` deve consumir `/daily-entries`, `/planned-habits` e `/completed-habits`.
- Login e cadastro devem persistir o token retornado pela API.
- `apiRequest` deve anexar `Authorization: Bearer <token>` quando houver sessão.

## Banco e migrações

Hoje o Hibernate usa `ddl-auto=update`, o que é suficiente para desenvolvimento local, mas não é uma estratégia boa para evolução controlada do schema.

Quando o modelo estabilizar, faz sentido introduzir migrações com Flyway ou Liquibase e decidir se `scripts/habitus_schema_completo.sql` vira:

- apenas documentação de referência;
- uma migração inicial;
- ou um artefato removido para evitar divergência.

