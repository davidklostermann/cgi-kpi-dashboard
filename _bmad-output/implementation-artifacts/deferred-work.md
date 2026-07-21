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

## Deferred from: security multi-user planning (2026-07-21)

- Implementierung Epics 11–14 — Planungsartefakte + P0 geschlossen; kein Anwendungscode in diesem Schritt.
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
