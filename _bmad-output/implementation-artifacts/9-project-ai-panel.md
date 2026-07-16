---
story_key: 9-project-ai-panel
epic: 9
---

# Epic 9 — Projekt-Assistent (Handoff)

Status: review

## Scope

Umsetzung des Pakets `handoff/project-ai-panel` (Stories 9.1–9.5 zusammengefasst):

- `ApprovedProjectDataReader` mit stabilen `factId`s
- `GET /api/projects/{id}/ai/analysis?refresh=`
- `POST /api/projects/{id}/ai/questions`
- Mock-`AiModelClient` (kein API-Key, CI-fähig)
- Evidenz-Validator
- Angular `app-project-ai-panel` mit Tabs Überblick / Maßnahmen / Fragen

## Grenzen

- Kein echter Modellprovider (nur `app.ai.provider=mock`)
- Maßnahmen-/Kapazitätsdaten fehlen (Epic 7) → als `missingData`
- Entwurf bleibt lokal im UI, kein Persistieren

## Change Log

- 2026-07-16: Projekt-Assistent laut Handoff-Paket implementiert
