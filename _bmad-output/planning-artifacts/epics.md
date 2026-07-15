---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - stack-pivot-angular-cgi-2026-07-14
inputDocuments:
  - prds/prd-cgi-kpi-dashboard-2026-07-13/prd.md
  - prds/prd-cgi-kpi-dashboard-2026-07-13/addendum.md
  - briefs/brief-cgi-kpi-dashboard-2026-07-13/brief.md
  - architecture/architecture-cgi-kpi-dashboard-2026-07-13/ARCHITECTURE-SPINE.md
  - ux-designs/ux-cgi-kpi-dashboard-2026-07-13/DESIGN.md
  - ux-designs/ux-cgi-kpi-dashboard-2026-07-13/EXPERIENCE.md
stack: Angular 20 + CGI EDS 19.0.0 + Spring Boot 3.5.16
---

# cgi-kpi-dashboard — Epic Breakdown

## Overview

Epics und User Stories für den MVP-Pilot. Stack: **Angular 20 + CGI EDS 19.0.0** (Frontend), **Spring Boot + PostgreSQL** (Backend). Detaillierte Stories: [`stories/stories-mvp.md`](stories/stories-mvp.md).

## Requirements Inventory (aktualisiert 2026-07-14)

### Functional Requirements

FR-1 … FR-19 unverändert aus PRD (siehe vorherige Extraktion). Bindung an Epics in Coverage Map.

### NonFunctional Requirements

NFR-1: Aha-Moment ~30s · NFR-2: Gemini-Key serverseitig · NFR-3: Mock-only · NFR-4: KI-Kennzeichnung · NFR-5: KPI bei KI-Ausfall nutzbar · NFR-6: **WCAG 2.1 AA** · NFR-7: Desktop-first · NFR-8: Kein Auth · NFR-9: Formelles „Sie" · NFR-10: API-Fehler `{ code, message }`

### Additional Requirements (Architektur)

- Monorepo `backend/` + `frontend/` (Angular 20, SCSS, Material, CDK, CGI EDS 19.0.0)
- Feature-Struktur: `core/`, `shared/`, `features/portfolio|project|ai`
- RxJS + HttpClient; Signals für UI-Zustand; **kein TanStack/NgRx**
- `kpi.*` / `ai.*` Grenzen; Reader-Interfaces für Gemini
- REST gemäß AD-5; UUID; Flyway Mock-Seed; lokaler Betrieb ohne Docker
- Gantt: Angular/HTML/CSS/SVG; **keine verbindliche Chart-Bibliothek**
- Referenzpaket: `frontend/vendor/cgi-sentry-angular-components-lib-19.0.0.tgz`

### UX Design Requirements

UX-DR1: Layout Haupt 8–9/12 · KI 3–4/12 (Desktop); KI unter Hauptinhalt (Tablet/Mobil)  
UX-DR2: CGI EDS Farben und Shell (Top/Side Nav, Breadcrumbs)  
UX-DR3–UX-DR11: kpi-card, status-badge, ki-panel, trend-chart, gantt-timeline, project-table, filter-bar, quick-reply, chat  
UX-DR12: Unabhängiges Laden Fakten/KI  
UX-DR13: WCAG 2.1 AA, sr-only für Visualisierungen  
UX-DR14: Keine Donut/Tacho-Charts  
UX-DR15: Caption-Kontrast `{colors.caption}` (#333333)

## FR Coverage Map

| FR | Epic(s) | Story-IDs |
|---|---|---|
| FR-1 | Epic 4 | 4.1, 4.2, 4.3 |
| FR-2 | Epic 5 | 5.3 |
| FR-3 | Epic 5 | 5.2, 5.4 |
| FR-4 | Epic 8 | 8.2, 8.3, 8.4 |
| FR-5 | Epic 6 | 6.1, 6.3 |
| FR-6 | Epic 7 | 7.1, 7.2 |
| FR-7 | Epic 2, 5, 6 | 2.2, 5.5, 6.5 |
| FR-8 | Epic 4 | 4.4 |
| FR-9 | Epic 3, 4, 6 | 3.4, 4.1, 6.1 |
| FR-10 | Epic 2, 8, 9 | 2.4, 8.3, 9.4 |
| FR-11 | Epic 9 | 9.2, 9.4 |
| FR-12 | Epic 9 | 9.2, 9.4 |
| FR-13 | Epic 8, 9 | 8.1, 9.1 |
| FR-14 | Epic 9 | 9.3 |
| FR-15 | Epic 10 | 10.1, 10.2 |
| FR-16 | Epic 9 | 9.3, 9.4 |
| FR-17 | Epic 9 | 9.5 |
| FR-18 | Epic 9 | 9.3 |
| FR-19 | Epic 3 | 3.2, 3.3 |

## Epic List

| # | Epic | Ziel | Stories |
|---|---|---|---|
| 1 | Technische Projektgrundlage | Monorepo, Backend-/Frontend-Grundgerüst, Dev-Setup | 1.1–1.4 |
| 2 | CGI-konforme Anwendungshülle | Shell, Routing, Layout-Raster | 2.1–2.4 |
| 3 | Mock-Daten und Backend-Grundlage | Domain, Flyway, Seed, KPI-Modul, Basis-API | 3.1–3.5 |
| 4 | Portfolio-KPI-Übersicht | KPI-Berechnung, Karten, Filter | 4.1–4.4 |
| 5 | Portfolio-Zeitleiste und Projekttabelle | Gantt, Tabelle, Trend, Navigation | 5.1–5.5 |
| 6 | Projekt-Detailseite | Kernkennzahlen, Zeitleiste, Budget | 6.1–6.5 |
| 7 | Risiken, Termine und Maßnahmen | Risiko-API und -UI, Status-Badges | 7.1–7.3 |
| 8 | KI-Portfolioanalyse | Trendanalyse Backend + Panel | 8.1–8.4 |
| 9 | KI-Projektanalyse und Q&A | Summary, Prognose, Q&A, Chips | 9.1–9.5 |
| 10 | Fehlerbehandlung, Barrierefreiheit, Qualität | AD-7, FR-15, WCAG | 10.1–10.4 |

## Implementierungsreihenfolge

1. **1.1 → 1.4** — Technische Grundstruktur  
2. **3.1 → 3.5** — Backend + Mock-Daten (parallel ab 1.2: **2.1 → 2.4** Shell)  
3. **4.1 → 4.4** — Portfolio-KPIs  
4. **5.1 → 5.5** — Zeitleiste + Tabelle  
5. **6.1 → 6.5** + **7.1 → 7.3** — Projekt-Detail + Risiken  
6. **8.1 → 8.4** — KI Portfolio (nach KPI-Readern)  
7. **9.1 → 9.5** — KI Projekt (nach ApprovedProjectDataReader)  
8. **10.1 → 10.4** — Querschnitt A11y/Fehler (forlaufend, Abschluss-Review)

## Deferred Decisions

jpackage · Windows-EXE · produktives Deployment · CGI-Infrastruktur · echte Kundendaten · Auth/Rollen · produktive Gemini-Config · **endgültige Chart-Bibliothek** · CI/CD · Docker · eingeschränkter Arbeitsrechner-Betrieb · KPI-Formeln (fachlich OFFEN)

## Offene Fragen

| Thema | Status |
|---|---|
| Sortierbare Tabellenspalten (welche?) | `[OFFEN]` — MVP: Name, Status, Fortschritt |
| Zeitraum-Filter-Definition | `[OFFEN]` — MVP: Berichtsmonat |
| Top-3-Logik (KI vs. deterministisch) | `[OFFEN]` — MVP: KI aus Reader-DTOs |
| Leerzustands-Microcopy Portfolio | `[OFFEN]` |
| CGI EDS Paket-Installation (npm registry vs. lokal) | Implementierungsphase |

## Bekannte Risiken

| Risiko | Mitigation |
|---|---|
| CGI EDS Paket noch nicht installiert | Referenz-TGZ; Story 2.1 mit Stub/Material bis Integration |
| Gantt ohne Chart-Lib aufwendig | MVP-Scope HTML/CSS/SVG; Deferred für Lib |
| Gemini-Latenz blockiert UX | AD-7: unabhängige Panels |
| Kontrast historischer Mockups | Spines + DESIGN.md CGI-Farben maßgeblich |
