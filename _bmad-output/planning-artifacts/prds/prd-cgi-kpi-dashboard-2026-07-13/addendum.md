---
title: "PRD Addendum: cgi-kpi-dashboard"
status: final
created: 2026-07-13
updated: 2026-07-15
parent: prd.md
---

# PRD Addendum

Technische und fachliche Vertiefung, die nicht in die PRD-Hauptnarrative gehört. Verbindliche Produktanforderungen stehen in [prd.md](prd.md).

## Technische Grundrichtung (aus Brief, für Architecture)

| Bereich | Technologie |
|---------|-------------|
| Backend | Java 21, Spring Boot 3.5.x, Maven, REST, Spring Data JPA, PostgreSQL, Flyway, Jakarta Bean Validation, Actuator |
| Tests | JUnit, Spring Boot Test, ArchUnit |
| Frontend | Angular 20, TypeScript, SCSS, Angular Material, Angular CDK, CGI Experience Design System 19.0.0, HttpClient, RxJS, Angular Signals |
| Struktur | Monorepo; KPI-Berechnung im Backend |
| KI | Gemini API ausschließlich serverseitig; API-Key nicht im Frontend/Git |

## KPI-Inventar

Detaillierte Metrik-Liste mit `[OFFEN]`-Formeln: [Brief-Addendum](../briefs/brief-cgi-kpi-dashboard-2026-07-13/addendum.md) — inkl. **Management Insights** (§5) und **Berichtsstandsvergleich** (§6).

PRD-FR-9 verlangt Abdeckung aller KPI-Bereiche; fachliche Definitionen werden im Pilot oder vor Architecture-Sign-off geklärt.

## Domain-Modell — Erweiterung (Planungsstand 2026-07-15)

Bestehend (Stories 3.1–3.3): `Project`, `ProjectPhase`, `Milestone`, `Risk`, `ProjectBudget`.

**Geplant (additive Migrationen, ab Story 3.6+):**

| Entität / Feld | Zweck | MVP |
|---|---|---|
| `Project.projectLead` | Projektleitung in Tabelle/Detail | Mock-Name |
| `Project.businessUnit` | Geschäftsbereich (optional) | `[OFFEN]` — ggf. = Kunde |
| `Project.lastDataUpdate` | Letzte Aktualisierung | Timestamp |
| `Project.predictedEndDate` | Deterministische Terminprognose | `[OFFEN]` Berechnung |
| `Problem` (neu) | Getrennt von Risiko | Pflichtfelder FR-6 |
| `Risk` Erweiterung | probability, impact, responsible, dueDate | Felder optional bis fachlich geklärt |
| `ProjectPhase` / `Milestone` Erweiterung | Status, Ist/Prognose-Termine, Abweichung, Blockade | `[OFFEN]` |
| `ProjectReportSnapshot` (neu) | Berichtsstand aktuell/vorherig | `[ASSUMPTION]` 2 Snapshots/Projekt im Seed |

**Kein Scope:** Aufgaben, Ressourcenplanung, vollständiges Maßnahmenmanagement.

## Mock-Portfolio (FR-19)

Ca. 20 Projekte mit mindestens einem Vertreter pro Szenario:

| Szenario | Zweck im Pilot |
|----------|----------------|
| Im Plan | Baseline, positive Referenz |
| Terminverzug | Terminabweichung, Ampel Gelb/Rot |
| Erhöhter Budgetverbrauch | Budget-KPI, Budget-Prognose |
| Offene Risiken | Risiko-Darstellung, Q&A |
| Widersprüchliche Signale | KPI vs. KI-Interpretation testen |
| Abgeschlossen | Portfolio-Aggregation, Filter |

Seed-Erweiterung (Story 3.7): Projektleitung, Probleme, Insight-Szenarien, 2 Berichtsstand-Snapshots pro Projekt.

## REST-Oberfläche — Ergänzungen (Planung)

| Endpunkt | FR | Beschreibung |
|---|---|---|
| `GET /api/projects` | FR-2 | Erweiterte Tabellenfelder |
| `GET /api/projects/{id}/master-data` | FR-5 | Stammdaten |
| `GET /api/projects/{id}/kpis` | FR-5 | Management-KPIs inkl. Prognose |
| `GET /api/projects/{id}/insights` | FR-20 | Deterministische Management Insights |
| `GET /api/projects/{id}/trends` | FR-21 | Berichtsstandsvergleich |
| `GET /api/projects/{id}/phases` | FR-5 | Phasen/Meilensteine Detail |
| `GET /api/projects/{id}/risks` | FR-6 | Risiken |
| `GET /api/projects/{id}/problems` | FR-6 | Probleme (getrennt) |

## MVP-Historisierung `[ASSUMPTION]`

Keine vollständige Audit-Historie. Stattdessen Tabelle `project_report_snapshots` mit wenigen Kennfeldern pro Snapshot (Fortschritt, Budget-Ist, Terminabweichung, Status, Risiko-Zähler, Snapshot-Datum). Seed liefert **aktuellen** und **vorherigen** Berichtsstand; `kpi.*` berechnet Deltas. Spätere echte Datenquellen können denselben Mechanismus nutzen oder ersetzen `[OFFEN]`.

## Demo auf Arbeitsrechner

Betriebsanforderung: `[OFFEN]` (lokaler Start, Docker, o. Ä.).

## Gemini-Integration (Hinweise für Architecture)

- Separate Endpunkte: Portfolio-Trendanalyse, Management-Zusammenfassung, Prognose, Projekt-Q&A
- **Eingabe:** freigegebene KPI-DTOs, Management-Insight-DTOs, Risiken/Probleme/Meilensteine — kein JPA
- Gemini **interpretiert** deterministische Fakten; erfindet keine KPI-Werte (FR-14)
- Prompting und Eingabe-Schema: `[OFFEN]`
- Speicherung von KI-Ausgaben: `[OFFEN]`
- Timeout/Fehlerbehandlung → FR-15

## Auswirkungen auf Stories 3.1–3.4

| Story | Impact |
|---|---|
| 3.1–3.3 | **Kein Breaking Change** — bereits implementiert; Erweiterung via **additive** Flyway-Migrationen (V3+) |
| 3.4 | DTOs/Reader/Services erweitern: Insight-DTOs, Projekt-KPI-DTOs, Trend-Deltas; Stub-Reader durch echte Berechnung ersetzen |
