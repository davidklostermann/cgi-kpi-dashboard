---
story_key: 5-4-trend-statusvisualisierung
epic: 5
baseline_commit: 6368a0db1d44a618f5f15b0fd305cf930fb99e0e
---

# Story 5.4: Trend- und Statusvisualisierung (FR-3)

Status: review

## Story

Als Führungskraft  
möchte ich Verlauf und Statusverteilung sehen  
damit ich Trends erkenne.

## Acceptance Criteria

1. **Gegeben** Portfolio, wenn Visualisierungen laden, dann mindestens Fortschrittstrend und Budgetentwicklung mit beschrifteten Achsen.
2. **Gegeben** Statusverteilung, dann als Zahlenzeile oder Balken mit Labels, kein Donut.
3. **Gegeben** Segment-Umschalter 3M/6M/12M, dann filtert nur die Darstellung.

## Tasks / Subtasks

- [x] Task 1: `PortfolioTrendDto` + Assembler aus Snapshots (AC: 1)
- [x] Task 2: `GET /api/portfolio/trends` mit Filtern (AC: 1)
- [x] Task 3: `trend-chart` Komponente (AC: 1–3, UX-DR14)
- [x] Task 4: `PortfolioTrendsSectionComponent` + Page-Einbindung (AC: 1)
- [x] Task 5: Tests Backend + Frontend inkl. sr-only Summary (AC: 1–3)
- [x] Task 6: Story-Dokumentation

## Dev Notes

- **Seed:** Snapshots nur für 2026-06 und 2026-07 — 3M/6M/12M zeigen im MVP dieselben Punkte.
- **Statusverteilung:** aktueller Stand der gefilterten Projekte, nicht historisch pro Monat.
- **Filter-Sync:** gleiche Query-Parameter wie KPI/Gantt/Tabelle.

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- Backend aggregiert monatliche Ø-Fortschritt- und Ist-Budget-Trends aus `project_report_snapshots`.
- Frontend: SVG-Liniencharts, Status-Balken mit Labels, 3M/6M/12M nur Darstellungsfilter, sr-only-Zusammenfassung.

### File List

- backend: PortfolioTrend* DTOs, Assembler, Reader, Controller, Service, Tests
- frontend: trend-chart, portfolio-trends-section, models, API, page integration, Tests
- _bmad-output/implementation-artifacts/5-4-trend-statusvisualisierung.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-16: Story 5.4 — Trend- und Statusvisualisierung implementiert
