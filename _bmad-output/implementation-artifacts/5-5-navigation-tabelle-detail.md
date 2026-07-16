---
story_key: 5-5-navigation-tabelle-detail
epic: 5
baseline_commit: 6368a0db1d44a618f5f15b0fd305cf930fb99e0e
---

# Story 5.5: Navigation Tabelle → Detail (FR-7)

Status: review

## Story

Als Nutzer  
möchte ich per Zeilenklick ins Projekt navigieren  
damit ich Details sehe.

## Acceptance Criteria

1. **Gegeben** Tabellenzeile, wenn Klick oder Enter, dann Navigation zu `/projects/{uuid}`.
2. **Gegeben** Navigation, dann bleibt Filterzustand in Session/Service erhalten.

## Tasks / Subtasks

- [x] Task 1: Zeilenklick → Router-Navigation (bereits in 5.3, AC: 1)
- [x] Task 2: Enter-Tastatur-Navigation + Fokus/ARIA (AC: 1)
- [x] Task 3: Filter-Persistenz via root `PortfolioFilterService` (AC: 2)
- [x] Task 4: Router- und Service-Tests (AC: 1–2)
- [x] Task 5: Story-Dokumentation

## Dev Notes

- **Kein Backend:** rein Frontend-Routing und Session-State.
- **Filter-Service:** `providedIn: 'root'` — überlebt Portfolio ↔ Detail-Navigation.
- **6.5 Follow-up:** explizite Zurück-Navigation mit Breadcrumb-Link (bereits vorhanden).

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- Tabellenzeilen als `role="link"` mit Enter-Handler und `aria-label`.
- Filter bleiben beim Navigieren zur Detailseite und zurück erhalten.

### File List

- frontend: project-table (Enter/ARIA), portfolio-filter.service, Tests
- _bmad-output/implementation-artifacts/5-5-navigation-tabelle-detail.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-16: Story 5.5 — Navigation Tabelle → Detail mit Filter-Persistenz
