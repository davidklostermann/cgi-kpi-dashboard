---
story_key: 6-7-berichtsstandsvergleich
epic: 6
---

# Story 6.7: Berichtsstandsvergleich (FR-21)

Status: done

## Acceptance Criteria

1. `GET /api/projects/{id}/trends` mit Deltas aus Snapshots.
2. UI-Hinweis wenn vorheriger Berichtsstand fehlt.
3. `app-project-report-comparison` als **eigenständiger Faktenbereich** auf der Projekt-Detailseite (nach Phasen/Gantt, nicht unter Management Insights).

## Detailseiten-Reihenfolge (Fakten-Spalte)

KPIs → Probleme/Risiken/Maßnahmen → Team & Kapazität → Phasen/Gantt → Berichtsstandsvergleich

## Change Log

- 2026-07-16: Story 6.7 implementiert
- 2026-07-16: Dokumentation präzisiert — Berichtsstandsvergleich ist eigenständiger Bereich, nicht unter Insights
