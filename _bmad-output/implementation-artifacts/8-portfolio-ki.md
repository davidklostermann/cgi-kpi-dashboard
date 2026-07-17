---
story_key: 8-portfolio-ki
epic: 8
---

# Epic 8 — KI-Portfolioanalyse

Status: review

## Scope

- Freigegebene Portfolio-Fakten über `PortfolioKpiReader` + `PortfolioTableReader` (AD-2)
- `GET /api/portfolio/ai/trend-analysis` mit Text, Disclaimer, Top-3
- `app-portfolio-ai-panel` auf der Portfolio-Seite (Loading/Error/Empty/Retry)
- Mock-Default; Gemini optional über `APP_AI_PROVIDER=gemini`

## Endpoints

- `GET /api/portfolio/ai/trend-analysis`

## Change Log

- 2026-07-16: Epic 8 implementiert (Mock + Gemini-fähig)
