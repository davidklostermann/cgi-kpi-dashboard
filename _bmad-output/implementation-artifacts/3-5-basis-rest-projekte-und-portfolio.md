---
story_key: 3-5-basis-rest-projekte-und-portfolio
epic: 3
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 3.5: Basis-REST Projekte und Portfolio

Status: done

## Review Findings (2026-07-15)

### Decision-needed
_(deferred — siehe Defer unten)_

### Patch
- [x] [Review][Patch] Ungültige UUID im Pfad — codebasiert abgeleitet: ohne dedizierten Handler würde `MethodArgumentTypeMismatchException` in `GlobalExceptionHandler.handleUnhandled` landen (500). Behoben: Handler liefert 400 `{code, message}` [`GlobalExceptionHandler.java`]
- [x] [Review][Patch] Integrationstest für malformed UUID — `getProjectByMalformedUuidReturnsStructuredBadRequest` [`ProjectControllerIntegrationTest.java`]
- [x] [Review][Patch] Listen-Test prüft Sortierung und Zusatzfelder (`progressPercent`, `scheduleDeviationDays`, `plannedEndDate`) am ersten Seed-Eintrag [`ProjectControllerIntegrationTest.java`]

### Defer
- [x] [Review][Defer] Application-Schicht liefert API-DTOs direkt — MVP-Muster beibehalten; Refactor optional in späterer Story [`ProjectQueryService.java`] — deferred, Entscheidung vorerst offen
- [x] [Review][Defer] `findAll()` + In-Memory-Sortierung ohne Pagination — akzeptabel für Mock-Portfolio (~20 Projekte) [`DefaultProjectQueryService.java:30-33`] — deferred, MVP-Scope
- [x] [Review][Defer] AC3 nur auf Projekt-DTOs getestet, nicht auf KPI-REST-Endpunkte — KPI-API folgt in Epic 4 [`ProjectControllerIntegrationTest.java:37,52`] — deferred, keine KPI-Endpunkte in dieser Story
- [x] [Review][Defer] `status` als unvalidierter String — bereits in Story 3.1 deferred [`ProjectListItemDto.java:12`] — deferred, pre-existing
- [x] [Review][Defer] Kein `progress_percent`-Range-Check im Mapper — Seed-Daten kontrolliert [`ProjectMapper.java:21`] — deferred, Datenqualität später
- [x] [Review][Defer] `ProjectMapper` ohne dedizierte Unit-Tests — Happy-Path über Integrationstest abgedeckt [`ProjectMapper.java`] — deferred, ausreichende Abdeckung für Basis-Story

## Story

Als Frontend  
möchte ich Basis-Endpunkte  
damit Listen geladen werden können.

## Acceptance Criteria

1. **Gegeben** Mock-Daten, wenn `GET /api/projects`, dann Liste mit erweiterten Tabellenfeldern (FR-2) — mindestens UUID, Name, Kunde, Status.
2. **Gegeben** UUID, wenn `GET /api/projects/{id}`, dann Projektdetails inkl. Stammdaten-Basis.
3. **Gegeben** KPI-Responses, dann enthalten sie kein `aiGenerated`.

## Tasks / Subtasks

- [x] Task 1: API-DTOs `ProjectListItemDto`, `ProjectDetailDto` (AC: 1, 2)
- [x] Task 2: `ProjectQueryService` + Mapper in `application` (AC: 1, 2)
- [x] Task 3: `ProjectController` mit GET-Listen und GET-Detail (AC: 1, 2)
- [x] Task 4: MockMvc-Integrationstests (AC: 1, 2, 3)
- [x] Task 5: Story-Dokumentation aktualisieren

## Dev Notes

- **AD-5:** REST unter `/api/projects`; UUID als String; Fehler via `ApiException` → `{ code, message }`.
- **AD-3:** Controller liefert DTOs, keine Domain-Entitäten. Keine KPI-Berechnung in dieser Story.
- Erweiterte FR-2-Felder (Projektleitung, Budget, Risiken) folgen in Story 3.6/Epic 4 — nur vorhandene Entity-Felder mappen.
- Seed-Referenz-UUID: `a0000000-0000-4000-8000-000000000001` (Nexus Analytics Pilot).

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 24/24 grün (2026-07-15, nach Code-Review-Patches)
- Branch: `story/3.5-basis-rest`

### Completion Notes List

- API-DTOs: `ProjectListItemDto` (Liste/Tabellenbasis), `ProjectDetailDto` (Stammdaten).
- Application: `ProjectQueryService` + `DefaultProjectQueryService` — read-only über `ProjectRepository`.
- REST: `ProjectController` — `GET /api/projects`, `GET /api/projects/{id}`.
- Mapper: `ProjectMapper` — Domain → DTO, keine Entity-Exposition.
- Tests: `ProjectControllerIntegrationTest` — Seed-Daten, Detail, 404, malformed UUID, Listen-Sortierung/Zusatzfelder, kein `aiGenerated`.
- Code-Review 2026-07-15: UUID-400-Handler, Listen-Test-Ergänzungen; Schichten-Design deferred.

### File List

- backend/src/main/java/com/cgi/kpi/dashboard/api/projects/ProjectController.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/projects/ProjectMapper.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/projects/dto/ProjectListItemDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/projects/dto/ProjectDetailDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/application/ProjectQueryService.java
- backend/src/main/java/com/cgi/kpi/dashboard/application/DefaultProjectQueryService.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandler.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/projects/ProjectControllerIntegrationTest.java
- _bmad-output/implementation-artifacts/3-5-basis-rest-projekte-und-portfolio.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 3.5 — Basis-REST Projekte implementiert
- 2026-07-15: Code-Review abgeschlossen — Patches angewendet, Status done
