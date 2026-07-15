---
story_key: 3-7-mock-seed-erweiterung
epic: 3
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 3.7: Mock-Seed Erweiterung

Status: review

## Story

Als Demo-Nutzer  
möchte ich erweiterte Mock-Daten  
damit Tabellen, Insights und Berichtsvergleich demonstriert werden können.

## Acceptance Criteria

1. **Gegeben** Seed V4, **dann** haben Projekte Projektleitung, Probleme, erweiterte Risiko-Felder (soweit modelliert).
2. **Gegeben** Seed, **dann** existieren je Projekt 2 Berichtsstand-Snapshots (aktuell + vorherig) `[ASSUMPTION]`.
3. **Gegeben** Seed, **dann** mindestens je ein Projekt pro Management-Insight-Typ `[OFFEN: Regeln]`.

## Tasks / Subtasks

- [x] Task 1: Flyway V4 Seed-Erweiterung (AC: 1, 2)
- [x] Task 2: Seed-Daten für Projektleitung, Probleme, Snapshots (AC: 1, 2)
- [x] Task 3: Insight-Szenario-Klassifikator für Seed-Abdeckung (AC: 3)
- [x] Task 4: Integrationstests Seed-Erweiterung (AC: 1, 2, 3)
- [x] Task 5: Flyway-Tests auf V4 anheben
- [x] Task 6: Story-Dokumentation aktualisieren

## Dev Notes

- Baut auf V3-Schema (Story 3.6) auf — **keine** Schema-Änderung in V4.
- Risiko-Erweiterung (probability, impact, …) **nicht modelliert** — AC1 nur soweit Entity-Felder existieren.
- Management-Insight-Typen aus Brief-Addendum §5; Regeln `[OFFEN]` — Seed liefert charakteristische Projekte je Typ.
- Snapshots: vorheriger Stand ~30 Tage älter, leichte Deltas für FR-21-Demo.

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 32/32 grün (2026-07-15)

### Completion Notes List

- V4: UPDATE Projektleitung/`last_data_update`/`predicted_end_date` für 20 Projekte; 8 Probleme; 40 Snapshots (2/Projekt).
- `MockSeedInsightClassifier` + `MockManagementInsightType` für AC3-Abdeckung (Heuristik, Regeln OFFEN).
- `MockSeedExtendedIntegrationTest` — Stammdaten, Probleme, Snapshots, Insight-Szenarien.
- Flyway-Tests auf 4 Migrationen angehoben.

### File List

- backend/src/main/resources/db/migration/V4__mock_seed_extension.sql
- backend/scripts/gen_v4_seed.py
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/MockSeedExtendedIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/seed/MockManagementInsightType.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/seed/MockSeedInsightClassifier.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayMigrationIntegrationTest.java
- _bmad-output/implementation-artifacts/3-7-mock-seed-erweiterung.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 3.7 — Mock-Seed Erweiterung implementiert
