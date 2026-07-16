---
story_key: 4-2-kpi-card-komponente
epic: 4
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 4.2: kpi-card Komponente

Status: done

## Story

Als Nutzer  
möchte ich einheitliche KPI-Karten  
damit Kennzahlen schnell erfassbar sind.

## Acceptance Criteria

1. **Gegeben** KPI-Daten, wenn Karte rendert, dann zeigt sie Label, Wert, Einheit tabular-nums.
2. **Gegeben** Karte, dann ist sie read-only und CGI-konform gestylt.

## Tasks / Subtasks

- [x] Task 1: `KpiCardComponent` in `shared/components` (AC: 1, 2)
- [x] Task 2: Inputs `label`, `value`, `unit`, optional `delta` (AC: 1)
- [x] Task 3: CGI-Tokens (`--cgi-surface`, `--cgi-border`, `--cgi-neutral-*`) (AC: 2)
- [x] Task 4: Component-Tests Inputs/Outputs (AC: 1, 2)
- [x] Task 5: Story-Dokumentation aktualisieren

## Dev Notes

- **UX-DR3:** Weiße Karte, Label klein/fett, Wert groß, optional Delta.
- Read-only: semantisches `<article>`, keine interaktiven Controls.
- Integration in Portfolio-Seite folgt in Story 4.3.

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- `KpiCardComponent` — standalone, Signal-Inputs.
- Styling an CGI-Token-System aus `styles.scss` angelehnt.
- Tests: Label/Wert/Einheit, tabular-nums, read-only, CGI-Oberfläche.

### File List

- frontend/src/app/shared/components/kpi-card.component.ts
- frontend/src/app/shared/components/kpi-card.component.html
- frontend/src/app/shared/components/kpi-card.component.scss
- frontend/src/app/shared/components/kpi-card.component.spec.ts
- frontend/src/app/shared/components/README.md
- _bmad-output/implementation-artifacts/4-2-kpi-card-komponente.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 4.2 — kpi-card Komponente implementiert
