# Habitus Web

Front-end React do Habitus.

## Rodando localmente

```bash
npm install
npm run dev
```

Crie um arquivo `.env` a partir de `.env.example` se precisar alterar a URL da API.

## Estrutura

```txt
src/
  app/          Configuracao da aplicacao e rotas
  components/   Componentes reutilizaveis
    calendar/   Componentes da tela de calendario
    layout/     Sidebar e topo
    profile/    Componentes ligados ao usuario
  data/         Dados mockados no formato esperado da API
  layouts/      Estruturas comuns de tela
  pages/        Telas da aplicacao
  services/     Integracoes externas, como API HTTP
  styles/       Estilos globais e tokens
  utils/        Funcoes utilitarias
```

## Tela de calendario

A tela em `src/pages/CalendarPage.jsx` ja esta componentizada. Os dados passam por
`src/services/calendarService.js`, que hoje devolve mocks de `src/data/calendarMock.js`
e depois pode ser trocado por chamadas reais para a API.
