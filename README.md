# Habitus

Habitus é um sistema de diário de hábitos dividido em três serviços principais:

- Front-end em React
- Back-end principal em Java Spring Boot
- Microserviço de estatísticas em C# ASP.NET Core
- Scripts auxiliares em Python

---

## Estrutura

```txt
frontend/habitus-web      Interface web
backend/habitus-api       API principal e persistência
services/habitus-stats    Cálculo de estatísticas
scripts/python            Scripts auxiliares
docs                      Documentação técnica
```

---

## Fluxo
```
React → Java Spring Boot → Banco de dados
React → Java Spring Boot → C# Stats Service
```
