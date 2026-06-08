# Habitus Web

Frontend web do Habitus, implementado com React 19, React Router 7, Vite 7 e JavaScript.

## Requisitos

- Node.js compatível com Vite 7.
- npm.

## Rodando Localmente

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
- `/recuperar-senha`
- `/calendario`
- `/habitos`
- `/evolucao`
- `/perfil`
- `/configuracoes`

A rota raiz e rotas desconhecidas redirecionam para `/login`.

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
  content/      Textos e opções estáticas de interface
  layouts/      Estruturas comuns de tela
  pages/        Telas da aplicação
  services/     Integrações com a API e adaptação de dados
  styles/       Estilos globais e tokens visuais
  utils/        Funções utilitárias
```

## Estado Atual da Integração

- `src/services/api.js` centraliza chamadas HTTP e token bearer.
- `authService.js` integra login, cadastro, usuário atual e atualização de senha.
- `habitService.js` usa endpoints reais de hábitos.
- `calendarService.js` usa endpoints agregados de mês e dia.
- `profileService.js` usa endpoints reais de perfil e mantém cache local apenas para token, usuário e configurações simples.

## Observações

- A tela `/recuperar-senha` existe visualmente, mas ainda não tem fluxo real com backend.
- A tela de evolução ainda deve receber métricas quando o serviço/API C# de estatísticas for implementado.
