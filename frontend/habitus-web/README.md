# Habitus Web

Front-end web do Habitus, implementado com React 19, React Router 7 e Vite 7.

## Requisitos

- Node.js compatível com Vite 7.
- npm.

## Rodando localmente

```bash
npm install
npm run dev
```

O Vite sobe em `http://localhost:5173` por padrão.

## Configuração

Crie um arquivo `.env` a partir de `.env.example` se precisar alterar a URL da API:

```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

O cliente HTTP base fica em `src/services/api.js`.

## Scripts

```bash
npm run dev      # servidor local de desenvolvimento
npm run build    # build de produção
npm run preview  # preview local do build
```

## Rotas

- `/login`
- `/cadastro`
- `/calendario`
- `/habitos`
- `/evolucao`
- `/perfil`
- `/configuracoes`

A rota raiz redireciona para `/calendario`.

## Estrutura

```txt
src/
  app/          Configuração da aplicação e rotas
  components/   Componentes reutilizáveis
    calendar/   Componentes da tela de calendário
    habits/     Componentes ligados a hábitos
    layout/     Sidebar e topo
    profile/    Componentes ligados ao usuário
    settings/   Componentes de configurações
  content/      Conteúdo e dados mockados no formato esperado pela UI
  layouts/      Estruturas comuns de tela
  pages/        Telas da aplicação
  services/     Integrações externas e adaptação de dados
  styles/       Estilos globais e tokens visuais
  utils/        Funções utilitárias
```

## Estado atual da integração

- `src/services/api.js` já centraliza a base para chamadas HTTP reais.
- `calendarService.js`, `habitService.js` e `profileService.js` ainda usam dados locais em `src/content` ou `localStorage`.
- A tela de calendário está componentizada em `src/components/calendar` e montada por `src/pages/CalendarPage.jsx`.
- O perfil persiste alterações locais via `localStorage`, usando a chave `habitus-profile-overview`.

## Próximos pontos naturais

- Substituir os mocks de hábitos e calendário por chamadas reais à API.
- Definir autenticação no front após login/cadastro e anexar `Authorization: Bearer <token>` nas chamadas protegidas.
- Alinhar o contrato visual de hábitos do front com os campos aceitos pela API (`title`, `icon`, `color`, `frequencyType`, `reminderTimes` e `frequencyDays`).
