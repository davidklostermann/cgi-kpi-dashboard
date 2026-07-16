---
story_key: 7-2-risiken-ui
epic: 7
---

# Story 7.2: Risiken-UI (FR-6)

Status: done

## Review (2026-07-16)

**Ergebnis:** Freigegeben mit dokumentierter UI-Abweichung.

| Kriterium | Bewertung |
|---|---|
| Risiken auf Detailseite sichtbar | ✅ in `app-project-issues-actions-section` |
| Schweregrad als Wort + Farbe (nicht nur Farbe) | ✅ `severityLabel` + `aria-label` + linke Farblinie |
| Typ/Kategorie, Metriken, Owner, Fälligkeit, Maßnahme | ✅ |
| Loading / Error / Empty | ✅ Component-Tests |
| Separate Risiko-Liste (nicht kombiniert) | ⚠️ UX-Entscheidung: gemeinsame Section mit Problemen |
| `app-status-badge` für Schweregrad | ⚠️ eigene Badge-Klasse statt Shared-Component — fachlich ok |

**Tests:** `project-issues-actions-section.component.spec.ts` (4/4) — grün.

## Story

Als Projektleiter  
möchte ich Risiken sehen  
damit ich Maßnahmen einordnen kann.

## Acceptance Criteria

1. Risiko-Karten auf der Projekt-Detailseite mit Schweregrad-Badge (Wort + Farbe).
2. Typ/Kategorie, Metriken, Owner, Fälligkeit, Maßnahme/Vorbereitung sichtbar.

## Implementierungsstand

- **Abweichung zur ursprünglichen Story:** Keine separate Risiko-Liste.
- Umsetzung in `app-project-issues-actions-section` („Probleme, Risiken & Maßnahmen“).
- Schweregrad-Badges mit Text („Kritisch“, „Hoch“) und linker Farblinie; Farbe nicht alleinige Statusinfo.

## Tests (bestanden)

- `project-issues-actions-section.component.spec.ts` (Rendering, Loading, Error, Empty)

## Change Log

- 2026-07-16: Risiken-UI als Teil der kombinierten Issues-Actions-Section umgesetzt
- 2026-07-16: Review freigegeben
