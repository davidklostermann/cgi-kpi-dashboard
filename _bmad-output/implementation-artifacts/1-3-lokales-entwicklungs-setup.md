---
story_key: 1-3-lokales-entwicklungs-setup
epic: 1
baseline_commit: NO_VCS
---

# Story 1.3: Lokales Entwicklungs-Setup

Status: done

## Story

Als Entwickler  
möchte ich dokumentierte Startschritte  
damit Backend, DB und Frontend lokal starten.

## Acceptance Criteria

1. **Gegeben** README, **wenn** befolgt, **dann** sind PostgreSQL, Backend und Frontend startbar.
2. **Gegeben** Angular Dev-Server, **wenn** `/api/*` aufgerufen wird, **dann** proxied er zum Backend `:8080`.
3. **Gegeben** `environment.ts`, **dann** enthält er nur `apiBaseUrl`, keine Secrets.

## Tasks / Subtasks

- [x] Task 1: Root-README mit lokalem Start (PostgreSQL, Backend, Frontend) (AC: 1)
- [x] Task 2: Maven Wrapper für reproduzierbares Backend-Build (AC: 1, deferred 1.1)
- [x] Task 3: Angular Dev-Proxy `/api` und `/actuator` → `:8080` (AC: 2)
- [x] Task 4: `environment.ts` + Migration `api.config.ts` (AC: 3, deferred 1.2)
- [x] Task 5: `PortfolioApiService.getHealthProbe()` über Root-Pfad `/actuator/health` (AC: 2, deferred 1.2)
- [x] Task 6: Tests und Verifikation (AC: 1–3)
- [x] Task 7: Story-Dokumentation aktualisieren

### Review Findings

- [x] [Review][Defer] Kein automatisierter E2E-Proxy-Test — manueller Smoke-Test ausreichend laut Story-Spec
- [x] [Review][Dismiss] PostgreSQL noch ohne Backend-Anbindung — bewusst, Flyway folgt in späteren Stories

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved — alle ACs erfüllt  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)

### Prüfmatrix

| Prüffokus | Ergebnis | Evidenz |
|---|---|---|
| Environment (AD-8) | **Bestanden** | `environment.ts` nur `apiBaseUrl`; Unit-Test prüft Keys |
| Dev-Proxy `/api` | **Bestanden** | `proxy.conf.json` → `:8080`; `/api/` liefert Backend-404 |
| Actuator-Proxy | **Bestanden** | `/actuator/health` → `{"status":"UP"}` auf `:4200` |
| `api.config.ts` Migration | **Bestanden** | Liest `environment.apiBaseUrl`, nicht hardcoded |
| `getHealthProbe()` | **Bestanden** | `getAtRoot('/actuator/health')` ohne `/api`-Prefix |
| README / mvnw | **Bestanden** | Root-README dokumentiert PG, Backend, Frontend; `mvnw` vorhanden |
| Deferred 1.1 + 1.2 | **Adressiert** | mvnw, environment, actuator-Pfad |

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 README-Start | **Erfüllt** | `README.md` mit PostgreSQL, `mvnw`, `npm start` |
| AC2 Proxy `/api/*` | **Erfüllt** | `proxy.conf.json` + Backend-Response über `:4200` |
| AC3 `environment.ts` | **Erfüllt** | Nur `apiBaseUrl`, keine Secrets |

## Dev Notes

- **AD-8:** Keine Secrets in `environment.ts` — nur `apiBaseUrl`.
- **AD-9:** Lokaler Lauf ohne Docker; PostgreSQL lokal installiert.
- Backend verbindet sich in dieser Story noch **nicht** mit PostgreSQL (Flyway/JPA folgen in späteren Stories).
- Dev-Proxy: `/api` für REST-Fakten; `/actuator` separat (Actuator liegt nicht unter `/api`).

### References

- [Source: ARCHITECTURE-SPINE.md — AD-8, AD-9, Config]
- [Source: stories-mvp.md — Story 1.3]
- [Source: deferred-work.md — Findings aus 1.1 und 1.2]

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 3/3 grün
- `npx ng test --watch=false` — 8/8 grün
- Proxy-Smoke: `http://localhost:4201/actuator/health` → `{"status":"UP"}`
- Proxy-Smoke: `http://localhost:4201/api/` → 404 vom Backend (Proxy aktiv, keine Controller)

### Completion Notes List

- Root-README mit PostgreSQL-, Backend- und Frontend-Startschritten.
- Maven Wrapper (`mvnw`/`mvnw.cmd`) für reproduzierbares Backend-Build.
- `environment.ts` / `environment.prod.ts` mit ausschließlich `apiBaseUrl`.
- `api.config.ts` liest `environment.apiBaseUrl` statt hardcoded `/api`.
- Dev-Proxy `proxy.conf.json` für `/api` und `/actuator` → `:8080`.
- `PortfolioApiService.getHealthProbe()` nutzt `getAtRoot('/actuator/health')` ohne `/api`-Prefix.
- `backend/.env.example` für serverseitige Secrets (AD-8).
- Deferred Findings aus 1.1 (mvnw) und 1.2 (environment, actuator) adressiert.
- Code Review 2026-07-14: Approved, Proxy `:4200/actuator/health` → UP.

### File List

- README.md
- .gitignore
- backend/.env.example
- backend/mvnw
- backend/mvnw.cmd
- backend/.mvn/wrapper/maven-wrapper.properties
- frontend/proxy.conf.json
- frontend/angular.json
- frontend/src/environments/environment.ts
- frontend/src/environments/environment.prod.ts
- frontend/src/environments/environment.spec.ts
- frontend/src/app/core/api/api.config.ts
- frontend/src/app/core/api/api.config.spec.ts
- frontend/src/app/core/api/api-client.service.ts
- frontend/src/app/core/api/portfolio-api.service.ts
- frontend/src/app/core/api/portfolio-api.service.spec.ts

### Change Log

- 2026-07-14: Story 1.3 — Lokales Entwicklungs-Setup implementiert
- 2026-07-14: BMAD Code Review — Approved
