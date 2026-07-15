---
story_key: 3-1-domain-modell-und-jpa
epic: 3
baseline_commit: NO_VCS
---

# Story 3.1: Domain-Modell und JPA

Status: done

## Story

Als System  
möchte ich persistierte Projekt-Entitäten  
damit KPIs berechnet werden können.

## Acceptance Criteria

1. **Gegeben** Flyway Start, **wenn** Migrationen laufen, **dann** existieren Tabellen für Projekte, Phasen, Meilensteine, Risiken, Budget/Aufwand.
2. **Gegeben** Entitäten, **dann** nutzen PKs UUID.

## Tasks / Subtasks

- [x] Task 1: JPA, PostgreSQL, Flyway Dependencies (AC: 1)
- [x] Task 2: Domain-Entitäten mit UUID-PKs (AC: 2)
- [x] Task 3: Flyway V1 Schema-Migration (AC: 1)
- [x] Task 4: Spring Data JPA Repositories in infrastructure (AC: 1)
- [x] Task 5: Repository-Integrationstests + Test-Profil (AC: 1, 2)
- [x] Task 6: Story-Dokumentation aktualisieren

### Review Findings

- [x] [Review][Defer] Status/Severity als String statt Enum — Domain-Enums in Story 3.3/Seed oder KPI-Stories
- [x] [Review][Defer] Keine `domain.repository`-Interfaces — Spring Data direkt in `infrastructure.persistence`; Adapter bei Bedarf in 3.4+
- [x] [Review][Defer] Tests nutzen H2, nicht PostgreSQL — lokale PG-Verifikation durch Nutzer bestätigt; optional Testcontainers später
- [x] [Review][Dismiss] Default-Username `postgres` in YAML — korrekt via `SPRING_DATASOURCE_*` überschreibbar (`cgi_kpi_app` verifiziert)

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved — alle ACs erfüllt  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)

### Prüfmatrix

| Prüffokus | Ergebnis | Evidenz |
|---|---|---|
| Domain-Modell & Beziehungen | **Bestanden** | `Project` Aggregate: `@OneToMany` Phases/Milestones/Risks, `@OneToOne` Budget; bidirektionale Helper |
| UUID `@PrePersist` | **Bestanden** | `UuidEntity.ensureId()` — Integrationstest prüft UUID auf allen Entitäten |
| Flyway V1 Schema | **Bestanden** | 5 Tabellen, FK CASCADE, Indizes auf Kind-Tabellen |
| JPA ↔ SQL Konsistenz | **Bestanden** | Spaltennamen, Typen, Nullable, Längen stimmen überein |
| `ddl-auto: validate` | **Bestanden** | `application.yml` + `application-test.yml` |
| Spring Data Repositories | **Bestanden** | 5 Repositories in `infrastructure.persistence` |
| H2 Testprofil | **Bestanden** | `MODE=PostgreSQL`, Flyway enabled |
| Projekt-Graph-Test | **Bestanden** | `DomainRepositoryIntegrationTest` |
| Flyway-Tabellen-Check | **Bestanden** | `FlywayDomainSchemaIntegrationTest` |
| Env-Vars / Secrets | **Bestanden** | `${SPRING_DATASOURCE_*}` ohne Hardcoded-Passwörter; `.env.example` kommentiert |
| Lokale PG-Verifikation | **Bestanden** | Nutzer: `cgi_kpi_dashboard`, `cgi_kpi_app`, Migration + Tabellen OK |
| Automatisierte Tests | **Bestanden** | `.\mvnw.cmd test` — 8/8 grün |

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 Flyway-Tabellen | **Erfüllt** | V1 + FlywayDomainSchemaIntegrationTest + lokale PG-Verifikation |
| AC2 UUID-PKs | **Erfüllt** | UuidEntity + Repository-Integrationstest |

## Dev Notes

- Entitäten: `Project`, `ProjectPhase`, `Milestone`, `Risk`, `ProjectBudget` in `domain.model`.
- Repositories in `infrastructure.persistence`.
- Tests: H2 (PostgreSQL-Mode) + Flyway; Produktion: PostgreSQL.
- **Nicht enthalten:** Seed-Daten (Story 3.3), KPI-Logik.

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 8/8 grün (Review 2026-07-14)
- Lokale Verifikation Nutzer: PostgreSQL + Flyway + Backend-Start erfolgreich

### Completion Notes List

- JPA + Flyway + PostgreSQL Driver konfiguriert.
- Flyway `V1__create_domain_schema.sql` mit 5 Tabellen.
- UUID-PKs via `UuidEntity` + `@PrePersist`.
- Repository-Integrationstest speichert vollständigen Projekt-Graphen.
- Flyway-Schema-Test prüft Tabellenexistenz.
- Code Review 2026-07-14: Approved, Story auf done gesetzt.

### File List

- backend/pom.xml
- backend/src/main/resources/application.yml
- backend/src/main/resources/db/migration/V1__create_domain_schema.sql
- backend/src/test/resources/application-test.yml
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/UuidEntity.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Project.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/ProjectPhase.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Milestone.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Risk.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/ProjectBudget.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/config/JpaConfig.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/ProjectRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/ProjectPhaseRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/MilestoneRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/RiskRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/ProjectBudgetRepository.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/DomainRepositoryIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayDomainSchemaIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/CgiKpiDashboardApplicationTests.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandlerIntegrationTest.java

### Change Log

- 2026-07-14: Story 3.1 — Domain-Modell und JPA implementiert
- 2026-07-14: BMAD Code Review — Approved
