---
story_key: 6-2-projekt-stammdaten-header
epic: 6
baseline_commit: HEAD
---

# Story 6.2: Projekt-Stammdaten und Header (FR-5)

Status: review

## Story

Als Nutzer  
möchte ich Stammdaten und ausgeschriebenen Status  
damit ich Projektkontext habe.

## Acceptance Criteria

1. Detailseite zeigt Breadcrumb, Projektname, Projekt-ID, Kunde, Projektleitung, Start-/Plan-/Prognose-Ende, Phase, Status-Badge, letzte Datenaktualisierung.
2. `GET /api/projects/{id}/master-data` liefert die Stammdaten-Felder.

## Tasks / Subtasks

- [x] Task 1: `ProjectMasterDataDto` + Reader/Service (AC: 2)
- [x] Task 2: Endpoint `GET /api/projects/{id}/master-data` (AC: 2)
- [x] Task 3: Detail-Header UI mit Status-Badge (AC: 1)
- [x] Task 4: API- und Component-Tests (AC: 1–2)

## Dev Agent Record

### Completion Notes List

- Stammdaten über `ProjectKpiReader.readProjectMasterData`, Phase via `ProjectKpiCalculator.resolveCurrentPhaseName`.
- Frontend lädt Master-Data isoliert mit Load/Error-State.

### File List

- backend: ProjectMasterDataDto, ProjectKpiReader/Service/JpaReader, ProjectController, IntegrationTest
- frontend: project-api, project-detail-page, project-detail.model
- _bmad-output/implementation-artifacts/6-2-projekt-stammdaten-header.md
- sprint-status.yaml

### Change Log

- 2026-07-16: Story 6.2 implementiert
