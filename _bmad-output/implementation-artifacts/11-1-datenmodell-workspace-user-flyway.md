# Story 11.1: Datenmodell Workspace/User/Membership + Flyway-Migration

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

As a Entwickler,
I want das persistente Auth-/Workspace-Datenmodell inkl. Flyway-Migration, JPA-Entities und Repositories,
so that Benutzer, Rollen und der Default-Workspace speicherbar sind und alle bestehenden Projekte ohne Datenverlust einem Workspace zugeordnet werden (AD-14, FR-23, Grundlage für FR-22/FR-25).

**Nutzen:** Ermöglicht Bootstrap-Admin (11.2), Session-Auth (11.3+) und Workspace-Isolation (Epic 12) auf einer sicheren Schema-Basis — ohne Passwörter oder Admins in SQL.

## Scope

**In dieser Story (Implementierung, nicht nur Plan):**

- Tabellen `workspace`, `app_user`, `workspace_membership`
- Rollen `USER` | `ADMIN` (DB-Constraint + Java-Enum)
- Nutzerfelder: aktiv/deaktiviert, `must_change_password`, `failed_login_count`, `locked_until`, Zeitstempel
- Optimistische Versionierung (`@Version` / Spalte `version`) auf `app_user` und `workspace_membership` (Threat T14)
- Genau **ein** Default-Workspace (feste, dokumentierte UUID) in der Migration
- `projects.workspace_id` + vollständiger Backfill aller bestehenden Projektzeilen → danach `NOT NULL` + FK + Index
- Unique Constraint `workspace_membership(workspace_id, user_id)`
- Foreign Keys, Indizes, CHECK auf Rolle
- JPA-Entities, Enums, Spring Data Repositories
- Flyway- und Persistenz-/Schema-Tests
- Dokumentierter Extension-Point für späteres `project_membership` (nicht implementiert)
- Dokumentiertes Konzept privater Daten (`workspace_id` + `user_id`) — Tabellen erst in Story 12.1

## Out of Scope

- Bootstrap-Administrator / Env-Credentials → **Story 11.2**
- Spring Security, Session, CSRF, Login/Logout/`/me` → **11.3 / 11.4**
- Angular Auth / Guards → **11.5**
- Workspace-Scoped Queries / AuthZ-Filter / private Settings-Tabellen → **Epic 12**
- `project_membership`-Tabelle oder User↔Projekt-Zuordnung
- API-Key, Master-Key, `ai_provider_config`, Audit-Events
- Benutzer oder Passwort-Hashes in Flyway/Seed
- Standardpasswort, Default-Admin, Klartext-Secrets
- API-Controller, DTOs für Auth/Admin
- Frontend-Änderungen
- Änderung bestehender Seed-Inhalte (V2–V7) außer durch Backfill von `workspace_id`

## Abhängigkeiten

- **Voraussetzung:** MVP Epics 1–9 (Domain + Flyway V1–V7) — erfüllt
- **Keine** Abhängigkeit von Story 10.1 (bleibt parallel `ready-for-dev`, wird vorerst nicht implementiert)
- **Blockiert:** 11.2, 11.3 (UserDetails laden), Epic 12 Schema-Nutzung

## Acceptance Criteria

1. **Flyway V8** existiert unter `backend/src/main/resources/db/migration/` und ist die nächste Version nach der höchsten bestehenden Migration **V7**. Migration läuft erfolgreich:
   - auf leerer DB (V1→V8 komplett), und
   - als Upgrade einer bereits auf V7 stehenden DB.
2. **Tabellen** `workspace`, `app_user`, `workspace_membership` existieren mit den Spalten gemäß Dev Notes (Schema-Vertrag).
3. **Default-Workspace:** genau eine Workspace-Zeile mit **fester UUID** (Konstante in Migration + Java, z. B. `c0000000-0000-4000-8000-000000000001`) und stabilem Namen (z. B. `Default`); keine weiteren Workspace-Seeds.
4. **Kein** `INSERT` in `app_user` oder `workspace_membership` in Flyway; **kein** Passwort, Hash, Username oder Admin in SQL.
5. **`projects.workspace_id`:** Spalte wird nullable hinzugefügt → alle bestehenden Zeilen werden auf die Default-Workspace-UUID gebackfillt → Assert `COUNT(*) WHERE workspace_id IS NULL = 0` → Spalte wird `NOT NULL` → FK auf `workspace(id)` → Index `idx_projects_workspace_id`.
6. **Child-Fachtabellen** (`project_phases`, `milestones`, `risks`, `problems`, `project_budgets`, `project_report_snapshots`, `project_role_capacities`, `project_capacity_summaries`) erhalten in v1 **kein** eigenes `workspace_id`; Isolation läuft über `project_id` → `projects.workspace_id` (dokumentiert). Seed-Daten und Projektanzahl bleiben erhalten (kein DELETE/TRUNCATE).
7. **Constraints:**
   - `UNIQUE (workspace_id, user_id)` auf `workspace_membership`
   - `UNIQUE (username)` auf `app_user`
   - DB-`CHECK (role IN ('USER', 'ADMIN'))` auf `workspace_membership.role` (oder äquivalente Postgres-Domain/ENUM, konsistent mit bestehenden VARCHAR-Status-Konventionen → bevorzugt VARCHAR + CHECK)
   - FKs: membership → workspace, membership → app_user; projects → workspace
8. **JPA:** Entities `Workspace`, `AppUser`, `WorkspaceMembership`; Enum `WorkspaceRole { USER, ADMIN }`; `Project` um `workspaceId` (oder ManyToOne `Workspace`) erweitert; `ddl-auto: validate` bleibt grün.
9. **Repositories:** `WorkspaceRepository`, `AppUserRepository`, `WorkspaceMembershipRepository` in `infrastructure.persistence` (bestehendes Muster).
10. **Optimistische Versionierung:** Spalte `version` + JPA `@Version` auf `AppUser` und `WorkspaceMembership` (T14 / spätere Concurrent-Admin-Updates).
11. **Lockout-Felder** existieren auf `app_user`: `failed_login_count` (NOT NULL, Default 0), `locked_until` (nullable). Keine Login-Logik in dieser Story.
12. **Extension-Point:** SQL-Kommentar und Dev-Note dokumentieren späteres optionales `project_membership` (nicht angelegt).
13. **Private-Daten-Konzept:** Dev Notes beschreiben künftige Tabellen mit `workspace_id` + `user_id` (Settings/Filter/Views) — **keine** Tabellen-Erstellung in V8.
14. **Tests:**
    - `FlywayMigrationIntegrationTest`: erwartete Migrationsanzahl **8**, Current Version `"8"`, zweite `migrate()` idempotent
    - Schema-IT: Tabellen `WORKSPACE`, `APP_USER`, `WORKSPACE_MEMBERSHIP` vorhanden; `PROJECTS.WORKSPACE_ID` NOT NULL; Default-Workspace vorhanden; `app_user`-Count = 0
    - Persistenz-IT oder Repository-Test: Workspace laden; Membership mit Role speichern/laden; Unique-(workspace,user) und Role-CHECK greifen (Fehlversuch erwartbar)
    - Bestehende Domain-/API-ITs bleiben grün (Projekte weiterhin lesbar)

## Tasks / Subtasks

- [x] Schema-Vertrag finalisieren (AC: #2, #7, #10, #11)
  - [x] Spaltenlisten `workspace` / `app_user` / `workspace_membership` wie unten umsetzen
  - [x] Default-Workspace-UUID als gemeinsame Konstante (Java + SQL-Kommentar)
- [x] Flyway `V8__workspace_user_membership.sql` (AC: #1, #3–#7, #12)
  - [x] CREATE `workspace` + INSERT Default-Zeile
  - [x] CREATE `app_user` (ohne Seed-User)
  - [x] CREATE `workspace_membership` + UNIQUE + CHECK + FKs
  - [x] ALTER `projects` ADD `workspace_id` NULL → Backfill → NOT NULL → FK → Index
  - [x] Keine User/Password/Admin-INSERTs; Kommentar Extension-Point `project_membership`
- [x] JPA Domain (AC: #8, #10)
  - [x] `Workspace`, `AppUser`, `WorkspaceMembership` extends `UuidEntity`
  - [x] `WorkspaceRole` Enum (`@Enumerated(STRING)`)
  - [x] `Project`: Feld `workspaceId` (UUID) — keine `@ManyToOne`
  - [x] `@Version` auf User + Membership
- [x] Repositories (AC: #9)
  - [x] `WorkspaceRepository`, `AppUserRepository` (`findByUsername`), `WorkspaceMembershipRepository` (`findByWorkspaceIdAndUserId`, …)
- [x] Tests (AC: #14)
  - [x] `FlywayMigrationIntegrationTest` Count/Version → 8
  - [x] `FlywayDomainSchemaIntegrationTest` / neuer Schema-Test um Auth-Tabellen + `workspace_id` erweitern
  - [x] Neuer Persistenz-Test für Membership-Constraints
  - [x] Regression: bestehende `@SpringBootTest`-Suiten grün
- [x] DoD-Checkliste abhaken (siehe unten)

### Review Findings

- [x] [Review][Patch] Role-CHECK-Test assertiert nur generische PersistenceException statt Constraint-Verletzung [`WorkspaceMembershipPersistenceIntegrationTest.java:126`]
- [x] [Review][Patch] `DomainRepositoryIntegrationTest` prüft `workspaceId`-Roundtrip nach Persist nicht [`DomainRepositoryIntegrationTest.java:50`]
- [x] [Review][Patch] Negativer Test für `UNIQUE(username)` fehlt [`AppUser.java` / `WorkspaceMembershipPersistenceIntegrationTest`]
- [x] [Review][Patch] FK-Negative-Test für ungültige `workspace_id`/`user_id` bei Membership fehlt [`WorkspaceMembershipPersistenceIntegrationTest`]
- [x] [Review][Defer] Username case-sensitiv (PostgreSQL UNIQUE ohne Normalisierung) — deferred, Story 11.2 Bootstrap/Login
- [x] [Review][Defer] Leerer Username / leerer `password_hash` schema-konform — deferred, Validierung in 11.2
- [x] [Review][Defer] `failed_login_count` ohne CHECK `>= 0` — deferred, Lockout-Logik Story 11.3
- [x] [Review][Defer] Nur H2-ITs, kein PostgreSQL/Testcontainers — deferred, Projekt-Pattern seit Epic 3
- [x] [Review][Defer] Isolierter V7→V8-Upgrade-Test fehlt — deferred, Endzustand via FlywayMigrationIntegrationTest abgedeckt
- [x] [Review][Defer] `WorkspaceMembershipRepository.findByUserId` fehlt — deferred, Story 11.3 UserDetails
- [x] [Review][Defer] Unit-Tests bauen `Project` ohne `workspaceId` (In-Memory) — deferred, kein Persist-Pfad

## Dev Notes

### Flyway-Version (verbindlich)

| Ist | Nächste Migration |
|---|---|
| Höchste vorhandene: **V7** (`V7__mock_seed_issues_capacity.sql`) | **`V8__workspace_user_membership.sql`** |

Vor Implementierung erneut `backend/src/main/resources/db/migration/` prüfen — falls inzwischen V8+ existiert, nächste freie Nummer wählen und diese Story-Datei anpassen.

### Schema-Vertrag (V8)

**`workspace`**
| Spalte | Typ | Constraints |
|---|---|---|
| `id` | UUID | PK |
| `name` | VARCHAR(200) | NOT NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| `updated_at` | TIMESTAMPTZ | NOT NULL |

**Default-Workspace-INSERT:**  
`id = c0000000-0000-4000-8000-000000000001`, `name = 'Default'` (oder `Default Workspace`). UUID darf nicht mit Seed-Projekten `a0000000-…` kollidieren.

**`app_user`**
| Spalte | Typ | Constraints |
|---|---|---|
| `id` | UUID | PK |
| `username` | VARCHAR(100) | NOT NULL, UNIQUE |
| `password_hash` | VARCHAR(255) | NOT NULL — Spalte existiert; **keine** Zeilen in V8 |
| `active` | BOOLEAN | NOT NULL DEFAULT TRUE |
| `must_change_password` | BOOLEAN | NOT NULL DEFAULT FALSE |
| `failed_login_count` | INTEGER | NOT NULL DEFAULT 0 |
| `locked_until` | TIMESTAMPTZ | NULL |
| `created_at` / `updated_at` | TIMESTAMPTZ | NOT NULL |
| `version` | BIGINT | NOT NULL DEFAULT 0 (@Version) |

**Hinweis `password_hash NOT NULL`:** OK, solange keine User-Zeilen ohne Hash eingefügt werden. Bootstrap (11.2) setzt immer einen Hash.

**`workspace_membership`**
| Spalte | Typ | Constraints |
|---|---|---|
| `id` | UUID | PK |
| `workspace_id` | UUID | NOT NULL FK → workspace |
| `user_id` | UUID | NOT NULL FK → app_user |
| `role` | VARCHAR(20) | NOT NULL, CHECK IN ('USER','ADMIN') |
| `created_at` / `updated_at` | TIMESTAMPTZ | NOT NULL |
| `version` | BIGINT | NOT NULL DEFAULT 0 (@Version) |
| | | UNIQUE (`workspace_id`, `user_id`) |

Indizes: zusätzlich zu UNIQUE/PK z. B. `idx_workspace_membership_user_id` (Lookup Memberships eines Users).

**`projects` (Delta)**
```sql
ALTER TABLE projects ADD COLUMN workspace_id UUID NULL;
UPDATE projects SET workspace_id = 'c0000000-0000-4000-8000-000000000001' WHERE workspace_id IS NULL;
-- optional guard: DO $$ BEGIN IF EXISTS (SELECT 1 FROM projects WHERE workspace_id IS NULL) THEN RAISE EXCEPTION 'backfill incomplete'; END IF; END $$;
ALTER TABLE projects ALTER COLUMN workspace_id SET NOT NULL;
ALTER TABLE projects ADD CONSTRAINT fk_projects_workspace FOREIGN KEY (workspace_id) REFERENCES workspace (id);
CREATE INDEX idx_projects_workspace_id ON projects (workspace_id);
```

### Architektur-Compliance

| ID | Vorgabe für 11.1 |
|---|---|
| **AD-14** | Default-Workspace; Backfill; kein `project_membership` v1; Extension dokumentieren |
| **AD-12** | Bootstrap/Passwort **nicht** in Flyway (nur Schema-Vorbereitung für User) |
| **AD-13** | Rollen USER/ADMIN im Modell (AuthZ-Enforcement später) |
| **FR-23** | Rollenmodell persistierbar |
| **FR-25 / AD-14** | `workspace_id` auf Projekten als Isolationswurzel |
| **FR-31** | `active`-Flag vorbereitet |
| **NFR-12** | Nur Hash-Spalte; kein Klartext |
| **NFR-18 / T07** | `failed_login_count`, `locked_until` Spalten (Logik später) |
| **T14 / T16** | `@Version`; kein Flyway-Admin-Passwort |
| **UuidEntity** | Alle neuen Entities erben UUID-PK-Konvention |

Quellen:
- [Source: `_bmad-output/planning-artifacts/stories/stories-security-multi-user.md` — Story 11.1]
- [Source: `_bmad-output/planning-artifacts/architecture/.../ARCHITECTURE-SPINE.md` — Schema-Migration, Invariants]
- [Source: `_bmad-output/planning-artifacts/architecture/.../security-decisions.md` — AD-12/14]
- [Source: `_bmad-output/planning-artifacts/prds/.../prd.md` — §3.1, §3.5, FR-23]
- [Source: `_bmad-output/planning-artifacts/prds/.../security-addendum.md` — Workspace-Diagramm, Bootstrap-Verbote]
- [Source: `_bmad-output/planning-artifacts/architecture/.../threat-model.md` — T07, T14, T16]

### Bestehende Code-Muster (MUST folgen)

- Entities: `backend/.../domain/model/*` extends `UuidEntity`
- Repositories: `backend/.../infrastructure/persistence/*Repository`
- Flyway: `classpath:db/migration`, `ddl-auto: validate`
- Status-Werte bisher als VARCHAR (nicht Postgres ENUM) — Rolle ebenfalls VARCHAR + CHECK
- Tests: `@SpringBootTest` + `@ActiveProfiles("test")`

### Anti-Patterns (NICHT)

- User/Admin/Passwort in V8 oder Seed-Anpassung V2–V7
- `project_membership` anlegen
- Private-Settings-Tabellen in V8 (→ 12.1)
- Spring Security Starter „nebenbei“ hinzufügen
- `workspace_id` redundant auf alle Child-Tabellen denormalisieren (Scope-Creep, Sync-Risiko)
- Hibernate `ddl-auto: update` statt Flyway
- JWT / localStorage Felder „vorbereiten“

### Private-Daten-Konzept (nur Dokumentation, 12.1)

Künftige Tabellen (Namen beispielhaft): `user_ui_preference`, `user_saved_filter`, … jeweils:
- `workspace_id` NOT NULL FK
- `user_id` NOT NULL FK
- Werte **nur** aus Security Context, nie aus Client-IDs (AD-13/14)

### Extension-Point `project_membership`

Später optional: Tabelle `(id, workspace_id, project_id, user_id, …)` für User↔Projekt. v1: alle aktiven Members sehen alle Workspace-Projekte. In V8 nur SQL-Kommentar, keine Tabelle.

### Risiken

| Risiko | Mitigation |
|---|---|
| Backfill vergisst Zeilen → NOT NULL scheitert | Guard-Query vor NOT NULL; IT zählt `workspace_id IS NULL = 0` |
| JPA validate bricht ohne Entity-Update | `Project.workspaceId` zwingend mitliefern |
| Feste UUID driftet zwischen SQL/Java | Eine Konstante + Test assertet ID |
| `password_hash NOT NULL` blockiert spätere Flows | 11.2 schreibt immer Hash; keine leeren User-Inserts |
| Child ohne workspace_id missverstanden als Lücke | Explizit in AC #6 und DoD |

## Erwartete Dateien

**Neu**
- `backend/src/main/resources/db/migration/V8__workspace_user_membership.sql`
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Workspace.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/AppUser.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/WorkspaceMembership.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/WorkspaceRole.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceRepository.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/AppUserRepository.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceMembershipRepository.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceUserSchemaIntegrationTest.java` (Name frei wählbar)
- Optional: `…/WorkspaceMembershipRepositoryTest.java` / Constraint-IT

**UPDATE**
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Project.java` (`workspaceId`)
- `backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayMigrationIntegrationTest.java` (Count 7→8, Version `"8"`)
- `backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayDomainSchemaIntegrationTest.java` (Auth-Tabellen; ggf. Capacity-Tabellen nachziehen falls fehlend)

**Nicht anfassen**
- Frontend, Security-Config, AI-Services, V1–V7 SQL-Inhalte (außer Lesen), Bootstrap-Runner

## Testanforderungen

1. Frische Migrate → Version 8, alle Tabellen, Default-WS, 0 User, alle Projekte mit Default-`workspace_id`
2. Zweite Migrate → 0 executed, validate OK
3. Persistenz: Membership speichern mit `ADMIN`/`USER`; ungültige Role → DB-Fehler; Doppel-Membership → Unique-Verletzung
4. Bestehende Projekt-Seed-Anzahl unverändert (z. B. bisherige 20 Projekte bleiben)
5. Anwendungskontext startet mit `ddl-auto: validate`

## Definition of Done

- [x] Alle ACs 1–14 erfüllt
- [x] V8 ohne User/Passwort/Admin-Seeds
- [x] Backfill vollständig; `projects.workspace_id` NOT NULL + FK + Index
- [x] Entities + Repositories + Tests grün
- [x] Kein Spring-Security-/Login-/Angular-Code
- [x] Extension-Point + Private-Daten-Konzept dokumentiert
- [x] Story-Status nach Implementierung → `review`

## Traceability

| Anforderung | Abdeckung in 11.1 |
|---|---|
| FR-23 | Role Enum + Membership + CHECK |
| FR-25 / AD-14 | Default-WS + `projects.workspace_id` Backfill |
| FR-31 | `app_user.active` |
| FR-22 (Vorbereitung) | User-Tabelle ohne Auth-Endpoints |
| NFR-12 | `password_hash`-Spalte, kein Klartext |
| NFR-18 | Lockout-Spalten |
| AD-12 | Kein Flyway-Passwort / kein Bootstrap hier |
| AD-13 | USER/ADMIN Modell |
| AD-14 | Schema-Migration wie Spine |
| T07 / T14 / T16 | Lockout-Felder, @Version, kein Seed-Admin |

## Project Structure Notes

- Package-Delta Security-Spine (`…security/`, `…admin/`) **noch nicht** anlegen — erst ab 11.3/13.x
- Domain bleibt unter `domain.model`; Persistenz unter `infrastructure.persistence`

## References

- `_bmad-output/planning-artifacts/stories/stories-security-multi-user.md` — Epic 11 / Story 11.1
- `_bmad-output/planning-artifacts/epics.md` — Epic 11
- `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-security-multi-user/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-security-multi-user/security-addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/ARCHITECTURE-SPINE.md`
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/security-decisions.md`
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/threat-model.md`
- `backend/src/main/resources/db/migration/V1__*.sql` … `V7__*.sql`
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Project.java`, `UuidEntity.java`

## Dev Agent Record

### Agent Model Used

Cursor Grok 4.5

### Debug Log References

- V8: PostgreSQL `DO $$` Block entfernt — inkompatibel mit H2 (PostgreSQL-MODE); Guard über `SET NOT NULL` nach Backfill.

### Completion Notes List

- Flyway V8: workspace + Default Workspace, app_user, workspace_membership, projects.workspace_id Backfill
- Project.workspaceId als UUID-Feld (keine ManyToOne)
- Persistenz-/Schema-ITs + alle Backend-Tests grün (`mvn test`)
- Kein Bootstrap/Security/Login/Angular

### File List

- backend/src/main/resources/db/migration/V8__workspace_user_membership.sql
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/WorkspaceIds.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/WorkspaceRole.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Workspace.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/AppUser.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/WorkspaceMembership.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Project.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/AppUserRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceMembershipRepository.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayMigrationIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayDomainSchemaIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceUserSchemaIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/WorkspaceMembershipPersistenceIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/DomainRepositoryIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/DomainExtensionIntegrationTest.java
- _bmad-output/implementation-artifacts/11-1-datenmodell-workspace-user-flyway.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-21: Story 11.1 implementiert — Auth/Workspace-Schema V8 + JPA + Tests; Status → review
