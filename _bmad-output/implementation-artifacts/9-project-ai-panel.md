---
story_key: 9-project-ai-panel
epic: 9
---

# Epic 9 — Projekt-Assistent (Handoff)

Status: in-progress

## Scope (Codebase)

Umsetzung des Pakets `handoff/project-ai-panel` (Stories 9.1–9.5):

- `ApprovedProjectDataReader` mit stabilen `factId`s
- `GET /api/projects/{id}/ai/analysis?refresh=`
- `POST /api/projects/{id}/ai/questions`
- Mock-`AiModelClient` (kein API-Key, CI-fähig)
- Evidenz-Validator
- Angular `app-project-ai-panel` mit Tabs Überblick / Maßnahmen / Fragen (Komponente + Specs vorhanden)

## Produktstand (2026-07-16)

- **Backend und experimenteller Frontend-Code bleiben erhalten** (nicht gelöscht).
- Auf der **Projekt-Detailseite** wird aktuell **`app-ai-panel-placeholder`** gerendert, nicht `app-project-ai-panel`.
- Story **9.4 ist im Produkt nicht vollständig aktiv** — KI-Spalte zeigt Platzhaltertext für Epic 8/9.

## Grenzen

- Kein echter Modellprovider (nur `app.ai.provider=mock`)
- Maßnahmen-/Kapazitätsdaten über Epic 7 (`issues-actions`, `capacity`) teilweise verfügbar
- Entwurf bleibt lokal im UI, kein Persistieren

## Change Log

- 2026-07-16: Projekt-Assistent laut Handoff-Paket implementiert
- 2026-07-16: Detailseite wieder auf `app-ai-panel-placeholder` umgestellt; 9.4 Backend/Komponente vorhanden, aber nicht produktiv aktiviert
