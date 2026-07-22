# Story 11.3: Spring-Security-Grundkonfiguration

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

As a Entwickler,
I want Default Deny und Session-Security für alle API-Endpunkte,
so that das Backend nicht mehr ohne Authentifizierung erreichbar ist und Story 11.4 Login auf einer sicheren Filter-Chain aufbauen kann (AD-12, FR-22, NFR-13).

**Nutzen:** Schließt die offene MVP-API (AD-6 SUPERSEDED); Session-Cookie-Modell, CSRF-Vorbereitung für SPA, JSON-401/403 — ohne JWT und ohne Login-UI.

## Scope

**In dieser Story (Backend only):**

- `spring-boot-starter-security` (ersetzt/ergänzt isoliertes `spring-security-crypto`; `PasswordEncoder`-Bean weiterverwenden)
- `SecurityFilterChain`: **Default Deny** für `/api/**`
- Öffentlich (permitAll): `POST /api/auth/login` (Stub-Pfad für 11.4 — noch kein funktionierender Login), `GET /actuator/health` (Smoke/Proxy)
- Unauthenticated Zugriff auf geschützte `/api/**` → **401** mit `{ "code", "message" }` (bestehendes `ApiErrorResponse`-Format)
- Authenticated ohne Berechtigung → **403** JSON (Vorbereitung; Rollen-Enforcement auf URL-Ebene für KI/Admin erst **12.2**)
- **Session-Auth** (kein JWT, kein Bearer-Token-Pfad)
- Session-Fixation-Schutz: `sessionFixation().changeSessionId()` (Spring Security Default bestätigen/explizit setzen)
- **CSRF aktiv** für state-changing Requests (`POST`/`PUT`/`PATCH`/`DELETE`); Cookie-basiertes CSRF-Token für SPA (Angular 11.5): `CookieCsrfTokenRepository` + Request-Handler für Header `X-XSRF-TOKEN`
- Session-Cookie: **HttpOnly**; **Secure** im Prod-Profil (`application-prod.yml`)
- `DashboardUserDetailsService` lädt `AppUser` + `WorkspaceMembership` (Default-Workspace v1)
- Custom Principal `DashboardUserDetails` mit `userId`, `workspaceId`, `authorities` (`ROLE_USER` / `ROLE_ADMIN`)
- `WorkspaceMembershipRepository.findByUserId` (11.1-Defer)
- Deaktivierte User (`active=false`): `loadUserByUsername` → `DisabledException` (Login folgt 11.4; Service muss vorbereitet sein)
- **Bestehende API-Integrationstests** anpassen: `@WithMockUser(roles = "ADMIN")` oder äquivalent — sonst 401-Regression
- Neue Security-Integrationstests: unauth → 401; CSRF auf Write ohne Token → 403
- Dokumentation: Single-Instance-Session (JVM-lokal), kein Spring Session JDBC/Redis in v1

## Out of Scope

- Login/Logout/`GET /api/auth/me` Implementierung → **11.4**
- Passwortwechsel, Lockout, Rate-Limit → **11.4 / 14.2**
- Angular Auth, Guards, Interceptor → **11.5**
- Vollständige Auth-Test-Matrix (parallele Sessions, Bootstrap+Login E2E) → **11.6**
- URL-Policy `/**/ai/**` nur ADMIN, `/api/admin/**` → **12.2** (AD-13)
- Workspace-scoped Queries / IDOR-Tests → **Epic 12**
- OIDC/SSO-Implementierung (nur Extension-Point dokumentieren)
- Spring Session JDBC/Redis, Horizontal Scaling
- Frontend-Änderungen
- Flyway-Änderungen
- Story 10.1 (pausiert)

## Abhängigkeiten

- **Voraussetzung:** **11.1 done** (Schema), **11.2 done** (Bootstrap-Admin + BCrypt `PasswordEncoder`)
- **Blockiert:** 11.4 (Login nutzt Filter Chain + UserDetailsService), 11.5, 11.6, Epic 12 AuthZ
- **Parallel:** Epic 10 pausiert — nicht anfassen

## Acceptance Criteria

1. **Dependency:** `spring-boot-starter-security` in `pom.xml`; `spring-security-crypto` kann entfallen (Starter transitiv) oder bleiben — **kein** doppelter `PasswordEncoder`-Bean-Konflikt.
2. **Default Deny:** Alle `/api/**`-Endpoints (bestehende Portfolio/Project/AI-Controller) erfordern Authentication. Ohne Session/Principal → **401** JSON `{ code, message }`.
3. **Öffentliche Pfade:** Mindestens `POST /api/auth/login` und `GET /actuator/health` sind ohne Auth erreichbar (Login-Endpoint darf in 11.3 leer/501/405 sein — **Pfad muss permitAll sein**, Implementierung in 11.4).
4. **Kein Security-Bypass im Prod-Code:** Kein `permitAll()` auf `/api/portfolio/**`, `/api/projects/**`, etc.
5. **Session:** Servlet-Session-basiert; **kein** JWT-Filter, kein OAuth2-Resource-Server, kein Token in Response-Header für Client-Persistenz.
6. **Session-Fixation:** `SessionManagementConfigurer` mit `changeSessionId()` (oder äquivalente Spring-Security-6-Konfiguration) aktiv.
7. **CSRF:** Aktiv global; `CookieCsrfTokenRepository` für SPA; Writes ohne gültiges CSRF-Token → **403** (Integrationstest mit MockMvc).
8. **Cookies:** Session-Cookie HttpOnly; Profil `prod`: `server.servlet.session.cookie.secure=true` (neue `application-prod.yml` oder dokumentierter Abschnitt).
9. **UserDetails:** `DashboardUserDetailsService` lädt per `username` den `AppUser`, prüft `active`, lädt `WorkspaceMembership` für `WorkspaceIds.DEFAULT` (v1 ein Workspace); Authorities aus `WorkspaceRole`.
10. **Principal-Felder:** Authenticated Principal enthält `userId` (UUID), `workspaceId` (UUID), `username`, `GrantedAuthority`-Liste — aus DB, nicht aus Request.
11. **Deaktiviert:** User mit `active=false` → `DisabledException` aus UserDetailsService (Unit- oder Integrationstest).
12. **Regression:** Alle bestehenden Backend-Tests grün (`mvn test`) — API-ITs mit Test-Security-Kontext (`@WithMockUser` o.ä.).
13. **Dokumentation:** README oder Dev Notes: Single-Backend-Instance-Sessions; Multi-Instance → Spring Session JDBC/Redis (OFFEN).

## Tasks / Subtasks

- [x] Maven + Beans (AC: #1)
  - [x] `spring-boot-starter-security` hinzufügen
  - [x] `PasswordEncoder`-Bean konsolidieren (bestehende `BootstrapConfig` oder nach `SecurityConfig` verschieben — **ein** Bean)
- [x] UserDetails (AC: #9–#11)
  - [x] `WorkspaceMembershipRepository.findByUserId(UUID userId)`
  - [x] `DashboardUserDetails` implements `UserDetails` + `userId`/`workspaceId`
  - [x] `DashboardUserDetailsService` implements `UserDetailsService`
- [x] SecurityFilterChain (AC: #2–#8)
  - [x] `SecurityConfig` mit `SecurityFilterChain` Bean
  - [x] `authorizeHttpRequests`: permitAll login + health; `/api/**` authenticated
  - [x] `JsonAuthenticationEntryPoint` → 401 `ApiErrorResponse`
  - [x] `JsonAccessDeniedHandler` → 403 `ApiErrorResponse`
  - [x] Session + CSRF Konfiguration
  - [x] `application-prod.yml`: Secure Session-Cookie
- [x] Tests (AC: #12)
  - [x] `SecurityFilterChainIntegrationTest`: unauth 401, CSRF write blocked
  - [x] `DashboardUserDetailsServiceTest` / IT: active/disabled user
  - [x] Alle `@AutoConfigureMockMvc`-ITs: `@WithMockUser(roles = "ADMIN")` (7 Dateien, siehe unten)
- [x] Docs (AC: #13)
- [x] `mvn test` grün

### Review Findings

- [x] [Review][Patch] Workspace-Fallback entfernen (AC9) [`DashboardUserDetailsService.java:46`]
- [x] [Review][Patch] `/actuator/info` absichern [`SecurityConfig.java:44`]
- [x] [Review][Defer] `lockedUntil` / Account-Lockout nicht in UserDetails [`DashboardUserDetails.java:72`] — deferred, Story 11.4
- [x] [Review][Defer] `mustChangePassword` nicht durchgesetzt [`DashboardUserDetails.java:47`] — deferred, Story 11.4
- [x] [Review][Defer] Session bleibt nach Deaktivierung gültig [`SecurityConfig.java:38`] — deferred, Story 11.6
- [x] [Review][Defer] Rollen-Enforcement auf URL-Ebene fehlt [`SecurityConfig.java:46`] — deferred, Story 12.2 (explizit Out of Scope)

## Dev Notes

### Architektur-Compliance

| ID | Vorgabe für 11.3 |
|---|---|
| **AD-12** | Session-Auth, CSRF, Fixation, HttpOnly/Secure, kein JWT |
| **AD-13** | Default Deny (URL-Ebene); feingranulare KI/Admin-Policies → 12.2 |
| **FR-22** | Auth-Infrastruktur (Login folgt 11.4) |
| **NFR-13** | Geschützte APIs |
| **NFR-10** | Fehlerformat `{ code, message }` bei 401/403 |

Quellen:
- [Source: `stories/stories-security-multi-user.md` — Story 11.3]
- [Source: `security-decisions.md` — AD-12]
- [Source: `security-addendum.md` — Auth Architecture v1]
- [Source: `ARCHITECTURE-SPINE.md` — Security Invariants]

### Bestehender Code (MUST lesen vor Implementierung)

| Datei | Ist-Zustand | Was 11.3 ändert |
|---|---|---|
| `BootstrapConfig.java` | `PasswordEncoder` @Bean, `BootstrapProperties` | Ggf. Security-Package erweitern; Bean-Duplikat vermeiden |
| `BootstrapAdminService.java` | Nutzt `PasswordEncoder` | Unverändert |
| `AppUser.java` / `WorkspaceMembership.java` | Domain | UserDetails-Mapping |
| `WorkspaceMembershipRepository.java` | `findByWorkspaceIdAndUserId` | + `findByUserId` |
| `GlobalExceptionHandler.java` | `{ code, message }` | Security EntryPoint/Handler **parallel** (Filter vor Controller) |
| `ApiErrorResponse.java` | Record | Wiederverwenden für 401/403 |
| Alle `*Controller.java` unter `/api/**` | Offen | Werden geschützt |
| `PortfolioControllerIntegrationTest` etc. | Erwarten 200 ohne Auth | `@WithMockUser` nötig |
| `pom.xml` | Nur `spring-security-crypto` | + `starter-security` |
| `application-test.yml` | Bootstrap disabled | Security aktiv in Tests (realistisch) |

### Empfohlene Package-Struktur (neu/erweitert)

```
backend/src/main/java/com/cgi/kpi/dashboard/security/
  config/
    SecurityConfig.java              # SecurityFilterChain, CSRF, Session
    BootstrapConfig.java             # bleibt; PasswordEncoder ggf. hier oder SecurityConfig
  user/
    DashboardUserDetails.java
    DashboardUserDetailsService.java
  web/
    JsonAuthenticationEntryPoint.java
    JsonAccessDeniedHandler.java
  bootstrap/                         # unverändert aus 11.2
```

**Noch nicht:** `AuthController` (11.4), `@EnableMethodSecurity` für KI (12.2).

### SecurityFilterChain-Vertrag (verbindlich)

```java
// Pseudocode — Spring Security 6 Lambda DSL
http
  .csrf(csrf -> csrf
      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
  .sessionManagement(session -> session
      .sessionFixation(fixation -> fixation.changeSessionId()))
  .authorizeHttpRequests(auth -> auth
      .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
      .requestMatchers("/actuator/health").permitAll()
      .requestMatchers("/api/**").authenticated()
      .anyRequest().permitAll())  // statische Assets / Nicht-API — MVP Backend nur API
  .exceptionHandling(ex -> ex
      .authenticationEntryPoint(jsonAuthenticationEntryPoint)
      .accessDeniedHandler(jsonAccessDeniedHandler));
```

**401-Body-Beispiel:** `{ "code": "UNAUTHORIZED", "message": "Authentication required" }`  
**403-Body-Beispiel:** `{ "code": "FORBIDDEN", "message": "Access denied" }`  
Codes an bestehende Konvention anlehnen (`GlobalExceptionHandler` nutzt `NOT_FOUND`, `BAD_REQUEST`).

### UserDetails-Mapping (v1)

| DB | Principal |
|---|---|
| `AppUser.id` | `userId` |
| `AppUser.username` | `UserDetails.getUsername()` |
| `AppUser.passwordHash` | `getPassword()` |
| `AppUser.active == false` | `DisabledException` |
| `WorkspaceMembership.workspaceId` (Default-WS) | `workspaceId` |
| `WorkspaceMembership.role` | `ROLE_ADMIN` / `ROLE_USER` |

**v1-Annahme:** Ein User hat genau eine Membership im Default-Workspace (Bootstrap + spätere Admin-Anlage). Fehlt Membership → `UsernameNotFoundException` oder `DisabledException` — **kein** Principal ohne Rolle.

`findByUserId`: Spring Data `Optional<WorkspaceMembership> findByUserId(UUID userId);` — UNIQUE (workspace,user) garantiert max. eine Zeile pro User im Default-WS.

### CSRF + SPA (Vorbereitung 11.5)

- `CookieCsrfTokenRepository.withHttpOnlyFalse()` — Angular liest Cookie `XSRF-TOKEN`, sendet Header `X-XSRF-TOKEN`
- GET-Requests: kein CSRF-Zwang
- `POST /api/auth/login`: in 11.4 permitAll **und** CSRF-Token vom vorherigen GET holen — Login-IT in 11.4/11.6
- **11.3:** CSRF-IT auf bestehendem geschützten POST (z. B. `POST /api/projects/{id}/ai/questions`) mit `@WithMockUser` + ohne CSRF → 403

### Session-Cookie / Prod-Profil

Neu: `backend/src/main/resources/application-prod.yml`:

```yaml
server:
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: lax
```

Dev/Test: Secure=false (HTTP localhost). SameSite Lax als Default — final Prod P1 laut Addendum.

### Login-Endpoint-Stub (11.3)

11.4 implementiert Controller. Optionen für 11.3:
- **A (empfohlen):** Kein Controller — nur `permitAll` auf Pfad; unauthenticated POST → 403 CSRF oder 404 bis 11.4
- **B:** Leerer `AuthController` mit `@PostMapping("/login")` → 501 Not Implemented

**Entscheidung Story:** Option A — nur Security-Regel; 11.4 liefert Controller. IT prüft permitAll via `POST /api/auth/login` ohne Auth ≠ 401 (z. B. 403 CSRF oder 404).

### Test-Migrationsliste (AC #12 — PFLICHT)

Alle folgenden Klassen brauchen authentifizierten MockMvc-Kontext:

| Datei |
|---|
| `CgiKpiDashboardApplicationTests.java` |
| `api/portfolio/PortfolioControllerIntegrationTest.java` |
| `api/portfolio/PortfolioAiControllerIntegrationTest.java` |
| `api/projects/ProjectControllerIntegrationTest.java` |
| `api/projects/ProjectAiControllerIntegrationTest.java` |
| `api/projects/ProjectAiControllerGeminiIntegrationTest.java` |
| `api/error/GlobalExceptionHandlerIntegrationTest.java` |

**Muster:**

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class PortfolioControllerIntegrationTest { ... }
```

Für Tests die explizit Unauth prüfen: `@AutoConfigureMockMvc` ohne `@WithMockUser` in separater Klasse.

**Nicht ändern:** Bootstrap-ITs ohne MockMvc (kein HTTP). Persistence-ITs ohne `/api/**`-Calls.

Optional: `@Import(SecurityTestSupport.class)` — nur wennDRY nötig; Klassen-Level `@WithMockUser` reicht für MVP.

### Anti-Patterns (NICHT)

- `permitAll()` auf `/api/**` „temporarily“
- Security in Tests global deaktivieren (`spring.security.enabled=false`, `addFilters = false`) außer isoliertem Security-IT
- JWT / OAuth2 Resource Server Dependencies
- `@CrossOrigin(origins = "*")` als Security-Ersatz
- Zwei `PasswordEncoder`-Beans
- Login-Logik in `SecurityConfig`
- Rollen aus Request-Headern
- `WebSecurityCustomizer` ignoring `/api/**`

### Risiken

| Risiko | Mitigation |
|---|---|
| 148 Tests → 401-Flut | Systematisch `@WithMockUser` auf MockMvc-ITs |
| CSRF bricht alle POST-ITs | `@WithMockUser` + `csrf()` PostProcessor in MockMvc oder `@AutoConfigureMockMvc(addFilters = true)` mit CSRF-Token aus Cookie |
| Gemini-IT env-spezifisch | `@WithMockUser` zusätzlich zu bestehenden `@DynamicPropertySource` |
| Deaktivierter User ungetestet | `DashboardUserDetailsServiceTest` |
| Doppelter PasswordEncoder | Eine Config-Klasse owns Bean |

### Erwartete Dateien

**Neu**
- `security/config/SecurityConfig.java`
- `security/user/DashboardUserDetails.java`
- `security/user/DashboardUserDetailsService.java`
- `security/web/JsonAuthenticationEntryPoint.java`
- `security/web/JsonAccessDeniedHandler.java`
- `src/main/resources/application-prod.yml`
- `src/test/java/.../security/config/SecurityFilterChainIntegrationTest.java`
- `src/test/java/.../security/user/DashboardUserDetailsServiceTest.java` (oder IT)

**UPDATE**
- `pom.xml`
- `WorkspaceMembershipRepository.java`
- 7 MockMvc-Integrationstest-Dateien (siehe Liste)
- `README.md` (kurz: API erfordert Session ab 11.3; Login ab 11.4)

**Nicht anfassen**
- Frontend, Flyway, AI-Business-Logik, Bootstrap-Runner
- Story 10.1

## Testanforderungen

1. `GET /api/portfolio/kpis` ohne Auth → **401** + JSON body mit `code`
2. `GET /api/portfolio/kpis` mit `@WithMockUser(roles="ADMIN")` → **200** (bestehende Assertions)
3. `POST /api/projects/{id}/ai/questions` mit User, **ohne** CSRF → **403**
4. `DashboardUserDetailsService`: aktiver User → Authorities; `active=false` → Exception
5. Gesamtsuite `mvn test` grün

## Definition of Done

- [x] AC 1–13 erfüllt
- [x] Kein JWT; kein Login-Controller (11.4)
- [x] Alle MockMvc-ITs mit Test-Auth
- [x] `mvn test` grün
- [x] Single-Instance-Session dokumentiert

## Traceability

| Anforderung | Abdeckung in 11.3 |
|---|---|
| FR-22 | Session-Infrastruktur |
| NFR-13 | Default Deny `/api/**` |
| NFR-10 | JSON 401/403 |
| AD-12 | Session, CSRF, Fixation, Cookies |
| AD-13 | Default Deny (feingranular → 12.2) |

## Previous Story Intelligence (11.2)

- `PasswordEncoder` = `BCryptPasswordEncoder` in `BootstrapConfig` — wiederverwenden
- Bootstrap-Admin hat BCrypt-Hash, `mustChangePassword=true` — Login erst 11.4
- `application-test.yml`: `spring.config.override-system-properties: true` + leere Bootstrap-Props
- Isolated H2 pattern für Bootstrap-ITs — Security-ITs können Shared-H2 + `@WithMockUser` nutzen
- Review-Lesson: Mockito muss exakte Repository-Methoden verifizieren; Env-Precedence in Tests beachten
- Username trim in Bootstrap; case-sensitiv UNIQUE

## Previous Story Intelligence (11.1)

- `WorkspaceRole`: USER | ADMIN → Spring `ROLE_*`
- `WorkspaceIds.DEFAULT` für Membership-Lookup
- `AppUserRepository.findByUsername(String)`
- Deferred erledigt in 11.3: `findByUserId`

## Git Intelligence

Epic 11 lokal/uncommitted: V8 Schema, Bootstrap, crypto. Kein `starter-security` im Repo. Commit-Stil: `feat(…)` / Story-Nummern.

## Latest Tech Information

- **Spring Boot 3.5.16** / **Spring Security 6.x** (via BOM)
- Security-Konfiguration: **Lambda DSL** `SecurityFilterChain` @Bean (kein `WebSecurityConfigurerAdapter`)
- CSRF SPA: [Spring Security CSRF Cookie Repository](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa)
- Test: `@WithMockUser`, `SecurityMockMvcRequestPostProcessors.csrf()`
- `@EnableWebSecurity` implizit via Starter

## Project Structure Notes

- Package `security.*` bereits durch 11.2 angelegt — erweitern, nicht duplizieren
- API-Fehler unter `api.error` — Security-Handler in `security.web` (Filter-Ebene)
- Kein `admin.*` bis Epic 13

## References

- `_bmad-output/planning-artifacts/stories/stories-security-multi-user.md` — Story 11.3
- `_bmad-output/planning-artifacts/epics.md` — Epic 11
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/security-decisions.md`
- `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-security-multi-user/security-addendum.md`
- `_bmad-output/implementation-artifacts/11-2-bootstrap-administrator-env.md`
- `_bmad-output/implementation-artifacts/11-1-datenmodell-workspace-user-flyway.md`
- `backend/src/main/java/com/cgi/kpi/dashboard/api/error/ApiErrorResponse.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/config/BootstrapConfig.java`

## Dev Agent Record

### Agent Model Used

Composer (Cursor)

### Debug Log References

- `spring-security-test` Test-Dependency ergänzt (Compile-Fehler `@WithMockUser`/`csrf()`)
- `GlobalExceptionHandlerWebMvcTest`: `@WithMockUser(roles = "ADMIN")` für Slice-Test

### Completion Notes List

- `spring-boot-starter-security` + `spring-security-test` (test scope); `PasswordEncoder` bleibt in `BootstrapConfig`
- `SecurityConfig`: Default Deny `/api/**`, permitAll `POST /api/auth/login` + `GET /actuator/health`, CSRF Cookie-Repository, Session-Fixation `changeSessionId()`
- JSON 401/403 via `JsonAuthenticationEntryPoint` / `JsonAccessDeniedHandler` mit `ApiErrorResponse`
- `DashboardUserDetails` + `DashboardUserDetailsService` mit Membership-Lookup; disabled User → `DisabledException`
- `application-prod.yml`: Secure HttpOnly Session-Cookie
- 155 Tests grün (`mvn test`)

### File List

**Neu**
- `backend/src/main/java/com/cgi/kpi/dashboard/security/config/SecurityConfig.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/user/DashboardUserDetails.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/user/DashboardUserDetailsService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/web/JsonAuthenticationEntryPoint.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/web/JsonAccessDeniedHandler.java`
- `backend/src/main/resources/application-prod.yml`
- `backend/src/test/java/com/cgi/kpi/dashboard/security/config/SecurityFilterChainIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/security/user/DashboardUserDetailsServiceTest.java`

**Geändert**
- `backend/pom.xml`
- `backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceMembershipRepository.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandlerIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandlerWebMvcTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioAiControllerIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioControllerIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/projects/ProjectAiControllerGeminiIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/projects/ProjectAiControllerIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/projects/ProjectControllerIntegrationTest.java`
- `README.md`

### Change Log

- 2026-07-21: Code-Review-Patches — Workspace-Fallback entfernt, `/actuator/**` geschützt (außer health)
