# Konsistenzcheck — cgi-kpi-dashboard Planungsartefakte

**Datum:** 2026-07-15  
**Anlass:** Erweiterung Management-Dashboard — nachvollziehbare Einzelprojekt-Analyse (Abschnitte A–H)

## Geprüfte Dokumente

| Dokument | Status | Konsistent |
|---|---|---|
| brief.md | aktualisiert (Scope, Nutzersichten) | ✓ |
| brief addendum.md (KPI-Inventar) | erweitert §5 Insights, §6 Berichtsstand | ✓ |
| prd.md | FR-2/5/6/8/11/12/16 präzisiert; FR-20/21 neu | ✓ |
| prd addendum.md | Domain, REST, Historisierung, Impact 3.1–3.4 | ✓ |
| ARCHITECTURE-SPINE.md | FR-1..21, kpi.insights, Snapshots | ✓ |
| EXPERIENCE.md | Detail-Hierarchie, insight-list, report-comparison | ✓ |
| DESIGN.md | Layout, Komponenten UX-DR16/17 | ✓ |
| epics.md | FR-Coverage FR-20/21; Epic 3/6/7 erweitert | ✓ |
| stories/stories-mvp.md | Stories 3.6/3.7, 6.6/6.7, 7.4; Erweiterungen 5.3, 6.x, 9.x | ✓ |
| reconcile-brief.md | aktualisiert 2026-07-15 | ✓ |

## Abgleich Anforderungen A–H ↔ Artefakte

| Anforderung | Brief/Addendum | PRD | UX | Arch | Stories |
|---|---|---|---|---|---|
| A — Management-Tabelle | ✓ | FR-2 | project-table | GET /projects | 5.3 |
| B — Projekt-Detail (7 Bereiche) | ✓ | FR-5, FR-6 | IA Hierarchie | Capability Map | 6.1–6.4, 7.1–7.4 |
| C — Management Insights | Addendum §5 | FR-20 | insight-list | kpi.insights | 6.6 |
| D — KI interpretiert | Addendum §7 | FR-11/12/16 | ki-panel | AD-4 | 9.1–9.4 |
| E — Berichtsstandsvergleich | Addendum §6 | FR-21 | report-comparison | project_report_snapshots | 6.7, 3.6/3.7 |
| F — UX-Hierarchie | ✓ | FR-10 | EXPERIENCE §Detail | — | 6.x Layout |
| G — Domain/API/Stories | prd addendum | FR-9 | — | Domain Extension | 3.6, 3.7, 6.6, 6.7, 7.4 |

## FR-Abdeckung (neu/geändert)

| FR | Epic | Stories | Status |
|---|---|---|---|
| FR-2 (erweitert) | 5 | 5.3 | Tabellenspalten + Sortierung dokumentiert |
| FR-5 (erweitert) | 6 | 6.1–6.4 | Stammdaten, KPIs, Phasen, Budget |
| FR-6 (erweitert) | 7 | 7.1, 7.2, 7.4 | Risiken/Probleme getrennt |
| FR-8 (erweitert) | 4 | 4.4 | Filter erweitert |
| FR-11/12/16 (präzisiert) | 9 | 9.2–9.4 | KI erfindet nicht |
| FR-20 (neu) | 6 | 6.6 | Deterministische Insights |
| FR-21 (neu) | 6 | 6.7 | Berichtsstandsvergleich |

## Architektur-Entscheidungen

| Entscheidung | Status | Dokument |
|---|---|---|
| MVP-Historisierung via `project_report_snapshots` (2 Snapshots/Projekt) | `[ASSUMPTION]` | prd addendum, ARCHITECTURE-SPINE |
| Insight-Regeln in `kpi.insights` | `[OFFEN]` Schwellenwerte | FR-20, Story 6.6 |
| Kein vollständiges PM-/Risiko-Workflow-System | bestätigt | PRD Non-Goals |
| Additive Flyway V3+ (kein Breaking Change zu 3.1–3.3) | bestätigt | prd addendum |

## Impact Stories 3.1–3.4 (Implementierung)

| Story | Impact | Maßnahme |
|---|---|---|
| 3.1 Domain V1 | Kein Breaking Change | Additive Migration V3 |
| 3.2 Flyway V1 | Kein Breaking Change | V3 ergänzt Tabellen/Felder |
| 3.3 Seed V2 | Erweiterung nötig | Story 3.7 Seed V4 |
| 3.4 KPI-Modul (review) | Erweiterung nötig | DTOs, Insights-Engine, echte Reader nach 3.6 |

## Verbleibende bewusste Inkonsistenzen

| Item | Begründung |
|---|---|
| HTML-Mockups (portfolio-und-projekt-detail.html) | Nicht aktualisiert; Spines maßgeblich |
| validation-reports (2026-07-13) | Vor Amendment; Spine/UX Amendment 2026-07-15 maßgeblich |
| Aufgaben-Metriken im KPI-Inventar | `[NON-GOAL im MVP]` — bewusst aus Scope |

## Ergebnis

**Planungsartefakte sind konsistent** für die erweiterte Management-Analyse. Keine Blocker für Stories ab 3.5 (nach Abschluss 3.4). Empfohlene Reihenfolge: **3.5 → 3.6 → 3.7 → 4.x/5.x → 6.6/6.7 → 7.4 → 9.x (Reader-Erweiterung)**.
