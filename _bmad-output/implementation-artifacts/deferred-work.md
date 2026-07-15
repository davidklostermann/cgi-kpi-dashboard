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
