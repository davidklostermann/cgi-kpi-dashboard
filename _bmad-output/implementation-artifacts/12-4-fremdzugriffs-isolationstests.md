---
baseline_commit: 018e700ec68b4df002edf2ed063a284e2b07d626
---

# Story 12.4: Fremdzugriffs- und Isolationstests

Status: done

## Story

As a Product Owner,
I want eine konsolidierte Integrationstest-Suite für Epic-12-Isolation,
so that Rollen-Policies, Workspace-Grenzen, private Settings und AI-Cache-Isolation nachweisbar abgesichert sind (FR-23/25/26, AD-13/14/18).

## Scope

**In dieser Story:**

- Konsolidierte IT-Suite `SecurityIsolationIntegrationTest` (Epic-12-DoD)
- USER → 403 auf KI + `/api/admin/**`
- ADMIN → KI erlaubt im eigenen Workspace
- Fremdes Workspace → 404 auf Fakten- und KI-Endpunkte (ADMIN)
- USER auf fremdem Projekt-KI → 403 (Rollen-Check vor Scope)
- Private Settings A ≠ B; manipulierte `userId`/`workspaceId` im Body ignoriert
- AI-Cache: Hit im gleichen Workspace; fremder Workspace-Key liefert keinen Leak

**Out of Scope:**

- Neue Produktions-Features
- Epic-13-Admin-APIs (nur Policy-403 auf Prefix)
- Portfolio-KI-Cache (kein Cache vorhanden)

## Abhängigkeiten

- **Voraussetzung:** 12.1–12.3 done/review
- **Schließt:** Epic 12 DoD ab

## Acceptance Criteria

1. **USER** → **403** `FORBIDDEN` auf `/api/portfolio/ai/**`, `/api/projects/*/ai/**`, `/api/admin/**`.
2. **ADMIN** → **200** auf KI-Endpunkte für Projekte im eigenen Workspace.
3. **Fremdes Workspace:** Fakten-APIs (`/api/portfolio/**`, `/api/projects/{id}/**`) → **404** `NOT_FOUND`.
4. **ADMIN** auf fremdem Projekt-KI → **404**; **USER** auf fremdem Projekt-KI → **403**.
5. **Preferences:** User A ≠ User B; Body-`userId`/`workspaceId` (auch verschachtelt) werden nicht persistiert/zurückgegeben.
6. **AI-Cache:** Zweiter Aufruf im gleichen Workspace ohne `refresh` nutzt Cache; Eintrag unter fremdem Workspace-Key wird nicht an Default-Workspace ausgeliefert.
7. Bestehende Isolation-/Auth-ITs bleiben grün.

## Tasks / Subtasks

- [x] Story-Datei + Sprint-Status (ready-for-dev → in-progress)
- [x] `SecurityIsolationIntegrationTest` mit AC #1–#6 (AC: #1–#6)
- [x] Regression: Workspace-, Authz-, AI-ITs grün (AC: #7)

### Review Findings

- [x] [Review][Defer] Duplikat-Abdeckung zu `WorkspaceIsolationIntegrationTest` / `AiEndpointAuthorizationIntegrationTest` — bewusste Epic-12-DoD-Suite
- [x] [Review][Defer] POST questions / ADMIN-Fakten-404 / GET-Preferences-Härtung optional — in Story-12.2-ITs abgedeckt

### Senior Developer Review (AI)

**Outcome:** Approve  
**Date:** 2026-07-22

## Dev Notes

### Bestehende Tests (Referenz, nicht löschen)

- `WorkspaceIsolationIntegrationTest` — WS-Scope + Preferences (12.1)
- `AiEndpointAuthorizationIntegrationTest` — Rollen-Policies (12.2)
- `ProjectAiAnalysisServiceTest` — Cache-Key Unit (12.3)

### Neue Suite

- Paket: `com.cgi.kpi.dashboard.api.security`
- Isolierte H2-DB, Bootstrap leer, Seed wie bestehende ITs
- Zwei Workspaces: `WorkspaceIds.DEFAULT` + Foreign UUID
- `@Autowired ProjectAiAnalysisCache` für Cache-Leak-Szenario

### References

- [Source: `stories-security-multi-user.md` — Story 12.4]
- [Source: `12-1-workspace-scope-private-settings.md`]
- [Source: `12-2-endpoint-policies-ki-admin.md`]
- [Source: `12-3-ai-cache-isolation.md`]

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- `SecurityIsolationIntegrationTest` deckt Epic-12-DoD ab: Rollen (USER 403 / ADMIN 200 KI), Workspace-IDOR (404), USER→403 vs ADMIN→404 auf fremdem Projekt-KI, Preferences-Isolation, AI-Cache-Hit + kein Foreign-Workspace-Leak.
- Bestehende ITs (`WorkspaceIsolationIntegrationTest`, `AiEndpointAuthorizationIntegrationTest`) unverändert und grün.

### File List

- backend/src/test/java/com/cgi/kpi/dashboard/api/security/SecurityIsolationIntegrationTest.java (neu)

### Change Log

- 2026-07-22: Story 12.4 — konsolidierte Fremdzugriffs-/Isolationstest-Suite; Epic-12-DoD abgedeckt.