# Habitus

Habitus é uma aplicação local de diário de hábitos. O objetivo é permitir que o usuário cadastre hábitos, acompanhe a rotina em calendário, registre conclusões por dia e edite informações de perfil.

## Estrutura do Repositório

```txt
backend/habitus-api       API principal, regras de negócio e persistência
frontend/habitus-web      Interface web em React
services/habitus-stats    Espaço reservado para serviço futuro de métricas
scripts                   Scripts auxiliares e artefatos de banco
vault                     Documentação e controle de tarefas do projeto
```

## Stack Atual

- Frontend: React 19, React Router 7, Vite 7 e JavaScript.
- Backend: Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Maven e SQLite.
- Banco local: SQLite em `backend/habitus-api/data/habitus.db` por padrão.

## Como Rodar Localmente

API:

```bash
cd backend/habitus-api
mvn spring-boot:run
```

A API sobe em `http://localhost:8080` e expõe rotas sob `/api`.

Frontend:

```bash
cd frontend/habitus-web
npm install
npm run dev
```

O Vite usa `http://localhost:5173` por padrão. A URL da API pode ser ajustada em `frontend/habitus-web/.env` com `VITE_API_BASE_URL`.

## Fluxo Atual

```txt
React/Vite -> Spring Boot API -> SQLite
```

O frontend consome a API Java por meio dos services em `src/services`. A API concentra autenticação local, regras de hábitos, perfil e calendário agregado. O SQLite é o banco local de desenvolvimento.

## Funcionalidades Implementadas

- Cadastro e login com token bearer simples.
- Perfil do usuário com atualização de dados, foto e senha.
- CRUD de hábitos com frequência, dias personalizados e horários opcionais.
- Calendário mensal agregado com resumo por dia, bolinhas de hábitos e horários.
- Edição manual de dia com conclusão de hábitos e subhorários.
- Configurações locais simples de tema, idioma e preferências visuais.

## Pendências Futuras

- Implementar fluxo real de recuperação de senha para a tela `/recuperar-senha`.
- Criar o serviço/API C# em `services/habitus-stats` para relatórios, cálculos e métricas consumidas pelo frontend.

## Documentação

- [API](backend/habitus-api/README.md)
- [Frontend](frontend/habitus-web/README.md)
- [Arquitetura](vault/01-projeto/arquitetura.md)
- [Endpoints](vault/01-projeto/endpoints.md)
- [Integração](vault/01-projeto/integracao.md)

## Observações

- `services/habitus-stats` ainda não possui implementação versionada e não faz parte do fluxo principal atual.
- Scripts em `scripts/` são auxiliares e não devem resolver o fluxo principal da aplicação.
