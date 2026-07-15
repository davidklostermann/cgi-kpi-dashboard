---
story_key: 3-2-flyway-migrationen
epic: 3
baseline_commit: NO_VCS
---

# Story 3.2: Flyway-Migrationen

Status: done

## Story

Als Entwickler  
möchte ich versionierte DB-Schema-Migrationen  
damit das Schema reproduzierbar ist.

## Acceptance Criteria

1. **Gegeben** leere DB, **wenn** App startet, **dann** wendet Flyway alle Migrationen an.
2. **Gegeben** erneuter Start, **dann** sind Migrationen idempotent (kein erneutes Anlegen).

## Tasks / Subtasks

- [x] Task 1: Flyway-Konfiguration härten (validate-on-migrate, clean-disabled)
- [x] Task 2: Flyway Migrate Test — leere DB, alle Migrationen angewendet (AC: 1)
- [x] Task 3: Flyway Migrate Test — zweiter Lauf idempotent (AC: 2)
- [x] Task 4: README Flyway-Abschnitt
- [x] Task 5: Story-Dokumentation aktualisieren

### Review Findings

- [x] [Review][Defer] `EXPECTED_MIGRATION_COUNT = 1` hardcoded — bei Story 3.3 (V2 Seed) Test anpassen
- [x] [Review][Defer] Idempotenz-Test via `flyway.migrate()`, nicht via zweiter Spring-Context-Neustart — semantisch äquivalent, ausreichend für AC2
- [x] [Review][Dismiss] Overlap mit `FlywayDomainSchemaIntegrationTest` (3.1) — komplementär: Tabellen vs. Migrate-Verhalten

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved — alle ACs erfüllt  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)

### Prüfmatrix

| Prüffokus | Ergebnis | Evidenz |
|---|---|---|
| Leere DB → alle Migrationen | **Bestanden** | App-Start loggt V1-Migration; `appliesAllMigrationsOnEmptyDatabase` |
| Idempotenter Neustart | **Bestanden** | `secondMigrateIsIdempotent` → 0 executions + validate OK |
| `validate-on-migrate` | **Bestanden** | `application.yml` + `application-test.yml` |
| `clean-disabled` | **Bestanden** | Schutz vor destruktivem `flyway clean` |
| Flyway Migrate Test | **Bestanden** | `FlywayMigrationIntegrationTest` (2 Tests) |
| README-Dokumentation | **Bestanden** | Flyway-Abschnitt + `flyway_schema_history` SQL |
| Keine Secrets | **Bestanden** | Keine Änderungen an Credentials |
| Automatisierte Tests | **Bestanden** | `.\mvnw.cmd test` — 10/10 grün |

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 Leere DB, alle Migrationen | **Erfüllt** | Spring Boot Startup + Flyway V1 SUCCESS |
| AC2 Erneuter Start idempotent | **Erfüllt** | Zweiter `migrate()` → 0 neue Migrationen |

## Dev Notes

- Schema V1 aus Story 3.1 — diese Story sichert Flyway-Betrieb und Tests.
- Seed-Daten folgen in Story 3.3 (separate Migration V2).

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 10/10 grün (Review 2026-07-14)

### Completion Notes List

- Flyway: `validate-on-migrate: true`, `clean-disabled: true` (main + test).
- `FlywayMigrationIntegrationTest`: AC1 (V1 applied on empty DB), AC2 (second migrate → 0 executions).
- README: Flyway-Abschnitt mit `flyway_schema_history`-Prüfung.
- Code Review 2026-07-14: Approved, Story auf done gesetzt.

### File List

- backend/src/main/resources/application.yml
- backend/src/test/resources/application-test.yml
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayMigrationIntegrationTest.java
- README.md

### Change Log

- 2026-07-14: Story 3.2 — Flyway-Migrationen implementiert
- 2026-07-14: BMAD Code Review — Approved
