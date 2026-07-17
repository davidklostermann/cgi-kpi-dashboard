---
story_key: 9-project-ai-panel
epic: 9
---

# Epic 9 — Projekt-Assistent

Status: review

## Scope

- `ApprovedProjectDataReader` mit stabilen `factId`s
- `GET /api/projects/{id}/ai/analysis?refresh=`
- `POST /api/projects/{id}/ai/questions`
- Mock- und Gemini-`AiModelClient`
- Evidenz-Validator (unbekannte Fact-IDs werden verworfen)
- `app-project-ai-panel` auf der Detailseite aktiv (Tabs Überblick / Maßnahmen / Fragen)

## Produktstand (2026-07-16)

- Detailseite rendert `app-project-ai-panel` (Placeholder ersetzt)
- Maßnahmen nur als Vorschläge, kein Persistieren
- Provider: `APP_AI_PROVIDER=mock|gemini`, Key nur `GEMINI_API_KEY`

## Change Log

- 2026-07-16: Projekt-Assistent implementiert
- 2026-07-16: Panel auf Detailseite aktiviert; Gemini-Anbindung ergänzt
