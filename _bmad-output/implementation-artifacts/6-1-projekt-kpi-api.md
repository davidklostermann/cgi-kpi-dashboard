---
story_key: 6-1-projekt-kpi-api
epic: 6
baseline_commit: HEAD
---

# Story 6.1: Projekt-KPI API (FR-5, FR-9)

Status: done

## Story

Als Projektleiter  
möchte ich Management-Kernkennzahlen  
damit ich Projektstatus beurteile.

## Acceptance Criteria

1. **Gegeben** Projekt-UUID, wenn `GET /api/projects/{id}/kpis`, dann: Status, Fortschritt, Phase, Zeitverbrauch, Terminabweichung, prognostiziertes Enddatum, Budget Plan/Ist/Verbrauch/Abweichung/Rest/Hochrechnung, Aufwand Plan/Ist/Abweichung/Rest, offene und kritische Risiken/Probleme — Plan/Ist/Prognose getrennt.
2. **Gegeben** Response, dann ausschließlich `kpi.*`-berechnet.

## Tasks / Subtasks

- [x] Task 1: `ProjectKpiDto` + `ProjectKpiCalculator` (AC: 1–2)
- [x] Task 2: `ProjectKpiReader` / `JpaProjectKpiReader` + Service (AC: 2)
- [x] Task 3: `GET /api/projects/{id}/kpis` im Controller (AC: 1)
- [x] Task 4: Unit- und Integrationstests (AC: 1–2)
- [x] Task 5: Story-Dokumentation

## Dev Notes

- Referenzdatum Phase/Zeitverbrauch: `2026-07-01` (wie Portfolio-Reader).
- Hochrechnung: `Ist / (Fortschritt/100)` bei Fortschritt &gt; 0.
- Terminabweichung: `ScheduleDeviationResolver` aus Plan- vs. Prognosedatum.
- 404 bei unbekannter UUID im einheitlichen Fehlerformat.

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- Deterministische Projekt-KPIs mit getrennten Schedule-/Budget-/Effort-/Risk-/Problem-Blöcken.
- Keine Frontend-Änderungen in dieser Story.

### File List

- backend: ProjectKpiDto, Calculator, Reader, Service, Controller, Tests
- _bmad-output/implementation-artifacts/6-1-projekt-kpi-api.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-16: Story 6.1 — Projekt-KPI API implementiert
