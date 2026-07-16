---
story_key: 5-2-gantt-timeline-komponente
epic: 5
baseline_commit: 6368a0db1d44a618f5f15b0fd305cf930fb99e0e
---

# Story 5.2: Gantt-Timeline Komponente

Status: review

## Story

Als Nutzer  
möchte ich eine Gantt-artige Zeitleiste  
damit ich Verzüge erkenne.

## Acceptance Criteria

1. **Gegeben** Timeline-Daten, wenn Komponente rendert, dann eine Zeile pro Projekt mit Zeitachse, heute-Marker, Legende.
2. **Gegeben** Tastatur, dann ist horizontales Scrollen bedienbar.
3. **Gegeben** Screenreader, dann existiert sr-only-Zusammenfassung der Zeilen.

## Tasks / Subtasks

- [x] Task 1: `PortfolioTimeline` Model + API-Methode (AC: 1)
- [x] Task 2: `GanttTimelineComponent` in `shared/components` (AC: 1, 2, 3)
- [x] Task 3: `PortfolioGanttSectionComponent` mit Filter-Sync (AC: 1)
- [x] Task 4: Einbindung in Portfolio-Seite (AC: 1)
- [x] Task 5: Component- + API-Tests (AC: 1, 2, 3)
- [x] Task 6: Story-Dokumentation aktualisieren

## Dev Notes

- **UX-DR8:** HTML/CSS Gantt, keine Chart-Bibliothek.
- **Phasentypen Seed:** `ANALYSE`, `UMSETZUNG`, `ROLLOUT`.
- **AD-7:** Eigener Lade-/Fehlerzustand in Gantt-Sektion.
- **AD-10:** Page injiziert kein HttpClient.

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- `app-gantt-timeline`: Zeilen, Monatsachse, Phasensegmente, Verzugs-Schraffur, Heute-Marker, Legende.
- `app-portfolio-gantt-section`: lädt `/api/portfolio/timeline`, reagiert auf Filter.
- sr-only-Zusammenfassung + fokussierbare Scroll-Region für Tastatur.

### File List

- frontend/src/app/shared/models/portfolio-timeline.model.ts
- frontend/src/app/shared/components/gantt-timeline.component.ts
- frontend/src/app/shared/components/gantt-timeline.component.html
- frontend/src/app/shared/components/gantt-timeline.component.scss
- frontend/src/app/shared/components/gantt-timeline.component.spec.ts
- frontend/src/app/features/portfolio/portfolio-gantt-section.component.ts
- frontend/src/app/features/portfolio/portfolio-gantt-section.component.html
- frontend/src/app/features/portfolio/portfolio-gantt-section.component.scss
- frontend/src/app/features/portfolio/portfolio-gantt-section.component.spec.ts
- frontend/src/app/core/api/portfolio-api.service.ts
- frontend/src/app/core/api/portfolio-api.service.spec.ts
- frontend/src/app/features/portfolio/portfolio-page.component.ts
- frontend/src/app/features/portfolio/portfolio-page.component.html
- frontend/src/app/features/portfolio/portfolio-page.component.spec.ts
- _bmad-output/implementation-artifacts/5-2-gantt-timeline-komponente.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-16: Story 5.2 — Gantt-Timeline Komponente implementiert
