# AGENTS

Este arquivo orienta como o Codex deve trabalhar neste repositório.

O objetivo deste arquivo não é documentar todo o projeto. O objetivo é explicar onde buscar contexto, onde buscar tarefas e como atualizar o controle.

## Instruções

Antes de fazer qualquer alteração, leia:

- `vault/01-projeto/arquitetura.md`
- `vault/01-projeto/endpoints.md`
- `vault/02-tarefas/tarefa-atual.md`

A tarefa principal sempre está em:

```txt
vault/02-tarefas/tarefa-atual.md
```

Execute apenas o que estiver em `tarefa-atual.md`.

Não invente tarefas extras durante a execução.

Se o usuário requisitar algo fora do escopo de `tarefa-atual.md`, executar somente com confirmação explícita do usuário e, ao final, registrar essa entrega em `tarefa-atual.md` como item concluído.

Não altere arquitetura sem pedido explícito.

Não crie endpoints novos sem verificar antes o arquivo:

```txt
vault/01-projeto/endpoints.md
```

Se um endpoint necessário já existir, use ele.

Se um endpoint parecer necessário mas não existir, informe isso ao final em vez de criar automaticamente.

## Estrutura

Estrutura principal do repositório:

```txt
backend/habitus-api
frontend/habitus-web
services/habitus-stats
scripts
vault
```

Estrutura principal do vault:

```txt
vault/01-projeto/arquitetura.md
vault/01-projeto/endpoints.md
vault/01-projeto/integracao.md
vault/01-projeto/resumo.md
vault/02-tarefas/tarefa-atual.md
vault/02-tarefas/tarefa-futura.md
```

## Stack

Frontend:

```txt
React, React Router, Vite e JavaScript.
```

Backend:

```txt
Java, Spring Boot, Maven e SQLite.
```

Serviço futuro:

```txt
services/habitus-stats em C# para métricas, relatórios e cálculos.
```

Scripts auxiliares:

```txt
scripts
```

## Fluxo

Fluxo atual da aplicação:

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend consome a API Java. O backend aplica regras e persiste dados. O SQLite é o banco local. O serviço C# de estatísticas ainda é futuro e não faz parte do fluxo principal. Scripts são auxiliares e não fazem parte do fluxo principal.

## Estado Atual

- Login, cadastro, hábitos, calendário e perfil estão integrados com a API principal.
- A rota `/recuperar-senha` existe no frontend, mas ainda não há endpoint real de recuperação de senha.
- A tela de evolução ainda não possui métricas reais.
- Relatórios, cálculos e métricas ficam para o futuro serviço/API C# em `services/habitus-stats`.

## Regras

- Manter o projeto simples.
- Não adicionar dependências novas sem necessidade.
- Não adicionar Docker, filas, mensageria ou arquitetura distribuída sem pedido explícito.
- Não substituir SQLite sem pedido explícito.
- Não alterar o modelo de autenticação sem pedido explícito.
- Não reescrever arquivos inteiros quando uma alteração pequena resolver.
- Não criar regra de negócio no frontend.
- Não usar scripts para resolver fluxo principal da aplicação.
- Não adicionar várias tarefas futuras sem necessidade.
- Não assumir que `sqlite3` CLI está disponível na máquina.
- Para executar SQL local no SQLite, usar Python (`python` + módulo `sqlite3`) quando for necessário rodar query/script.
- Para consultas locais de SQLite, preferir o utilitário somente-leitura `python scripts/python/sqlite_query.py "select ..."` ou `python scripts/python/sqlite_query.py "pragma ..."` para evitar depender do CLI `sqlite3`.
- O banco SQLite local é descartável durante o desenvolvimento. Quando uma mudança pedida exigir alterar estrutura ou formato de dados, não manter código legado apenas para compatibilidade com registros antigos; prefira ajustar o modelo atual e, se necessário, dropar/recriar ou normalizar o banco local de desenvolvimento.
- Para dados do calendário, usar os endpoints agregados `/api/calendar/month` e `/api/calendar/days/{date}`. Não montar o mês com chamadas por dia. Para resumos pontuais de um único dia, usar `/api/calendar/days/{date}` com cache no frontend.

## Controle de Tarefas

Toda execução deve seguir o arquivo:

```txt
vault/02-tarefas/tarefa-atual.md
```

Ao finalizar uma tarefa, marcar a tarefa como concluída adicionando:

```txt
(CONCLUIDO)
```

Exemplo:

```txt
- Integrar login com API (CONCLUIDO)
```

Se a tarefa tiver subtarefas, marcar apenas as subtarefas realmente concluídas.

Organização obrigatória de `tarefa-atual.md`:

- manter itens ainda não feitos no topo
- criar uma seção `## Concluidos` no final
- mover para essa seção todos os itens já concluídos

Depois de concluir, adicionar no máximo uma próxima tarefa sugerida em:

```txt
vault/02-tarefas/tarefa-futura.md
```

A tarefa futura deve:

- estar relacionada ao contexto atual
- ser simples
- ser objetiva
- não fugir da arquitetura do projeto
- não criar escopo desnecessário

## Navegação

Use estes arquivos como referência:

Arquitetura:

```txt
vault/01-projeto/arquitetura.md
```

Endpoints existentes:

```txt
vault/01-projeto/endpoints.md
```

Integração do frontend:

```txt
vault/01-projeto/integracao.md
```

Tarefa atual:

```txt
vault/02-tarefas/tarefa-atual.md
```

Tarefas futuras:

```txt
vault/02-tarefas/tarefa-futura.md
```

## Saída Esperada

Ao terminar, informe de forma curta:

- o que foi alterado
- quais arquivos foram alterados
- se a tarefa foi marcada como concluída
- se foi adicionada alguma tarefa futura
