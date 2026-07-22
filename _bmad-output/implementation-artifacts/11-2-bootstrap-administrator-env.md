# Story 11.2: Bootstrap-Administrator (Env)

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

As a Betreiber,
I want genau einen initialen Admin-Benutzer aus Umgebungsvariablen erzeugen,
so that kein festes Passwort im Repo oder in Flyway liegt und der erste Login erst nach Passwortwechsel möglich ist (AD-12, FR-22, T16).

**Nutzen:** Erster sicherer Admin ohne SQL-Seeds; Voraussetzung für Spring Security (11.3) und Login (11.4). Fehlende Env → App startet, aber ohne Login-Möglichkeit bis Ops konfiguriert.

## Scope

**In dieser Story (Backend only):**

- Startup-Runner, der **nur wenn `app_user`-Count = 0** einen Admin anlegt
- Env-Credentials: `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` (nur Env, nie hardcoded)
- Passwort mit **BCrypt** hashen vor Persistenz (`password_hash`)
- `must_change_password = true`, `active = true`
- `WorkspaceMembership` mit Rolle `ADMIN` im Default-Workspace (`WorkspaceIds.DEFAULT`)
- Idempotenz: nach erstem erfolgreichen Bootstrap **kein** weiterer Admin, auch nicht bei gesetzter Env
- Fehlende/leere Env → **kein** User, **kein** Default-Passwort; App startet normal
- Logging ohne Klartext-Passwort (Username ok, Password **nie**)
- `spring-security-crypto` für BCrypt ( **nicht** `spring-boot-starter-security` — kommt in 11.3)
- Integrationstests für Bootstrap-Regeln
- `.env.example` + README-Hinweis für Bootstrap-Env (ohne echte Werte)

## Out of Scope

- Spring Security Filter Chain, Session, CSRF → **11.3**
- Login/Logout/`/api/auth/me`, Passwortwechsel-Endpoint → **11.4**
- Angular Login-UI → **11.5**
- Vollständige Auth-Test-Suite (CSRF, parallele Sessions) → **11.6**
- Flyway-Migrationen / Schema-Änderungen (V8 aus 11.1 reicht)
- Username-Normalisierung (Lowercase) — Entscheidung offen, v1: **trim + non-blank**, Case beibehalten (siehe 11.1 Defer)
- Lockout, Rate-Limit, Login-Validierung
- Audit-Events
- Custom HealthIndicator (optional, siehe Dev Notes — nicht blockierend)
- Frontend-Änderungen

## Abhängigkeiten

- **Voraussetzung:** Story **11.1 done** — Tabellen `app_user`, `workspace_membership`, Default-Workspace V8
- **Blockiert:** 11.3 (UserDetails kann Bootstrap-User laden), 11.4 (Login braucht existierenden Admin)
- **Parallel:** Story 10.1 bleibt `ready-for-dev` / pausiert — nicht anfassen

## Acceptance Criteria

1. **Trigger:** Bootstrap läuft beim Application-Start **nur wenn** `AppUserRepository.count() == 0`. Existiert mindestens ein User → Runner beendet ohne Schreibzugriff (auch wenn Env gesetzt).
2. **Env-Quelle:** Credentials ausschließlich aus `BOOTSTRAP_ADMIN_USERNAME` und `BOOTSTRAP_ADMIN_PASSWORD` (Spring-Property-Binding über `application.yml` mit `${…}` erlaubt; **keine** Defaults wie `admin`/`password`).
3. **Fehlende Env:** Wenn Count = 0 und Username **oder** Password fehlt/leer (nach `trim`) → **kein** Insert; App startet; Log auf INFO/WARN ohne Secrets (z. B. „Bootstrap skipped: no credentials configured“).
4. **User-Anlage:** Bei Count = 0 und gültiger Env:
   - `AppUser`: `username` (getrimmt), `password_hash` = BCrypt-Hash, `active=true`, `must_change_password=true`, `failed_login_count=0`
   - genau **eine** Zeile in `app_user`
5. **Membership:** Zugehörige `WorkspaceMembership`: `workspace_id = WorkspaceIds.DEFAULT`, `role = ADMIN`, FK auf den neuen User.
6. **Idempotenz:** Zweiter Application-Start (oder erneuter Runner-Aufruf) bei bereits vorhandenem User → Count unverändert; **kein** zweiter Admin/Membership.
7. **Kein Klartext in DB/Logs:** `password_hash` ≠ Klartext; Logs enthalten weder Env-Passwort noch Klartext — Integrationstest oder Log-Appender-Assertion.
8. **Hash-Format:** Gespeicherter Hash ist BCrypt (z. B. Präfix `$2a$` / `$2b$`); Verifikation mit demselben `PasswordEncoder` in Tests.
9. **Kein Flyway-Seed:** Keine SQL-Änderung, die User/Passwörter einfügt.
10. **Dependency-Grenze:** `spring-security-crypto` hinzugefügt; **`spring-boot-starter-security` nicht** hinzufügen (explizit 11.3).
11. **Dokumentation:** `backend/.env.example` enthält kommentierte `BOOTSTRAP_ADMIN_*`-Variablen; README verweist kurz auf Bootstrap für ersten Admin.
12. **Tests:**
    - Bootstrap mit Env + leerer DB → 1 User, 1 ADMIN-Membership, `mustChangePassword=true`
    - Erneuter Start → weiterhin 1 User
    - Ohne Env → 0 User, Context startet
    - Mit Env aber pre-seeded User → kein zusätzlicher User
    - Ungültige/leere Credentials → 0 User
    - Bestehende Backend-Suite bleibt grün (`mvn test`)

## Tasks / Subtasks

- [x] Maven: `spring-security-crypto` (AC: #10)
  - [x] Kein `spring-boot-starter-security`
- [x] Config (AC: #2, #3)
  - [x] `BootstrapProperties` + `BootstrapConfig` (`@ConfigurationProperties(prefix = "app.bootstrap")`)
  - [x] `application.yml`: `admin-username: ${BOOTSTRAP_ADMIN_USERNAME:}`, `admin-password: ${BOOTSTRAP_ADMIN_PASSWORD:}`
  - [x] Hilfsmethoden `hasCredentials()`, `trimmedUsername()` — blank → skip
- [x] Bootstrap-Logik (AC: #1, #4–#6)
  - [x] `BootstrapAdminService` (transaktional) oder direkt im Runner
  - [x] `BCryptPasswordEncoder` als `@Bean` (Strength default 10 ok)
  - [x] `BootstrapAdminRunner` (`ApplicationRunner` oder `ApplicationReadyEvent` **nach** Flyway)
  - [x] Count-Check → Workspace existiert (`WorkspaceIds.DEFAULT`) → User + Membership speichern
- [x] Logging (AC: #7)
  - [x] INFO bei Erfolg (Username ok); WARN bei Skip (keine Credentials / User existiert)
  - [x] Niemals Password loggen — auch nicht in `toString()` von Properties
- [x] Tests (AC: #12)
  - [x] `BootstrapAdminIntegrationTest` mit `@DynamicPropertySource` für Env
  - [x] Assertions: Count, Role, mustChangePassword, BCrypt-Hash, Idempotenz
  - [x] Log-Test oder sicherstellen Properties nicht in Logs
- [x] Docs (AC: #11)
  - [x] `backend/.env.example`, `README.md` (kurzer Abschnitt)
- [x] Regression: `mvn test` grün

### Review Findings

- [x] [Review][Patch] Mockito-Verifikation prüft `save()` statt `saveAndFlush()` — Skip-Tests würden bei unerwünschter Persistenz grün bleiben [`BootstrapAdminServiceTest.java:67-105`]
- [x] [Review][Patch] OS-Env `BOOTSTRAP_ADMIN_*` kann `application-test.yml` überschreiben — Shared-H2-Tests ohne isolierte DB könnten unbeabsichtigt Bootstrap auslösen [`application-test.yml`, `BootstrapAdminNoCredentialsIntegrationTest.java`]
- [x] [Review][Patch] AC 12: kein Full-Context-IT für pre-seeded User + Env (nur Mock-Unit-Test) [`BootstrapAdminServiceTest.java:59-68`]
- [x] [Review][Patch] AC 12: kein Full-Context-IT für leere/Whitespace-Credentials auf DB-Ebene (blank Username, whitespace-only Password) [`BootstrapAdminNoCredentialsIntegrationTest.java`]
- [x] [Review][Patch] Username >100 Zeichen: Skip-Pfad implementiert, aber ohne Test [`BootstrapAdminService.java:58-61`]
- [x] [Review][Defer] TOCTOU-Race bei parallelem JVM-Start (zwei Instanzen, count==0) — v1 Single-Instance laut AD-12; UNIQUE(username) als harter Stop [`BootstrapAdminService.java:43-78`]
- [x] [Review][Defer] Bootstrap dauerhaft blockiert wenn irgendein `app_user` existiert (auch inaktiv/nicht-ADMIN) — by design count()-Trigger [`BootstrapAdminService.java:44-46`]
- [x] [Review][Defer] `DefaultWorkspaceMissingException` bricht App-Start ab — gewollt laut Story/User-Anforderung; kein Integrationstest für Startup-Failure [`BootstrapAdminRunner.java:17-18`]
- [x] [Review][Defer] Kein echtes Rollback-IT (User gespeichert, Membership scheitert) — Exception vor Inserts; TX-Rollback via `@Transactional` ausreichend für v1 [`BootstrapAdminServiceTest.java:94-105`]
- [x] [Review][Defer] Klartext-Passwort bleibt in `BootstrapProperties` im Heap — Standard Spring-Binding; kein Wipe vorgesehen [`BootstrapProperties.java`]
- [x] [Review][Defer] Logging-Test nur Erfolgspfad — Skip-Pfade nicht auf Secret-Leak geprüft [`BootstrapAdminLoggingTest.java`]

## Dev Notes

### Architektur-Compliance

| ID | Vorgabe für 11.2 |
|---|---|
| **AD-12** | Env-only Bootstrap; Hash; Force-Change-Flag; kein Flyway-Passwort |
| **AD-14** | Admin-Membership im Default-Workspace |
| **FR-22** | Identifizierbarer initialer Admin (ohne Login-Endpoints) |
| **NFR-12** | Nur Hash in DB |
| **T16** | Kein Seed-Passwort; Bootstrap-Tests |

Quellen:
- [Source: `stories/stories-security-multi-user.md` — Story 11.2]
- [Source: `security-addendum.md` — Bootstrap Administrator]
- [Source: `security-decisions.md` — AD-12 Bootstrap-Abschnitt]
- [Source: `threat-model.md` — T16]

### Bestehender Code (MUST lesen vor Implementierung)

| Datei | Ist-Zustand | Was 11.2 ändert |
|---|---|---|
| `AppUser.java` | Entity mit `passwordHash`, `mustChangePassword`, `active` | Nur via Bootstrap befüllen |
| `WorkspaceMembership.java` | UUID-FKs, `WorkspaceRole` enum | ADMIN-Zeile anlegen |
| `WorkspaceIds.java` | `DEFAULT` UUID + Name | Membership-Referenz |
| `AppUserRepository.java` | `count()`, `findByUsername` | Count = Trigger |
| `WorkspaceMembershipRepository.java` | `findByWorkspaceIdAndUserId` | Nach Bootstrap prüfbar |
| `AiProperties` / `AiConfig` | `@ConfigurationProperties`-Muster | Gleiches Muster für Bootstrap |
| `AiStartupDiagnostics` | `@EventListener(ApplicationReadyEvent)` | Analog für Runner-Timing |
| `pom.xml` | Kein Security | Nur `spring-security-crypto` |
| `application.yml` | `app.ai.*` | Ergänzen `app.bootstrap.*` |
| V8 Migration | Default-Workspace, 0 User | Workspace muss existieren |

### Empfohlene Package-Struktur (neu)

```
backend/src/main/java/com/cgi/kpi/dashboard/security/
  bootstrap/
    BootstrapAdminRunner.java      # ApplicationRunner
    BootstrapAdminService.java     # transaktionale Anlage (testbar)
  config/
    BootstrapProperties.java
    BootstrapConfig.java           # @EnableConfigurationProperties + PasswordEncoder @Bean
```

**Nicht** `SecurityConfig` / FilterChain — das ist 11.3.

### Property-/Env-Vertrag

```yaml
# application.yml (Ergänzung)
app:
  bootstrap:
    admin-username: ${BOOTSTRAP_ADMIN_USERNAME:}
    admin-password: ${BOOTSTRAP_ADMIN_PASSWORD:}
```

| Variable | Pflicht | Verhalten |
|---|---|---|
| `BOOTSTRAP_ADMIN_USERNAME` | Nur wenn Count=0 | Getrimmt; leer → Skip |
| `BOOTSTRAP_ADMIN_PASSWORD` | Nur wenn Count=0 | Nicht trimmen für Passwort-Inhalt (nur blank-check); leer → Skip |

**Keine** Fallback-Werte wie `admin`/`changeme` in YAML.

### Bootstrap-Ablauf (Sequenz)

```
ApplicationReady / ApplicationRunner
  → if appUserRepository.count() > 0 → return (INFO: users exist)
  → if !bootstrapProperties.hasCredentials() → return (WARN: skipped)
  → assert workspaceRepository.findById(WorkspaceIds.DEFAULT).isPresent()
  → encode password (BCrypt)
  → save AppUser (mustChangePassword=true)
  → save WorkspaceMembership (ADMIN, DEFAULT workspace)
  → INFO: bootstrap admin created for username=<name>
```

**Transaktion:** `@Transactional` auf Service-Methode; Count-Check und Insert in einer TX (Single-Instance v1).

### Passwort-Hashing

- Dependency: `org.springframework.security:spring-security-crypto` (Version via Boot BOM 3.5.16)
- Bean: `new BCryptPasswordEncoder()` (Default-Strength 10)
- **Nicht** `{noop}` — das war nur Test-Platzhalter in 11.1-ITs
- 11.3/11.4 verwenden denselben Encoder für Login — Bean wiederverwendbar halten

### Validierung (minimal, v1)

| Feld | Regel |
|---|---|
| Username | `trim()`; `length 1..100`; kein Insert bei blank |
| Password | `non-blank`; optional min. 8 Zeichen empfohlen — **wenn** implementiert, in Tests dokumentieren; PRD nennt keine Min-Länge → Ops-Verantwortung akzeptabel |

Username case-sensitiv (PostgreSQL UNIQUE) — aus 11.1 deferred; **kein** forced lowercase in 11.2.

### Logging-Regeln (T16)

- Erlaubt: `"Bootstrap admin created for username={}"`, `"Bootstrap skipped: users already exist"`, `"Bootstrap skipped: credentials not configured"`
- Verboten: Passwort, `password_hash`-Klartext, vollständige `BootstrapProperties`-Dumps
- `@ConfigurationProperties` ohne `@ToString` oder mit `@ToString(exclude = "adminPassword")`

### Optionale Health-Signal (Defer-freundlich)

Security-Addendum: Health darf „kein User + keine Bootstrap-Env“ signalisieren **ohne** Secrets.  
**Nicht AC-pflichtig** in 11.2 — kann als kleiner `HealthIndicator` folgen oder in 11.3/14. Wenn umgesetzt: Detail z. B. `bootstrapAdminPending=true`, **kein** Username/Password.

### Anti-Patterns (NICHT)

- Default-Passwort in Code/YAML/Flyway
- `spring-boot-starter-security` + offene APIs „nebenbei“
- Klartext in `password_hash`
- Bootstrap bei Count > 0
- Passwort in Logs/Exceptions
- Login-Controller „schon mal anlegen“
- JWT / localStorage-Vorbereitung
- Zweiten Admin bei jedem Start anlegen
- `{noop}`-Hash für Bootstrap-Admin

### Race / Concurrency

v1 **Single Backend Instance** (AD-12). Parallel-Start zweier JVMs: UNIQUE(`username`) + Count-Check minimieren Risiko; vollständige Distributed-Lock **out of scope**. Dokumentieren in Service-Kommentar.

### Erwartete Dateien

**Neu**
- `backend/src/main/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminRunner.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/config/BootstrapProperties.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/config/BootstrapConfig.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminIntegrationTest.java`

**UPDATE**
- `backend/pom.xml` — `spring-security-crypto`
- `backend/src/main/resources/application.yml` — `app.bootstrap`
- `backend/.env.example` — kommentierte Bootstrap-Vars
- `README.md` — kurzer Bootstrap-Hinweis

**Nicht anfassen**
- Flyway V1–V8 SQL
- Frontend
- Bestehende API-Controller
- AI-Services
- Story 10.1 Artefakte

## Testanforderungen

1. `@SpringBootTest` + `@ActiveProfiles("test")` — H2 wie bestehende ITs
2. `@DynamicPropertySource` setzt `BOOTSTRAP_ADMIN_USERNAME` / `BOOTSTRAP_ADMIN_PASSWORD` pro Testklasse oder nested class
3. `@Transactional` auf Tests **vorsichtig**: Bootstrap läuft beim Context-Start — Tests ggf. mit `@DirtiesContext` oder dedizierter Test-Config / `@TestPropertySource` + frischer Context pro Szenario
4. **Pattern-Hinweis:** Bootstrap beim ersten Context-Start — Testdesign:
   - Option A: `@SpringBootTest` mit Properties vor Start; `@Autowired` Repositories; assert nach `contextLoads`
   - Option B: `@Import(BootstrapAdminService.class)` + Unit-Test der Service-Methode **plus** schmaler IT für Runner
   - Mindestens **ein** Full-Context-IT wie oben
5. BCrypt: `passwordEncoder.matches(raw, saved.getPasswordHash())` muss true sein
6. Idempotenz: zweiter manueller Aufruf `bootstrapAdminService.runIfNeeded()` oder zweiter `@SpringBootTest`-Context mit gleicher DB (H2 in-memory reset pro Class ok)

## Definition of Done

- [x] AC 1–12 erfüllt
- [x] Kein Flyway-User-Seed; kein Klartext-Passwort in Repo
- [x] Nur `spring-security-crypto`, kein Security-Starter
- [x] `mvn test` grün
- [x] `.env.example` + README aktualisiert
- [x] Kein Login/Security-Filter/Angular

## Traceability

| Anforderung | Abdeckung in 11.2 |
|---|---|
| FR-22 | Initialer Admin persistiert |
| NFR-12 | BCrypt-Hash only |
| AD-12 | Env Bootstrap, Force-Change-Flag, kein Flyway-PW |
| AD-14 | Default-Workspace ADMIN membership |
| T16 | Kein Default-Passwort; Bootstrap-Tests |

## Previous Story Intelligence (11.1)

- V8 liefert Default-Workspace `c0000000-0000-4000-8000-000000000001` — Runner muss scheitern/fail-fast wenn fehlend (Flyway-Fehler wäre ohnehin vorher)
- `app_user`-Count nach Migration = **0** — idealer Bootstrap-Trigger
- 11.1-ITs nutzten `{noop}`-Hashes — **nicht** für Bootstrap übernehmen
- H2-Tests: kein PostgreSQL `DO $$` in SQL
- Constraint-Tests in `WorkspaceMembershipPersistenceIntegrationTest` — Pattern für DataIntegrity-Assertions wiederverwenden
- Review-Defer: Username-Validierung gehört in 11.2; case-sensitiv beibehalten
- `WorkspaceMembershipRepository.findByUserId` fehlt noch — für 11.2 reicht Count + `findByWorkspaceIdAndUserId`

## Git Intelligence

Letzte Commits fokussiert auf AI/Planning — kein Security-Code im Repo. 11.1-Implementierung lokal/uncommitted. Muster: Feature-Packages (`ai.config`, `infrastructure.persistence`), `@ConfigurationProperties`, Integrationstests unter `infrastructure.persistence` bzw. neu `security.bootstrap`.

## Latest Tech Information

- **Spring Boot 3.5.16** / **Java 21** — unverändert
- **BCrypt:** `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder` aus `spring-security-crypto` — leichtgewichtig, kein Web-Security-Stack
- Boot BOM managed Version — keine explizite Versionsnummer in `pom.xml` nötig
- `@ConfigurationProperties` + `@EnableConfigurationProperties` — identisch zu `AiConfig`/`AiProperties`

## Project Structure Notes

- Erstes Package unter `security/` — Spine-Vorgabe; Subpackages `bootstrap`, `config`
- Domain-Entities bleiben in `domain.model` — nicht duplizieren
- Kein `admin.*` Package bis Epic 13

## References

- `_bmad-output/planning-artifacts/stories/stories-security-multi-user.md` — Story 11.2
- `_bmad-output/planning-artifacts/epics.md` — Epic 11
- `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-security-multi-user/prd.md` — §3.4
- `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-security-multi-user/security-addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/ARCHITECTURE-SPINE.md`
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/security-decisions.md` — AD-12
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-security/threat-model.md` — T16
- `_bmad-output/implementation-artifacts/11-1-datenmodell-workspace-user-flyway.md` — Vorgänger
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/AppUser.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/ai/config/AiProperties.java`

## Dev Agent Record

### Agent Model Used

Cursor Composer

### Debug Log References

- Bootstrap-ITs: isolierte H2-DB pro Testklasse (`IsolatedH2Database`), damit geteiltes `jdbc:h2:mem:cgi_kpi_dashboard` nicht kollidiert.
- `application-test.yml`: leere Bootstrap-Properties überschreiben Entwickler-Env in Tests.

### Completion Notes List

- `BootstrapAdminService.bootstrapIfNeeded()` transaktional: User + ADMIN-Membership atomar
- Fehlender Default-Workspace → `DefaultWorkspaceMissingException`, kein Persist
- Erfolgs-Log ohne Passwort und ohne Username (T16)
- 148 Tests grün (`mvn test`); Review-Patches angewendet

### File List

- backend/pom.xml
- backend/src/main/resources/application.yml
- backend/src/test/resources/application-test.yml
- backend/.env.example
- README.md
- backend/src/main/java/com/cgi/kpi/dashboard/security/config/BootstrapProperties.java
- backend/src/main/java/com/cgi/kpi/dashboard/security/config/BootstrapConfig.java
- backend/src/main/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminService.java
- backend/src/main/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminRunner.java
- backend/src/main/java/com/cgi/kpi/dashboard/security/bootstrap/DefaultWorkspaceMissingException.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminNoCredentialsIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminServiceTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminLoggingTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminPreSeededUserIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminBlankUsernameIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/BootstrapAdminWhitespacePasswordIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/bootstrap/IsolatedH2Database.java
- backend/src/test/java/com/cgi/kpi/dashboard/security/config/BootstrapPropertiesTest.java
- _bmad-output/implementation-artifacts/11-2-bootstrap-administrator-env.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-21: Story 11.2 implementiert — Env-Bootstrap-Admin, BCrypt, Tests; Status → review
- 2026-07-21: Review-Patches — Mockito saveAndFlush, test env override, AC12-ITs; Status → done
