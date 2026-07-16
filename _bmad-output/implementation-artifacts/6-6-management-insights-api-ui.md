---
story_key: 6-6-management-insights-api-ui
epic: 6
---

# Story 6.6: Management Insights API und UI (FR-20)

Status: done

## Acceptance Criteria

1. `GET /api/projects/{id}/insights` mit Aussage, Kennzahlen, Vergleichswert [OFFEN], Zeitraum, Begründung, Typ „deterministisch“.
2. Regeln in `kpi.insights` (`ProjectInsightEngine`).
3. Angular-Komponente `app-project-insights-section` im Codebase (Loading/Error/Empty).

## Produktstand (2026-07-16)

- **API, Regel-Engine und Komponente bleiben erhalten** und sind getestet.
- **FR-20 ist aktuell nicht sichtbar im MVP-UI:** `app-project-insights-section` wird auf der Projekt-Detailseite **nicht mehr gerendert** (fachliche/UX-Entscheidung).
- KI- und andere Module können weiterhin über die Insights-API lesen.

## Change Log

- 2026-07-16: Story 6.6 implementiert
- 2026-07-16: Management-Insights-UI aus fachlicher/UX-Entscheidung aus der Projekt-Detailseite entfernt; API, Regel-Engine und Komponente bleiben erhalten; FR-20 derzeit nicht sichtbar im MVP-UI
