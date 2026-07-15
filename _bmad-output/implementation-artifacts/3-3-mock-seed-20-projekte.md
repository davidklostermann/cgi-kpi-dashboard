---
story_key: 3-3-mock-seed-20-projekte
epic: 3
baseline_commit: NO_VCS
---

# Story 3.3: Mock-Seed ~20 Projekte (FR-19)

Status: done

## Story

Als Demo-Nutzer  
möchte ich ein realistisches Mock-Portfolio  
damit der Pilot Szenarien zeigt.

## Acceptance Criteria

1. **Gegeben** Seed ausgeführt, **dann** existieren ca. 20 Projekte.
2. **Gegeben** Szenario-Typen, **dann** ist mindestens je ein Projekt: im Plan, Terminverzug, Budgetüberzug, offene Risiken, widersprüchliche Signale, abgeschlossen.
3. **Gegeben** Seed, **dann** enthält er keine echten Kundennamen.

## Tasks / Subtasks

- [x] Task 1: Flyway V2 Seed-Migration mit festen UUIDs (AC: 1, 3)
- [x] Task 2: Szenario-Abdeckung in Seed-Daten (AC: 2)
- [x] Task 3: Integrationstest Zählung + Szenario-Abdeckung (AC: 1, 2, 3)
- [x] Task 4: FlywayMigrationIntegrationTest auf 2 Migrationen anpassen
- [x] Task 5: DomainRepositoryIntegrationTest für Seed-Baseline anpassen
- [x] Task 6: Story-Dokumentation aktualisieren

### Review Findings

- [x] [Review][Defer] `MockSeedScenarioClassifier` nur in Tests — KPI-Modul (3.4+) braucht eigene Ampel-/Szenario-Logik; Classifier dient Seed-Validierung
- [x] [Review][Defer] Seed per Python-Script regenerierbar — Drift-Risiko wenn nur SQL manuell editiert wird; Script als Quelle dokumentiert
- [x] [Review][Dismiss] Projekt-UUIDs enden hexadezimal (`…014` = Projekt 20) — korrekt, nur ungewohnt
- [x] [Review][Dismiss] Toleranz `±2` und exakte `assertEquals(20)` redundant — harmlos, beide Assertions bleiben

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved — alle ACs erfüllt  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)  
**Manuelle Verifikation:** Nutzer — PostgreSQL, 20 Projekte sichtbar

### Prüfmatrix

| Prüffokus | Ergebnis | Evidenz |
|---|---|---|
| ~20 Projekte nach Seed | **Bestanden** | V2 INSERT (20 Zeilen); `MockSeedIntegrationTest`; Nutzer-DB |
| 6 Szenario-Typen abgedeckt | **Bestanden** | Seed-Daten + `seedCoversAllPilotScenarios` |
| Keine echten Kundennamen | **Bestanden** | Whitelist + Denylist in Test; fiktive Namen im SQL |
| Reproduzierbare UUIDs | **Bestanden** | Feste UUIDs in V2; ARCHITECTURE-SPINE-konform |
| Flyway V2 idempotent (Migrate) | **Bestanden** | `FlywayMigrationIntegrationTest` (2 Migrationen) |
| Domain-Tests mit Seed-Baseline | **Bestanden** | Relative Zählung in `DomainRepositoryIntegrationTest` |
| PostgreSQL-Laufzeit | **Bestanden** | Nutzer-Verifikation |
| Automatisierte Tests | **Bestanden** | `.\mvnw.cmd test` — 13/13 grün |

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 ca. 20 Projekte | **Erfüllt** | Exakt 20; Nutzer bestätigt |
| AC2 Szenario-Abdeckung | **Erfüllt** | Classifier + Integrationstest |
| AC3 Fiktive Kundennamen | **Erfüllt** | Seed + Test |

## Dev Notes

- Reproduzierbarer Seed via Flyway V2; gleiche UUIDs über Umgebungen (ARCHITECTURE-SPINE).
- Szenario-Erkennung im Test über Datenmerkmale (kein zusätzliches Schema-Feld).
- Fiktive Kundennamen (Acme, Beta Systems, …) — keine echten Unternehmensnamen.

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 13/13 grün (2026-07-14)

### Completion Notes List

- Flyway `V2__mock_seed.sql`: 20 Projekte mit Phasen, Meilensteinen, Risiken und Budget/Aufwand; feste UUIDs.
- Szenario-Vertreter: im Plan (p001), Terminverzug (p002/p003), Budgetüberzug (p004/p005), offene Risiken (p006/p007), widersprüchliche Signale (p008), abgeschlossen (p009).
- `MockSeedIntegrationTest`: Zählung (~20), Szenario-Abdeckung via `MockSeedScenarioClassifier`, fiktive Kundennamen.
- `FlywayMigrationIntegrationTest`: `EXPECTED_MIGRATION_COUNT = 2`.
- Optional: `backend/scripts/generate_mock_seed.py` zum Regenerieren des SQL aus Datendefinition.

### File List

- backend/src/main/resources/db/migration/V2__mock_seed.sql
- backend/scripts/generate_mock_seed.py
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/MockSeedIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/seed/MockPilotScenario.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/seed/MockSeedScenarioClassifier.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/FlywayMigrationIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/infrastructure/persistence/DomainRepositoryIntegrationTest.java
- README.md
- _bmad-output/implementation-artifacts/deferred-work.md

### Change Log

- 2026-07-14: Story 3.3 — Mock-Seed ~20 Projekte (FR-19) implementiert
- 2026-07-14: BMAD Code Review — Approved; Nutzer PostgreSQL-Verifikation
