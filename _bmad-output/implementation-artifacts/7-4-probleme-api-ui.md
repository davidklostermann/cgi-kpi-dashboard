---
story_key: 7-4-probleme-api-ui
epic: 7
---

# Story 7.4: Probleme API und UI (FR-6)

Status: done

## Review (2026-07-16)

**Ergebnis:** Freigegeben mit dokumentierter API-/UI-Abweichung.

| Kriterium | Bewertung |
|---|---|
| Probleme als strukturierte Liste | ✅ über `GET /issues-actions` (`itemType: PROBLEM`) |
| FR-6-Felder inkl. Gegenmaßnahme | ✅ `actionText` / „Laufende Maßnahme“ |
| Anzeige only (kein Workflow) | ✅ |
| Getrennt von Risiken erkennbar | ✅ `itemType`-Label auf Card |
| Dedizierter `/problems`-Endpunkt / eigene Section | ⚠️ kombinierte API und UI |

**Tests:** `ProjectControllerIntegrationTest`, `project-issues-actions-section.component.spec.ts` — grün.

## Story

Als Projektleiter  
möchte ich eingetretene Probleme getrennt von Risiken sehen  
damit ich den Ist-Zustand verstehe.

## Acceptance Criteria

1. Probleme mit FR-6-Mindestfeldern (Anzeige only, kein Workflow).
2. Problem-Bereich auf der Detailseite getrennt von reinen Risiko-Listen erkennbar.

## Implementierungsstand

- **Abweichung zur ursprünglichen Story:** Kein dedizierter `GET /api/projects/{id}/problems`.
- Stattdessen: `GET /api/projects/{id}/issues-actions` mit `itemType: PROBLEM`.
- UI: dieselbe kombinierte Section wie 7.2; Probleme und Risiken als Cards mit Typ-Label unterscheidbar.

## Tests (bestanden)

- `ProjectControllerIntegrationTest.getProjectIssuesActionsReturnsOpenProblemsAndRisks`
- `project-issues-actions-section.component.spec.ts`

## Change Log

- 2026-07-16: Probleme über kombinierte Issues-Actions-API/UI bereitgestellt
- 2026-07-16: Review freigegeben
