# AGENTS

Este arquivo orienta como o Codex deve trabalhar neste repositorio.

O objetivo deste arquivo nao e documentar todo o projeto.
O objetivo e explicar onde buscar contexto, onde buscar tarefas e como atualizar o controle.

## Instrucoes

Antes de fazer qualquer alteracao, leia:

- `vault/01-projeto/arquitetura.md`
- `vault/02-backend/endpoints.md`
- `vault/04-codex/tarefa-atual.md`

A tarefa principal sempre esta em:

```txt
vault/04-codex/tarefa-atual.md
```

Execute apenas o que estiver em `tarefa-atual.md`.

Nao invente tarefas extras durante a execucao.

Se o usuario requisitar algo fora do escopo de `tarefa-atual.md`, executar somente com confirmacao explicita do usuario e, ao final, registrar essa entrega em `tarefa-atual.md` como item concluido.

Nao altere arquitetura sem pedido explicito.

Nao crie endpoints novos sem verificar antes o arquivo:

```txt
vault/02-backend/endpoints.md
```

Se um endpoint necessario ja existir, use ele.

Se um endpoint parecer necessario mas nao existir, informe isso ao final em vez de criar automaticamente.

## Estrutura

Estrutura principal do repositorio:

```txt
backend/habitus-api
frontend/habitus-web
services/habitus-stats
scripts
docs
vault
```

Estrutura principal do vault:

```txt
vault/01-projeto/arquitetura.md
vault/01-projeto/endpoints.md
vault/01-projeto/integracao-api.md
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

Servico futuro:

```txt
services/habitus-stats
```

Scripts auxiliares:

```txt
scripts
```

## Fluxo

Fluxo atual da aplicacao:

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend consome a API Java.
O backend aplica regras e persiste dados.
O SQLite e o banco local.
O servico de estatisticas ainda e futuro.
Scripts sao auxiliares e nao fazem parte do fluxo principal.

## Regras

- Manter o projeto simples.
- Nao adicionar dependencias novas sem necessidade.
- Nao adicionar Docker, filas, mensageria ou arquitetura distribuida sem pedido explicito.
- Nao substituir SQLite sem pedido explicito.
- Nao alterar o modelo de autenticacao sem pedido explicito.
- Nao reescrever arquivos inteiros quando uma alteracao pequena resolver.
- Nao criar regra de negocio no frontend.
- Nao usar scripts para resolver fluxo principal da aplicacao.
- Nao adicionar varias tarefas futuras sem necessidade.
- Nao assumir que `sqlite3` CLI esta disponivel na maquina.
- Para executar SQL local no SQLite, usar Python (`python` + modulo `sqlite3`) quando for necessario rodar query/script.

## Controle de tarefas

Toda execucao deve seguir o arquivo:

```txt
vault/04-codex/tarefa-atual.md
```

Ao finalizar uma tarefa, marcar a tarefa como concluida adicionando:

```txt
(CONCLUIDO)
```

Exemplo:

```txt
- Integrar login com API (CONCLUIDO)
```

Se a tarefa tiver subtarefas, marcar apenas as subtarefas realmente concluidas.

Organizacao obrigatoria de `tarefa-atual.md`:

- manter itens ainda nao feitos no topo
- criar uma secao `## Concluidos` no final
- mover para essa secao todos os itens ja concluidos

Depois de concluir, adicionar no maximo uma proxima tarefa sugerida em:

```txt
vault/04-codex/tarefas-futuras.md
```

A tarefa futura deve:

- estar relacionada ao contexto atual
- ser simples
- ser objetiva
- nao fugir da arquitetura do projeto
- nao criar escopo desnecessario

## Navegacao

Use estes arquivos como referencia:

Arquitetura:

```txt
vault/01-projeto/arquitetura.md
```

Endpoints existentes:

```txt
vault/01-projeto/endpoints.md
```

Integracao do frontend:

```txt
vault/01-projeto/integracao-api.md
```

Tarefa atual:

```txt
vault/02-tarefas/tarefa-atual.md
```

Tarefas futuras:

```txt
vault/02-tarefas/tarefa-futura.md
```

## Saida esperada

Ao terminar, informe de forma curta:

- o que foi alterado
- quais arquivos foram alterados
- se a tarefa foi marcada como concluida
- se foi adicionada alguma tarefa futura
