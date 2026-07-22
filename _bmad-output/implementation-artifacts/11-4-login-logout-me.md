# Story 11.4: Login, Logout, `/api/auth/me` + Passwortwechsel-Zwang

Status: done

## Story

As a Nutzer,
I want mich anmelden/abmelden und meine Identität sehen,
so that eine Session entsteht und der Bootstrap-Admin sein Initialpasswort ändern muss (FR-22, AD-12).

## Scope

**In dieser Story (Backend only):**

- `POST /api/auth/login` — Session erstellen (öffentlich + CSRF)
- `POST /api/auth/logout` — Session invalidieren (auth + CSRF)
- `GET /api/auth/me` — Principal aus SecurityContext
- `POST /api/auth/change-password` — Pflicht-Flow für `must_change_password`
- `MustChangePasswordFilter` — blockiert `/api/**` außer `me`, `logout`, `change-password`
- Deaktivierte User: Login abgelehnt
- Tests: Login/Logout/Me, disabled, must-change, parallele Sessions

## Out of Scope

- Angular Login-UI → 11.5
- Vollständige Auth-Matrix → 11.6
- Rate-Limit/Lockout → 14.2
- Admin-Passwort-Reset → Epic 13

## Acceptance Criteria

1. `POST /api/auth/login` mit gültigen Credentials → 200 + Session + User-DTO
2. Ungültige Credentials → 401 `BAD_CREDENTIALS`
3. Deaktivierter User → 403 `ACCOUNT_DISABLED`
4. `GET /api/auth/me` ohne Auth → 401
5. `POST /api/auth/logout` invalidiert Session (auth + CSRF)
6. `must_change_password=true`: `/api/**` blockiert mit `PASSWORD_CHANGE_REQUIRED`, außer auth-Endpoints
7. `POST /api/auth/change-password` setzt neues Passwort, `must_change_password=false`
8. Parallele Sessions zweier User getrennt (Integrationstest)
9. `mvn test` grün

## Tasks / Subtasks

- [x] Auth API (login, logout, me, change-password)
- [x] MustChangePasswordFilter
- [x] AuthenticationManager Bean
- [x] Integrationstests
- [x] `mvn test` grün

### Review Findings

- [x] [Review][Patch] Must-Change-Filter blockiert nur `/api/**` — `/actuator/**` bleibt erreichbar [`MustChangePasswordFilter.java:57`]
- [x] [Review][Patch] Negativer Test für falsches Passwort bei `change-password` fehlt [`AuthControllerIntegrationTest.java`]
- [x] [Review][Patch] `changePassword` persistiert refreshed Principal nicht in Session [`AuthService.java:105`]
- [x] [Review][Defer] `lockedUntil` / Brute-Force-Lockout nicht implementiert [`DashboardUserDetailsService.java:40`] — deferred, Story 14.2
- [x] [Review][Defer] Passwortwechsel invalidiert andere Sessions nicht [`AuthService.java:88`] — deferred, Story 11.6
- [x] [Review][Defer] Stale `mustChangePassword` in Session nach Admin-DB-Update [`MustChangePasswordFilter.java:44`] — deferred, Epic 13
- [x] [Review][Defer] CSRF-Negativtests für Auth-Endpoints [`AuthControllerIntegrationTest.java`] — deferred, Story 11.6

### Dev Agent Record

- `AuthController`: login, logout, me, change-password
- `AuthService`: Session-Persistenz via `SecurityContextRepository`, disabled-User-Handling
- `MustChangePasswordFilter`: blockiert alle Pfade außer Auth-Whitelist bei `mustChangePassword`
- 167 Tests grün (inkl. Review-Patches)

**Neu:** `api/auth/*`, `MustChangePasswordFilter`, `AuthControllerIntegrationTest`  
**Geändert:** `SecurityConfig`, `GlobalExceptionHandler`, `pom.xml` (validation)

- 2026-07-21: Review-Patches — globaler Must-Change-Filter, Session nach Passwortwechsel, negative Tests

## Dev Notes