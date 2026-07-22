# Story 12.1: Workspace-Scope auf Repositories + Private Settings Isolation

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

As a authentifizierter Workspace-Nutzer,
I want dass Portfolio- und Projektdaten nur aus meinem Workspace kommen und private Einstellungen nur mir gehören,
so that kein Cross-Workspace-Zugriff und kein Fremdzugriff auf persönliche Settings möglich ist (FR-25, AD-13, AD-14).

## Scope

**In dieser Story:**

- Zentraler Zugriff auf den Security-Principal (`userId`, `workspaceId`) für KPI-/Query-Layer (nicht nur Auth)
- Alle gemeinsamen Portfolio-/Projekt-Queries workspace-scoped über `projects.workspace_id`
- Projekt-Zugriff per ID nur wenn `project.workspaceId == principal.workspaceId` (sonst 404)
- Client-gesendete `workspaceId` / `userId` werden für AuthZ **ignoriert** (nie als Quelle)
- Private Settings: Persistenz-Tabelle + API, Isolation `workspace_id` + `user_id` aus Context
- Basis-Isolationstests (vollständige Suite bleibt Story 12.4)

**Out of Scope:**

- Rollen-Policies KI nur ADMIN / `@PreAuthorize` → **Story 12.2**
- AI-Cache-Key mit `workspaceId` → **Story 12.3**
- Dedizierte Fremdzugriffs-Gesamtsuite → **Story 12.4**
- `project_membership` / Multi-Workspace-Auswahl
- Frontend-UI für gespeicherte Ansichten (optionaler Sync darf minimal bleiben)
- Benutzerverwaltung / Admin-APIs → Epic 13

## Abhängigkeiten

- **Voraussetzung:** Epic 11 done (V8 Schema, Session, `DashboardUserDetails` mit `workspaceId`)
- **Blockiert:** 12.2 (sinnvoll auf scoped Daten), 12.4 (Isolationstests)

## Acceptance Criteria

1. **Current-User-Helper** existiert (z. B. `CurrentUserService` / `SecuritySupport`) und liefert `requireUserId()`, `requireWorkspaceId()`, `requirePrincipal()` aus dem Security Context; wirft 401 wenn unauthentifiziert.
2. **`ProjectRepository`** hat mind.:
   - `List<Project> findAllByWorkspaceId(UUID workspaceId)`
   - `Optional<Project> findByIdAndWorkspaceId(UUID id, UUID workspaceId)`
3. **Portfolio-Reader** (`JpaPortfolioKpiReader`, `JpaPortfolioTableReader`, `JpaPortfolioTimelineReader`, `JpaPortfolioTrendReader`, `JpaPortfolioReportTrendReader`) laden Projekte nur über Workspace-Scope — **kein** unscoped `projectRepository.findAll()` mehr für fachliche Daten.
4. **Projekt-Queries** (`DefaultProjectQueryService`, `JpaProjectKpiReader` und abhängige Pfade) nutzen `findByIdAndWorkspaceId` (bzw. äquivalent); fremde Workspace-IDs → **404** `NOT_FOUND` (nicht 403).
5. **Kein** Request-Parameter `workspaceId`/`userId` steuert AuthZ; Controllers bleiben schlank, Scoping im Service/Reader.
6. **Private Settings:**
   - Flyway **V9**: Tabelle (Name z. B. `user_ui_preferences`) mit `workspace_id`, `user_id`, Payload (JSON/Text), Timestamps; Unique `(workspace_id, user_id)`
   - Entity + Repository: Lookup nur per Context-IDs
   - API: `GET` + `PUT /api/me/preferences` (oder `/api/users/me/preferences`) — IDs ausschließlich aus Principal; Body darf keine AuthZ-IDs erzwingen
7. **Isolationstests (mind.):**
   - Zwei Workspaces + Projekte; User nur in WS-A → Portfolio liefert nur WS-A; Projekt aus WS-B → 404
   - Preferences User A ≠ User B (kein Cross-Read/Write)
   - Manipulierter Body/`workspaceId` ändert nicht den Scope
8. Bestehende Auth-/Portfolio-/Projekt-ITs bleiben grün (Default-Workspace-Daten weiterhin sichtbar für Members).

## Tasks / Subtasks

- [x] Current-User-Helper im Security-Package (AC: #1)
- [x] `ProjectRepository` Scope-Methoden (AC: #2)
- [x] Portfolio-Reader auf `findAllByWorkspaceId` umstellen (AC: #3)
- [x] Project Query + KPI Reader IDOR-sicher (AC: #4)
- [x] Services injizieren Principal-Workspace (nicht Client) (AC: #5)
- [x] Flyway V9 + Entity/Repo/API Private Settings (AC: #6)
- [x] Isolationstests Workspace + Preferences (AC: #7)
- [x] Regression: bestehende relevante Maven-ITs grün (AC: #8)

### Review Findings

- [x] [Review][Patch] Portfolio-Isolationstest prüft falschen JsonPath — `$[*].name` trifft auf `PortfolioTableDto`-Root nicht zu; korrekt: `$.projects[*].name`. Test ist vacuous true und würde Cross-Workspace-Leaks nicht erkennen. [`WorkspaceIsolationIntegrationTest.java:114`]
- [x] [Review][Patch] Preferences-Identity-Sanitisierung nur auf Root-Ebene — `writeJson()` entfernt `userId`/`workspaceId` nur im Top-Level-`ObjectNode`; verschachtelte Felder bleiben persistiert. `getPreferences()` liefert gespeichertes JSON ungefiltert zurück. [`UserPreferencesService.java:64-85`]
- [x] [Review][Patch] Isolationstest-Abdeckung erweitern — mind. ein weiterer Portfolio-Endpunkt (z. B. `/api/portfolio/kpis`) und ggf. ein Projekt-Sub-Endpunkt gegen Fremd-Workspace absichern. [`WorkspaceIsolationIntegrationTest.java`]
- [x] [Review][Defer] Kind-Repos laden global per `findAll().filter(workspaceProjectIds)` — kein IDOR solange Filter korrekt; Story-Dev-Notes markieren `findByProject_IdIn` als Performance-Follow-up. [`JpaPortfolioKpiReader.java:73-82`]
- [x] [Review][Defer] Race bei erstem Preferences-PUT — parallele erste PUTs können `DataIntegrityViolationException` → 500 werfen; kein Handler in `GlobalExceptionHandler`. [`UserPreferencesService.java:43-61`]
- [x] [Review][Defer] Korruptes `preferences_json` wird beim GET still zu `{}` — `parseJson()` schluckt `JsonProcessingException`. [`UserPreferencesService.java:64-69`]
- [x] [Review][Defer] Doppelter `riskRepository.findAll()` in `JpaPortfolioTableReader.readTable()` — Performance, kein Isolationsrisiko. [`JpaPortfolioTableReader.java:80-93`]

## Dev Notes

### Architektur-Invarianten

- **AD-13:** Client-IDs keine AuthZ-Quelle; private Daten = Context `userId`; gemeinsame Daten = Workspace-Membership.
- **AD-14:** v1 ein Default-Workspace; alle Members sehen alle WS-Projekte; kein `project_membership`.
- Kind-Tabellen (Risks, Phases, …) behalten **kein** eigenes `workspace_id` — Scope über `project_id` → `projects.workspace_id`.
- Fremdes Projekt → **404** (Informationsminimierung; bestehendes `NOT_FOUND`-Muster).

### Ist-Zustand (Gaps)

| Bereich | Status |
|---------|--------|
| `projects.workspace_id` + V8 | vorhanden |
| `DashboardUserDetails.workspaceId` | vorhanden, nur Auth-Layer |
| Portfolio/Project Reader | noch `findAll()` / `findById` unscoped |
| Private Settings Backend | fehlt (V8 deferred) |
| Frontend-Filter | `PortfolioFilterService` in-memory only |

### Konkrete Touchpoints

**Security**

- Neu: `com.cgi.kpi.dashboard.security.user.CurrentUserService` (oder ähnlich)
- Wiederverwenden: `DashboardUserDetails`, `WorkspaceIds.DEFAULT` für Test-Seeds

**Persistence / KPI**

- `ProjectRepository` erweitern
- `JpaPortfolioKpiReader`, `JpaPortfolioTableReader`, `JpaPortfolioTimelineReader`, `JpaPortfolioTrendReader`, `JpaPortfolioReportTrendReader`
- `JpaProjectKpiReader`, `DefaultProjectQueryService`
- Optional: Kind-Repos mit `findByProject_IdIn` statt `findAll().filter` — Performance-Follow-up, nicht Blocker wenn WS-Projektliste klein bleibt

**Private Settings V9 (Vorschlag)**

```sql
CREATE TABLE user_ui_preferences (
  id UUID PRIMARY KEY,
  workspace_id UUID NOT NULL REFERENCES workspace(id),
  user_id UUID NOT NULL REFERENCES app_user(id),
  preferences_json TEXT NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (workspace_id, user_id)
);
```

API-Beispiel:

- `GET /api/me/preferences` → `{ "preferences": { ... } }`
- `PUT /api/me/preferences` → Body nur Preferences-Payload; speichern unter Context `(workspaceId, userId)`

Frontend: optional `PortfolioFilterService` kann später syncen — **nicht** erforderlich für AC, solange Backend-Isolation getestet ist.

### Testing Standards

- Pattern: `@SpringBootTest` + `@AutoConfigureMockMvc` + isolierte H2-URL (`UUID` im JDBC-URL) wie Auth-ITs
- Bootstrap-Credentials in Isolationstests leeren; User/Membership/Projekte manuell seedеn
- Keine echten Secrets/Passwörter in Logs; Test-Passwörter synthetisch
- CSRF für Writes: `.with(csrf())`

### Previous Story Intelligence

- Story 11.1: Private Settings-Tabellen bewusst deferred hierher
- Story 11.6: `ActiveAccountSessionFilter` revalidiert User; Principal bleibt `DashboardUserDetails`
- Deferred: Rollen-Enforcement URL → 12.2; Cache → 12.3

### Anti-Patterns (vermeiden)

- `workspaceId` aus Query-String/Header als Trust-Source
- 403 bei fremdem Projekt (→ 404)
- `findAll()` für Portfolio nach dieser Story
- Preferences ohne `user_id` Unique
- KI-Policies hier miterledigen (12.2)

### References

- [Source: `_bmad-output/planning-artifacts/stories/stories-security-multi-user.md` — Story 12.1]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/security-decisions.md` — AD-13, AD-14]
- [Source: `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-security-multi-user/prd.md` — §3.1–3.2, FR-25]
- [Source: `_bmad-output/implementation-artifacts/11-1-datenmodell-workspace-user-flyway.md` — Deferred Private Settings]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
