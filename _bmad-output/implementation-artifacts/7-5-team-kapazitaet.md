---
story_key: 7-5-team-kapazitaet
epic: 7
---

# Story 7.5: Team- und Kapazitätssicht (MVP-Scope-Erweiterung)

Status: done

## Review (2026-07-16)

**Ergebnis:** Vollständig freigegeben.

| Kriterium | Bewertung |
|---|---|
| `GET /api/projects/{id}/capacity` | ✅ |
| Rollen mit Besetzungsgrad % und Fortschrittsbalken | ✅ |
| Summary: fehlende FTE, Verfügbarkeit, Überlast, externe Optionen, Terminwirkung | ✅ |
| Keine sensiblen Personaldetails | ✅ nur Rollen/FTE |
| Loading / Error / Empty + responsive | ✅ |
| Flyway V6/V7 Seed | ✅ Projekt `…0001`; andere Projekte Empty-State |

**Tests:** `ProjectControllerIntegrationTest`, `ProjectIssuesCapacityAssemblerTest`, `project-team-capacity-section.component.spec.ts`, `project-api.service.spec.ts` — grün.

## Story

Als Projektleiter  
möchte ich projektbezogene Rollen- und Kapazitätsübersicht  
damit ich Terminwirkungen aus Ressourcenlücken erkenne.

## Acceptance Criteria

1. `GET /api/projects/{id}/capacity` mit Rollen (Name, FTE, Besetzungsgrad %), Summary (fehlende FTE, nächste Verfügbarkeit, überlastete Rollen, externe Optionen, Terminwirkung).
2. `app-project-team-capacity-section` auf der Detailseite mit Fortschrittsbalken und Summary-Box.
3. Keine sensiblen Personaldetails.
4. Loading-, Error- und Empty-State; responsive Layout.
5. Reproduzierbare Mock-Daten (Flyway V6/V7).

## Tasks / Subtasks

- [x] Domain `ProjectRoleCapacity`, `ProjectCapacitySummary` + Migration V6
- [x] Seed V7 für Projekt `…0001`
- [x] DTO `ProjectCapacityDto`, Assembler, Reader, Controller-Endpunkt
- [x] Angular Section + API-Service + Models
- [x] Component-, API- und Assembler-Tests

## Tests (bestanden)

- `ProjectControllerIntegrationTest.getProjectCapacityReturnsRoleCoverageAndSummary`
- `ProjectIssuesCapacityAssemblerTest`
- `project-team-capacity-section.component.spec.ts`
- `project-api.service.spec.ts`

## Change Log

- 2026-07-16: Story 7.5 — Team- und Kapazitätssicht implementiert (Review offen)
- 2026-07-16: Review freigegeben
