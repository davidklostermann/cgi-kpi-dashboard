# Deferred Work

## Deferred from: code review of 1-1-backend-grundgeruest-spring-boot (2026-07-14)

- ~~Kein Maven Wrapper (`mvnw`)~~ — behoben in Story 1.3.
- `groupId` `com.cgi.kpi` vs. Java-Base-Package `com.cgi.kpi.dashboard` — Naming-Inkonsistenz, kein Laufzeit-Risiko.
- Mockito dynamic agent loading Warnung in Surefire-Output — bekanntes JDK-21-Verhalten; optional später via Surefire-Agent-Konfiguration.

## Deferred from: code review of 1-2-angular-grundstruktur-feature-basiert (2026-07-14)

- ~~`api.config.ts` mit hardcoded `/api`~~ — behoben in Story 1.3 (`environment.apiBaseUrl`).
- ~~`PortfolioApiService.getHealthProbe()` Actuator-Pfad~~ — behoben in Story 1.3 (`getAtRoot` + `/actuator`-Proxy).
- Optionaler Unit-Test für `AiPanelPlaceholderComponent` — kein AC-Blocker.

## Deferred from: code review of 3-1-domain-modell-und-jpa (2026-07-14)

- Status/Severity als String statt typisierte Enums — Domain-Enums bei Seed/KPI-Stories.
- Keine `domain.repository`-Interfaces — Spring Data direkt in `infrastructure.persistence`; Adapter optional in 3.4+.
- CI-Tests auf H2 statt PostgreSQL — lokale PG-Verifikation OK; optional Testcontainers später.

## Deferred from: code review of 3-2-flyway-migrationen (2026-07-14)

- ~~`EXPECTED_MIGRATION_COUNT = 1` in FlywayMigrationIntegrationTest~~ — behoben in Story 3.3 (V2 Seed).
- Idempotenz-Test prüft `flyway.migrate()` statt vollständigen App-Neustart — semantisch ausreichend.

## Deferred from: code review of 9-project-ai-panel (2026-07-20, Chunk Frontend Projekt-Detail)

- Kein eigener Forecast-Bereich — Produktstand deckt Managementbewertung+Q&A ab; Story 9.2/9.4 Intent offen.
- Maßnahmen nur `evidenceFactIds`, kein Klartext-Evidence-Feld am Action-DTO — API-Erweiterung nötig.
- Gantt-Phasensegmente nur Plantermine — Ist/Prognose auf Segmentebene als Follow-up zu Story 6.4.
- Meilenstein-Blockaden `[OFFEN]` in Spec — Spec-OFFEN.

## Deferred from: code review of 8-portfolio-ki (2026-07-20, Chunk Backend AI)

- `extractJsonPayload` Brace-Counting ohne String-Kontext — JSON mit `{`/`}` in Werten kann fehlschneiden.
- Fehlende Unit-Tests für `JpaPortfolioReportTrendReader` — Coverage-Follow-up.

## Deferred from: code review of 11-3-spring-security-grundkonfiguration (2026-07-21)

- `lockedUntil` / Account-Lockout nicht in `DashboardUserDetailsService`/`DashboardUserDetails` — Story 11.4
- `mustChangePassword`-Enforcement fehlt — Story 11.4
- Session bleibt nach User-Deaktivierung gültig bis Expiry — Story 11.6
- Rollen-Enforcement auf URL-Ebene (`hasRole`) — Story 12.2 (Out of Scope 11.3)

## Deferred from: code review of 11-4-login-logout-me (2026-07-21)

- `lockedUntil` / Brute-Force-Lockout — Story 14.2
- Passwortwechsel invalidiert andere parallele Sessions nicht — Story 11.6
- Stale `mustChangePassword` in Session nach Admin-DB-Update — Epic 13
- CSRF-Negativtests für Auth-Endpoints — Story 11.6

## Deferred from: security multi-user planning (2026-07-21)
- Produktiv Secret Store / KMS Vendor — OFFEN, **Blocker vor Produktion**.
- CGI-SSO Timing — OFFEN; Architektur vorbereitet.
- Project-AI Cache ohne Tenant — Epic 12.3 (isolieren oder disable).
- Epic 10 + 11–14 in `sprint-status.yaml` als `backlog` nachgetragen (2026-07-21).
- Story 11.1 aufgeteilt: Bootstrap → 11.2; Security → 11.3; Login → 11.4; Angular → 11.5; Tests → 11.6.

## Deferred from: code review of 8-portfolio-ki + 9-project-ai-panel (2026-07-17)

- Portfolio-Fließtext ohne Evidence-Validierung — narrative KI-Zusammenfassung bewusst ohne Fact-Bindung im MVP.
- Kein Rate-Limiting/Cache für Portfolio-Trendanalyse — Kostenrisiko, nicht AC-blockierend.
- Q&A-Antworttext nicht vollständig validiert — Evidence-IDs werden gefiltert, Freitext bleibt Modell-output.
- Prompt-Injection-Härtung über Längenlimit hinaus — vollständige Sanitisierung als Security-Follow-up.


- `PortfolioKpiSummaryDto` ohne Ampelverteilung (FR-1) — Epic 4 geplant.
- Kein `StubApprovedProjectDataReader`-Bean — Implementierung in späteren Stories (AC3: Interface reicht).
- KPI-Werte im Infrastructure-Stub statt `kpi.*`-Berechnung — Epic 4 geplant.
- ArchUnit nur `kpi ↛ ai`, erweiterte AD-2-Grenzen später — AC1 erfüllt.
- `ApprovedProjectDataDto` unvollständig vs. Story 9.1/FR-5 — Erweiterung Epic 6/9.

## Deferred from: code review of 3-5-basis-rest-projekte-und-portfolio (2026-07-15)

- `findAll()` + In-Memory-Sortierung ohne Pagination — MVP-Scope (~20 Mock-Projekte).
- AC3 KPI-Endpunkte ohne `aiGenerated`-Test — KPI-REST folgt in Epic 4.
- `status` als String statt Enum — bereits Story 3.1 deferred.
- Kein `progress_percent`-Range-Check im Mapper — Seed-Daten kontrolliert.
- `ProjectMapper` ohne Unit-Tests — Integrationstest deckt Happy-Path ab.
- Application-Schicht liefert API-DTOs direkt (`ProjectQueryService` → `api.projects.dto.*`) — MVP-Muster beibehalten; Refactor optional später.

## Deferred from: code review of 11-1-datenmodell-workspace-user-flyway (2026-07-21)

- Username case-sensitiv (PostgreSQL UNIQUE ohne Normalisierung) — Entscheidung/Normalisierung in Story 11.2 Bootstrap/Login.
- Leerer Username / leerer `password_hash` schema-konform — Validierung in 11.2, nicht DB-CHECK in 11.1.
- `failed_login_count` ohne CHECK `>= 0` — Lockout-Logik Story 11.3.
- Nur H2-ITs, kein PostgreSQL/Testcontainers — Projekt-Pattern seit Epic 3.
- Isolierter V7→V8-Upgrade-Test fehlt — Endzustand via FlywayMigrationIntegrationTest abgedeckt.
- `WorkspaceMembershipRepository.findByUserId` fehlt — Story 11.3 UserDetails.
- Unit-Tests bauen `Project` ohne `workspaceId` (In-Memory) — kein Persist-Pfad betroffen.

## Deferred from: code review of 11-2-bootstrap-administrator-env (2026-07-21)

- TOCTOU-Race bei parallelem JVM-Start — v1 Single-Instance; UNIQUE(username) als Stop.
- Bootstrap blockiert bei jedem existierenden `app_user` — count()-Trigger by design.
- Default-Workspace fehlt → App-Start bricht ab — gewolltes Fail-Fast.
- Kein Rollback-IT User+Membership-Partial-Failure — TX vor Inserts ausreichend v1.
- Passwort bleibt in BootstrapProperties-Heap — kein Wipe vorgesehen.
- Logging-Test nur Erfolgspfad — Skip-Pfade nicht auf Secret-Leak geprüft.

## Deferred from: code review of 12-2-endpoint-policies-ki-admin (2026-07-22)

- Method Security (`@EnableMethodSecurity` / `@PreAuthorize`) — Story-12.2-DoD URL+Service akzeptiert; AD-13 erwähnt Method Security als dritte Schicht; Follow-up Epic 14 oder bei neuen KI-Controllern.

## Deferred from: code review of 12-1-workspace-scope-private-settings (2026-07-22)

- Kind-Repos laden global per `findAll().filter(workspaceProjectIds)` — Performance-Follow-up `findByProject_IdIn` laut Story-Dev-Notes.
- Race bei erstem Preferences-PUT kann unhandled `DataIntegrityViolationException` → 500 werfen.
- Korruptes `preferences_json` wird beim GET still zu leerem Objekt normalisiert.
- Doppelter `riskRepository.findAll()`-Scan in `JpaPortfolioTableReader.readTable()`.
