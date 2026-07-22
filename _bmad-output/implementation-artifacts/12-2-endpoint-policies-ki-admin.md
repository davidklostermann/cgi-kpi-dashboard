---
baseline_commit: 018e700ec68b4df002edf2ed063a284e2b07d626
---

# Story 12.2: Endpoint Policies — Fakten auth, KI + Admin nur ADMIN

Status: done

## Story

As a Workspace-Nutzer,
I want dass Fakten-APIs für alle authentifizierten Members zugänglich sind, KI- und Admin-Endpunkte aber nur für ADMIN,
so that KI-Missbrauch und Admin-Funktionen durch USER-Rollen nicht möglich sind (FR-23, FR-26, AD-13).

## Scope

**In dieser Story:**

- URL-Level: `/api/portfolio/ai/**`, `/api/projects/*/ai/**`, `/api/admin/**` → `ROLE_ADMIN`
- Service-Level: KI-Services prüfen Admin-Rolle (Defense-in-Depth)
- Fakten-APIs (`/api/portfolio/**`, `/api/projects/**` ohne `/ai/`) bleiben `authenticated` + Workspace-Scope (12.1)
- USER → **403** `FORBIDDEN` auf KI-Endpunkten; ADMIN → erlaubt (bei Workspace-Scope)
- Integrationstests: USER/KI 403, ADMIN/KI ok, USER/Fakten ok

**Out of Scope:**

- AI-Cache-Key mit workspaceId → **Story 12.3**
- Vollständige Fremdzugriffs-Suite → **Story 12.4**
- Benutzerverwaltung `/api/admin/**` Implementierung → **Epic 13** (URL-Policy hier vorbereiten)

## Abhängigkeiten

- **Voraussetzung:** Story 12.1 done (Workspace-Scope, CurrentUserService)
- **Blockiert:** 12.4 (Isolationstests bauen auf Policies auf), Epic 13 KI-Freigabe

## Acceptance Criteria

1. **SecurityConfig:** `GET /api/portfolio/ai/trend-analysis`, `GET /api/projects/{id}/ai/analysis`, `POST /api/projects/{id}/ai/questions` und `/api/admin/**` erfordern `ROLE_ADMIN`.
2. **Service-Check:** `PortfolioAiAnalysisService` und `ProjectAiAnalysisService` werfen 403 wenn Principal keine ADMIN-Rolle hat.
3. **Fakten-APIs** (`/api/portfolio/kpis`, `/api/projects/{id}`, etc.) bleiben für authentifizierte USER zugänglich (200 mit Workspace-Daten).
4. **USER** auf KI-Endpunkten → **403** JSON `{ "code": "FORBIDDEN", ... }`.
5. **ADMIN** auf KI-Endpunkten → funktioniert (200/mock) innerhalb eigenes Workspace.
6. Bestehende ADMIN-basierte ITs bleiben grün.

## Tasks / Subtasks

- [x] SecurityConfig URL-Policies für KI + `/api/admin/**` (AC: #1)
- [x] `CurrentUserService.requireAdmin()` + Service-Checks in KI-Services (AC: #2)
- [x] Integrationstest USER→403 KI, USER→200 Fakten, ADMIN→200 KI (AC: #3–#5)
- [x] Regression: bestehende Portfolio-/Projekt-/KI-ITs grün (AC: #6)

### Review Findings

- [x] [Review][Defer] Method Security (`@EnableMethodSecurity` / `@PreAuthorize`) — deferred; Story-12.2-DoD URL+Service akzeptiert (Entscheidung 2)
- [x] [Review][Decision] URL-Matcher enger als AD-13 — story-spezifische Matcher belassen (Entscheidung 2)
- [x] [Review][Patch] Service-Defense regressionsfest — Non-Admin-403-Tests in Portfolio/Project-AI-Service-Tests
- [x] [Review][Patch] Authz-IT ADMIN `POST …/ai/questions` ergänzt
- [x] [Review][Patch] Unauth→401-Test für KI-Endpunkte ergänzt

### Senior Developer Review (AI)

**Outcome:** Approve (nach Patches)  
**Date:** 2026-07-22

## Dev Notes

### Architektur-Invarianten

- **AD-13:** Default Deny; KI nur ADMIN; URL + Service-Level
- Spring `hasRole("ADMIN")` mappt auf `ROLE_ADMIN` aus `DashboardUserDetailsService.toAuthority()`
- Workspace/Projekt-Scope bleibt in Readern (12.1); Policy-Story ergänzt nur Rollen-Enforcement

### Touchpoints

- `SecurityConfig.java` — authorizeHttpRequests
- `CurrentUserService.java` — `requireAdmin()`
- `PortfolioAiAnalysisService.java`, `ProjectAiAnalysisService.java`
- Neu: `AiEndpointAuthorizationIntegrationTest.java`

### Testing

- Pattern wie `WorkspaceIsolationIntegrationTest`: isolierte H2-DB, Bootstrap leer, User manuell seeden
- `@WithDashboardUser(role = "ADMIN")` in bestehenden KI-ITs bleibt gültig

### References

- [Source: `stories-security-multi-user.md` — Story 12.2]
- [Source: `security-decisions.md` — AD-13]
- [Source: `11-3-spring-security-grundkonfiguration.md` — deferred URL policies]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

- URL-Level: `/api/portfolio/ai/**`, `/api/projects/*/ai/**`, `/api/admin/**` → `hasRole("ADMIN")` in `SecurityConfig`
- Service-Level: `CurrentUserService.requireAdmin()` in `PortfolioAiAnalysisService` und `ProjectAiAnalysisService`
- Neu: `AiEndpointAuthorizationIntegrationTest` — USER 403 auf KI/Admin, USER 200 auf Fakten/Preferences, ADMIN 200 auf KI

### File List

- `backend/src/main/java/com/cgi/kpi/dashboard/security/config/SecurityConfig.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/user/CurrentUserService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/ai/service/PortfolioAiAnalysisService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/ai/service/ProjectAiAnalysisService.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/authz/AiEndpointAuthorizationIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/security/user/CurrentUserServiceTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/ai/service/PortfolioAiAnalysisServiceTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/ai/service/ProjectAiAnalysisServiceTest.java`

### Change Log

- 2026-07-22: Story 12.2 — KI/Admin Endpoint-Policies (URL + Service), Authorization-ITs
- 2026-07-22: Code review — Approve; Patches für Authz-/Service-Tests angewendet.