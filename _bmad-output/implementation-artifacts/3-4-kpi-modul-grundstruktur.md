---
story_key: 3-4-kpi-modul-grundstruktur
epic: 3
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 3.4: kpi.* Modul Grundstruktur

Status: done

## Review Findings (2026-07-15)

### Decision-needed
_(keine)_

### Patch
- [x] [Review][Patch] Contract-Test prüft `ApprovedProjectDataReader`-Rückgabetyp nicht auf DTO statt Domain [`PortfolioKpiServiceContractTest.java`]
- [x] [Review][Patch] Integrationstest prüft nur `activeProjectCount`; Methodenname suggeriert vollständige KPI-Berechnung [`PortfolioKpiServiceIntegrationTest.java:21-25`]
- [x] [Review][Patch] Contract-Test importiert `domain.model.Project` — unnötige Kopplung im KPI-Modultest [`PortfolioKpiServiceContractTest.java:11`]

### Defer
- [x] [Review][Defer] `PortfolioKpiSummaryDto` ohne Ampelverteilung (FR-1) — bewusst Epic 4 [`PortfolioKpiSummaryDto.java`] — deferred, Grundstruktur-Story
- [x] [Review][Defer] Kein `StubApprovedProjectDataReader`-Bean — Dev Notes: Implementierung folgt in späteren Stories [`ApprovedProjectDataReader.java`] — deferred, AC3 erfüllt (Interface)
- [x] [Review][Defer] KPI-Werte im Infrastructure-Stub statt `kpi.*`-Berechnung — Epic 4 geplant [`StubPortfolioKpiReader.java`] — deferred, Platzhalter akzeptiert
- [x] [Review][Defer] ArchUnit prüft nur `kpi ↛ ai`, nicht erweiterte AD-2-Grenzen — Härtung in späterer Story [`KpiModuleArchitectureTest.java`] — deferred, AC1 erfüllt
- [x] [Review][Defer] `ApprovedProjectDataDto` unvollständig vs. Story 9.1/FR-5 — Erweiterung in Epic 6/9 [`ApprovedProjectDataDto.java`] — deferred, Grundstruktur

## Story

Als Architekt  
möchte ich ein isoliertes KPI-Modul  
damit Berechnungen zentral sind (AD-3).

## Acceptance Criteria

1. **Gegeben** `kpi.*`, **dann** importiert es nicht `ai.*`.
2. **Gegeben** KPI-Service, **dann** liefert er berechnete DTOs, keine Rohentitäten an API.
3. **Gegeben** `kpi/reader/`, **dann** existieren Interface-Stubs `PortfolioKpiReader`, `ApprovedProjectDataReader`.

## Tasks / Subtasks

- [x] Task 1: KPI-DTOs anlegen (AC: 2)
- [x] Task 2: Reader-Interface-Stubs in `kpi.reader` (AC: 3)
- [x] Task 3: KPI-Service mit DTO-Rückgabe (AC: 2)
- [x] Task 4: ArchUnit Package-Import-Test kpi ↛ ai (AC: 1)
- [x] Task 5: Service-Rückgabe-Test (keine Domain-Entitäten) (AC: 2)
- [x] Task 6: Story-Dokumentation aktualisieren

## Dev Notes

- **AD-2, AD-3:** Jede KPI-Zahl entsteht in `kpi.*`; `ai.*` liest nur über Reader-Interfaces.
- Reader-Implementierungen folgen in späteren Stories (Infrastructure-Adapter).
- Vollständige KPI-Berechnung folgt in Epic 4 — diese Story legt nur Modul-Grundstruktur.
- Base-Paket: `com.cgi.kpi.dashboard.kpi` mit Unterpaketen `dto`, `reader`, `service`.

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 17/17 grün (2026-07-15)
- Branch: `story/3.4-kpi-module`

### Completion Notes List

- KPI-DTOs: `PortfolioKpiSummaryDto`, `ApprovedProjectDataDto` (Records, kein JPA).
- Reader-Stubs: `PortfolioKpiReader`, `ApprovedProjectDataReader` in `kpi.reader`.
- Service: `PortfolioKpiService` + `DefaultPortfolioKpiService` — Rückgabe nur DTOs.
- Stub-Adapter: `StubPortfolioKpiReader` in `infrastructure.kpi` (Platzhalter bis Epic 4).
- ArchUnit: `KpiModuleArchitectureTest` — `kpi.*` importiert nicht `ai.*`.
- Contract-Test: `PortfolioKpiServiceContractTest` — Interface-Stubs + DTO-Rückgabe beider Reader/Service.
- Code-Review 2026-07-15: 3 Patch-Findings behoben (Contract-Test ApprovedProjectDataReader, Integrationstest Stub-Felder, Domain-Import entfernt).

### File List

- backend/pom.xml
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioKpiSummaryDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/ApprovedProjectDataDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/reader/PortfolioKpiReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/reader/ApprovedProjectDataReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiService.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/DefaultPortfolioKpiService.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/kpi/StubPortfolioKpiReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/kpi/package-info.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/KpiModuleArchitectureTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/PortfolioKpiServiceContractTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiServiceIntegrationTest.java
- _bmad-output/implementation-artifacts/3-4-kpi-modul-grundstruktur.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 3.4 — kpi.* Modul Grundstruktur implementiert
- 2026-07-15: Code-Review abgeschlossen — 3 Patches angewendet, Status done
