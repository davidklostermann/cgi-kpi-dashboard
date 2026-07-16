---
story_key: 5-3-portfolio-projekttabelle
epic: 5
baseline_commit: 6368a0db1d44a618f5f15b0fd305cf930fb99e0e
---

# Story 5.3: Portfolio-Projekttabelle (FR-2)

Status: review

## Story

Als Führungskraft  
möchte ich alle Projekte in einer Management-Tabelle  
damit ich Projekte vergleiche und Kritisches erkenne.

## Acceptance Criteria

1. **Gegeben** Portfolio, wenn Tabelle lädt, dann alle geforderten Spalten aus Backend.
2. **Gegeben** schmaler Viewport, dann horizontales Scrollen.
3. **Gegeben** Sortierung, dann nach Status, Fortschritt, Terminabweichung, Budgetabweichung, kritischen Issues, letzte Aktualisierung.
4. **Gegeben** Zeilenklick, dann Navigation zu `/projects/{uuid}`.

## Tasks / Subtasks

- [x] Task 1: `PortfolioTableDto` + Assembler in `kpi.*` (AC: 1)
- [x] Task 2: `GET /api/portfolio/projects` mit Filtern (AC: 1)
- [x] Task 3: `status-badge` + `project-table` Komponenten (AC: 1, 2, 3)
- [x] Task 4: `PortfolioTableSectionComponent` + Page-Einbindung (AC: 1, 4)
- [x] Task 5: Tests Backend + Frontend (AC: 1–4)
- [x] Task 6: Story-Dokumentation

## Dev Notes

- **MVP-Metriken:** Budget % = Ist/Plan; Abweichungen = (Ist−Plan)/Plan×100; kritische Issues = offene HIGH/CRITICAL Risiken + Probleme.
- **Filter-Sync:** gleiche Query-Parameter wie KPI/Gantt.
- **5.5 Follow-up:** Filter-Persistenz bei Navigation explizit in Story 5.5.

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- Management-Tabelle mit 15 Spalten, Sortierung, Status-Badge, Fortschrittsbalken.
- Zeilenklick navigiert zur Projekt-Detailseite.

### File List

- backend: PortfolioTable* DTOs, Assembler, Reader, Controller, Tests
- frontend: status-badge, project-table, portfolio-table-section, models, API, page integration, Tests
- _bmad-output/implementation-artifacts/5-3-portfolio-projekttabelle.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-16: Story 5.3 — Portfolio-Projekttabelle implementiert
