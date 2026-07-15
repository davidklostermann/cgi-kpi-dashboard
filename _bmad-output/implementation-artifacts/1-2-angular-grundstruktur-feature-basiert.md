---
story_key: 1-2-angular-grundstruktur-feature-basiert
epic: 1
baseline_commit: NO_VCS
---

# Story 1.2: Angular-Grundstruktur feature-basiert

Status: done

## Story

Als Entwickler  
möchte ich die dokumentierte Angular-Ordnerstruktur  
damit Features isoliert entwickelt werden können.

## Acceptance Criteria

1. **Gegeben** `frontend/`, **wenn** `ng serve` / `npm start`, **dann** lädt die Startseite ohne Fehler.
2. **Gegeben** die Struktur, **dann** existieren `core/`, `shared/`, `features/portfolio`, `features/project`, `features/ai`.
3. **Gegeben** eine Präsentationskomponente, **dann** kein direkter `HttpClient`-Import.

## Tasks / Subtasks

- [x] Task 1: Ordnerstruktur core/shared/features (AC: 2)
- [x] Task 2: Lazy Routes Portfolio/Project (AC: 1)
- [x] Task 3: HttpClient nur in core/api (AC: 3)
- [x] Task 4: Tests und Build (AC: 1)
- [x] Task 5: Story-Dokumentation aktualisieren

### Review Findings

- [x] [Review][Defer] `api.config.ts` hardcoded `/api` statt `environment.apiBaseUrl` — deferred, Story 1.3
- [x] [Review][Defer] `PortfolioApiService.getHealthProbe()` Pfad `/actuator/health` unter `/api`-Prefix — Stub, Korrektur mit Proxy in Story 1.3
- [x] [Review][Defer] Kein Unit-Test für `AiPanelPlaceholderComponent` — optional, kein AC-Blocker

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved — alle ACs erfüllt  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)

### Prüfmatrix

| Prüffokus | Ergebnis | Evidenz |
|---|---|---|
| Lazy Loading | **Bestanden** | `loadChildren` in `app.routes.ts`; `loadComponent` in Feature-Routes; Dev-Server zeigt separate Lazy Chunks (~3 kB) |
| Feature-Abgrenzung | **Bestanden** | `features/portfolio`, `project`, `ai` getrennt; UI in Features, HTTP in `core/api` |
| API-Service-Platzierung | **Bestanden** | `ApiClient` + `*ApiService` in `core/api/` gemäß AD-10 |
| Kein HttpClient in UI | **Bestanden** | Grep: HttpClient nur in `api-client.service.ts` + `app.config.ts` |
| Tests | **Bestanden** | 5/5 Tests grün; Portfolio/Project ohne HttpClient-Provider |
| Architektur AD-10 | **Bestanden** | Standalone, Lazy Routes, Signals-ready, HttpClient-Grenze |

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 App startet | **Erfüllt** | `ng serve` läuft auf http://localhost:4200/ |
| AC2 Ordnerstruktur | **Erfüllt** | `core/`, `shared/`, `features/{portfolio,project,ai}` |
| AC3 Kein HttpClient in UI | **Erfüllt** | Präsentationskomponenten ohne HttpClient-Import |

## Dev Notes

- Standalone Components (Angular 22), Lazy `loadChildren` für Portfolio/Project.
- `HttpClient` nur in `core/api/*` via `ApiClient`-Basisklasse.
- Kein CGI EDS, kein Proxy (Story 1.3).

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `npx ng test --watch=false` — 5/5 grün
- `npx ng build --configuration=development` — erfolgreich, Lazy Chunks sichtbar

### Completion Notes List

- Feature-basierte Ordnerstruktur gemäß AD-10 angelegt.
- Platzhalter-Routen `/portfolio` und `/projects` mit Lazy Loading.
- API-Services (`PortfolioApiService`, `ProjectApiService`, `AiApiService`) in `core/api`.
- Präsentationskomponenten ohne HttpClient; Tests ohne HttpClient-Provider.
- Code Review 2026-07-14: Approved, Story auf done gesetzt.

### Change Log

- 2026-07-14: Story 1.2 — Angular feature-basierte Grundstruktur
- 2026-07-14: BMAD Code Review — Approved

### File List

- frontend/src/app/app.ts
- frontend/src/app/app.html
- frontend/src/app/app.scss
- frontend/src/app/app.config.ts
- frontend/src/app/app.routes.ts
- frontend/src/app/app.spec.ts
- frontend/src/app/core/api/api.config.ts
- frontend/src/app/core/api/api-client.service.ts
- frontend/src/app/core/api/portfolio-api.service.ts
- frontend/src/app/core/api/project-api.service.ts
- frontend/src/app/core/api/ai-api.service.ts
- frontend/src/app/core/interceptors/README.md
- frontend/src/app/core/error-handling/README.md
- frontend/src/app/core/layout/README.md
- frontend/src/app/core/navigation/README.md
- frontend/src/app/core/services/README.md
- frontend/src/app/shared/components/README.md
- frontend/src/app/shared/models/README.md
- frontend/src/app/shared/pipes/README.md
- frontend/src/app/shared/directives/README.md
- frontend/src/app/shared/utilities/README.md
- frontend/src/app/features/portfolio/portfolio.routes.ts
- frontend/src/app/features/portfolio/portfolio-page.component.ts
- frontend/src/app/features/portfolio/portfolio-page.component.html
- frontend/src/app/features/portfolio/portfolio-page.component.scss
- frontend/src/app/features/portfolio/portfolio-page.component.spec.ts
- frontend/src/app/features/project/project.routes.ts
- frontend/src/app/features/project/project-list-page.component.ts
- frontend/src/app/features/project/project-list-page.component.html
- frontend/src/app/features/project/project-list-page.component.scss
- frontend/src/app/features/project/project-list-page.component.spec.ts
- frontend/src/app/features/ai/ai-panel-placeholder.component.ts
