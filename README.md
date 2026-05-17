# Habitus

Habitus é um projeto de diário de hábitos com front-end em React e API principal em Java Spring Boot. A ideia central é permitir que o usuário planeje hábitos, registre entradas diárias e acompanhe a execução ao longo do tempo.

## Estrutura do repositório

```txt
backend/habitus-api       API principal, regras de negócio e persistência
frontend/habitus-web      Interface web em React
services/habitus-stats    Espaço reservado para um serviço de estatísticas
scripts                   Scripts auxiliares e artefatos de banco
docs                      Documentação técnica complementar
```

## Stack atual

- **Front-end:** React 19, React Router 7 e Vite 7.
- **Back-end:** Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Maven e SQLite.
- **Banco local:** SQLite em `backend/habitus-api/data/habitus.db` por padrão.

## Como rodar localmente

### API

```bash
cd backend/habitus-api
mvn spring-boot:run
```

A API sobe em `http://localhost:8080` e expõe as rotas sob `/api`.

### Front-end

```bash
cd frontend/habitus-web
npm install
npm run dev
```

O Vite usa `http://localhost:5173` por padrão. A URL da API pode ser ajustada em `frontend/habitus-web/.env`, usando `frontend/habitus-web/.env.example` como base.

## Fluxo atual

```txt
React/Vite -> Spring Boot API -> SQLite
```

O front-end já tem a estrutura de integração HTTP em `src/services/api.js`, mas algumas telas ainda usam dados mockados em `src/content`. A API já possui endpoints para autenticação simples, usuário atual, hábitos, entradas diárias, planejamento e conclusão de hábitos.

## Documentação dos módulos

- [API](backend/habitus-api/README.md)
- [Front-end](frontend/habitus-web/README.md)
- [Notas técnicas](docs/notas-tecnicas.md)

## Observações

- `services/habitus-stats` existe como diretório, mas ainda não possui implementação versionada.
- `scripts/habitus_schema_completo.sql` descreve um modelo SQL de referência que não deve ser tratado automaticamente como contrato final da API sem revisão.
