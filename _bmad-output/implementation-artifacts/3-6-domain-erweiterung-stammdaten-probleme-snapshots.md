---
story_key: 3-6-domain-erweiterung-stammdaten-probleme-snapshots
epic: 3
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 3.6: Domain-Erweiterung Stammdaten, Probleme, Snapshots

Status: done

## Review Findings (2026-07-15 — kompakt)

- **ACs:** alle erfüllt — V3-Schema, FR-6-Problem getrennt, additive Migration.
- **Flyway V3:** nullable Spalten, FK CASCADE, Indizes — H2-kompatibel (einzelne `ALTER TABLE`).
- **JPA:** Mapping konsistent; `orphanRemoval` wie bestehende Kinder-Entitäten.
- **Tests:** 28/28 grün; Seed-Erhalt (`>=20`) abgedeckt.
- **3.7-Blocker:** keine — Risiko-Erweiterung `[OFFEN]`, bewusst aus Scope 3.6.

## Story

Als System  
möchte ich erweiterte Projekt-Stammdaten und getrennte Probleme  
damit Management-Analyse möglich ist (FR-5, FR-6, FR-21).

## Acceptance Criteria

1. **Gegeben** Flyway V3, **dann** existieren Felder/Entitäten: `project_lead`, `last_data_update`, `predicted_end_date` `[OFFEN]`, `problems`, `project_report_snapshots` `[ASSUMPTION]`.
2. **Gegeben** `problems`, **dann** getrennt von `risks` mit Mindestfeldern laut FR-6.
3. **Gegeben** bestehende 3.1-Tabellen, **dann** bleiben additive Migration — kein Datenverlust.

## Tasks / Subtasks

- [x] Task 1: Flyway V3 additive Schema-Migration (AC: 1, 3)
- [x] Task 2: `Project`-Entität um Stammdaten-Felder erweitern (AC: 1)
- [x] Task 3: `Problem`-Entität und Repository (AC: 1, 2)
- [x] Task 4: `ProjectReportSnapshot`-Entität und Repository (AC: 1)
- [x] Task 5: Flyway- und Repository-Integrationstests (AC: 1, 2, 3)
- [x] Task 6: Story-Dokumentation aktualisieren

## Dev Notes

- **FR-5:** `project_lead`, `last_data_update`, `predicted_end_date` (nullable — Berechnung `[OFFEN]`).
- **FR-6 Problem-Mindestfelder:** Titel, Beschreibung, Schweregrad, Status, Verantwortlichkeit, Zieltermin, Gegenmaßnahme — eigene Tabelle `problems`, getrennt von `risks`.
- **FR-21 Snapshot `[ASSUMPTION]`:** Kennfelder pro Berichtsstand — Fortschritt, Budget-Ist, Terminabweichung, Status, offene Risiken, Snapshot-Datum. Seed in Story 3.7.
- **AD-3 / Spine:** Domain in `domain.model`, Repositories in `infrastructure.persistence`.
- V3 nur Schema — **kein Seed-Update** (Story 3.7).
- Neue Spalten nullable für bestehende V2-Daten (additive Migration).

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 28/28 grün (2026-07-15)
- Branch: `story/3.5-basis-rest` (uncommitted, enthält auch 3.5-Arbeit)

### Completion Notes List

- Flyway V3: additive Spalten auf `projects`, Tabellen `problems` und `project_report_snapshots`.
- Domain: `Problem`, `ProjectReportSnapshot`; `Project` um Stammdaten-Felder und Beziehungen erweitert.
- Repositories: `ProblemRepository`, `ProjectReportSnapshotRepository` (7 JPA-Repos gesamt).
- Tests: `DomainExtensionIntegrationTest` (Stammdaten, Problem≠Risk, Snapshots, Seed-Erhalt); Flyway-Tests auf V3 angehoben.

### File List

- backend/src/main/resources/db/migration/V3__extend_domain_schema.sql
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Project.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/Problem.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/model/ProjectReportSnapshot.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/ProblemRepository.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/ProjectReportSnapshotRepository.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/DomainExtensionIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayMigrationIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayDomainSchemaIntegrationTest.java
- _bmad-output/implementation-artifacts/3-6-domain-erweiterung-stammdaten-probleme-snapshots.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 3.6 — Domain-Erweiterung Stammdaten, Probleme, Snapshots implementiert
