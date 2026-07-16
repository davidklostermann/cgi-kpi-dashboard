---
story_key: 7-1-risiken-api
epic: 7
---

# Story 7.1: Risiken API (FR-6)

Status: done

## Story

Als System  
möchte ich Risiken bereitstellen  
damit sie angezeigt werden.

## Acceptance Criteria

1. Risiken mit FR-6-Mindestfeldern (Titel, Beschreibung, Schweregrad, Status, Kategorie, Metriken, Owner, Fälligkeit, Gegenmaßnahme — soweit modelliert).
2. Anzahl offener/kritischer Risiken aggregierbar für KPIs und Portfolio.

## Implementierungsstand

- **Abweichung zur ursprünglichen Story:** Kein dedizierter `GET /api/projects/{id}/risks`.
- Stattdessen: `GET /api/projects/{id}/issues-actions` liefert offene Risiken als `itemType: RISK`.
- Domain-Entity `Risk` erweitert (V6-Migration); Seed in V7.
- KPI-Aggregation über `ProjectKpiCalculator` / `JpaProjectKpiReader`.

## Review (2026-07-16)

**Ergebnis:** Freigegeben mit dokumentierter API-Abweichung.

| Kriterium | Bewertung |
|---|---|
| Risiken als strukturierte Liste bereitstellbar | ✅ über `GET /issues-actions` (`itemType: RISK`) |
| FR-6-Felder | ✅ im DTO |
| Nur offene Risiken | ✅ Filter `status = OPEN` |
| KPI-Aggregation | ✅ `GET /kpis` |
| Dedizierter `/risks`-Endpunkt | ⚠️ kombinierte API |

**Tests:** `ProjectControllerIntegrationTest`, `ProjectIssuesCapacityAssemblerTest`, `ProjectKpiCalculatorTest` — grün.

## Change Log

- 2026-07-16: Risiken über kombinierte Issues-Actions-API bereitgestellt
- 2026-07-16: Review freigegeben — MVP-Ziel erfüllt über `issues-actions`
