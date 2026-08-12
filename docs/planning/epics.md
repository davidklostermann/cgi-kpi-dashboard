---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - stack-pivot-angular-cgi-2026-07-14
  - post-mvp-security-multi-user-2026-07-21
inputDocuments:
  - prds/prd-cgi-kpi-dashboard-2026-07-13/prd.md
  - prds/prd-cgi-kpi-dashboard-2026-07-13/addendum.md
  - prds/prd-cgi-kpi-dashboard-security-multi-user/prd.md
  - briefs/brief-cgi-kpi-dashboard-2026-07-13/brief.md
  - architecture/architecture-cgi-kpi-dashboard-2026-07-13/ARCHITECTURE-SPINE.md
  - architecture/architecture-cgi-kpi-dashboard-security/ARCHITECTURE-SPINE.md
  - ux-designs/ux-cgi-kpi-dashboard-2026-07-13/DESIGN.md
  - ux-designs/ux-cgi-kpi-dashboard-2026-07-13/EXPERIENCE.md
  - ux-designs/ux-cgi-kpi-dashboard-admin/DESIGN.md
  - ux-designs/ux-cgi-kpi-dashboard-admin/EXPERIENCE.md
  - prds/prd-cgi-kpi-dashboard-iso-management/prd-addendum.md
  - architecture/architecture-cgi-kpi-dashboard-iso-management/domain-extension.md
  - stories/stories-iso-management.md
stack: Angular 22 (docs historically said 20) + CGI EDS 19.0.0 + Spring Boot 3.5.16
post_mvp_extensions:
  - epic-15-iso-management-2026-07-29
  - epic-15-iso-management-done-2026-07-30
  - epic-16-capacity-data-binding-done-2026-07-30
  - epic-17-agile-delivery-2026-08-03
---

# cgi-kpi-dashboard — Epic Breakdown

## Overview

Epics und User Stories für den MVP-Pilot. Stack: **Angular 20 + CGI EDS 19.0.0** (Frontend), **Spring Boot + PostgreSQL** (Backend). Detaillierte Stories: [`stories/stories-mvp.md`](stories/stories-mvp.md).

## Requirements Inventory (aktualisiert 2026-07-15)

### Functional Requirements

FR-1 … FR-21 aus PRD (aktualisiert 2026-07-15). FR-1..FR-19 unverändert nummeriert; **FR-20** Management Insights, **FR-21** Berichtsstandsvergleich neu. Bindung an Epics in Coverage Map.

### NonFunctional Requirements

NFR-1: Aha-Moment ~30s · NFR-2: Gemini-Key serverseitig · NFR-3: Mock-only · NFR-4: KI-Kennzeichnung · NFR-5: KPI bei KI-Ausfall nutzbar · NFR-6: **WCAG 2.1 AA** · NFR-7: Desktop-first · NFR-8: Kein Auth **[SUPERSEDED 2026-07-21 → NFR-11..20 / Security-PRD]** · NFR-9: Formelles „Sie" · NFR-10: API-Fehler `{ code, message }`

**Post-MVP Security:** NFR-11..NFR-20 — siehe `prds/prd-cgi-kpi-dashboard-security-multi-user/prd.md`

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
UX-DR16: insight-list (Management Insights, FR-20) — API/Komponente vorhanden; aktuell **nicht** auf Projekt-Detailseite gerendert  
UX-DR17: report-comparison (Berichtsstandsvergleich, FR-21) — eigenständiger Faktenbereich auf der Detailseite
UX-DR18: iso-management-section (ISO 21502 – ergänzende Steuerungsfelder, FR-33) — fünf kompakte Cards zwischen Berichtsstandsvergleich und Team & Kapazität; keine Charts/Gauges
UX-DR19: agile-delivery-section (FR-35) — Portfolio-Spalte „Vorgehensmodell“ (Badges Agil/Hybrid/Klassisch); Detail nur AGILE/HYBRID: Sprint-Karten, Grafik „Sprint-Entwicklung“, KPI-Leiste; keine Prognose-/Risiko-Panels
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
| FR-5 | Epic 6 | 6.1, 6.2, 6.3, 6.4 |
| FR-6 | Epic 7 | 7.1, 7.2, 7.4, 7.5 |
| FR-7 | Epic 2, 5, 6 | 2.2, 5.5, 6.5 |
| FR-8 | Epic 4 | 4.4 |
| FR-9 | Epic 3, 4, 6 | 3.4, 4.1, 6.1, 6.6 |
| FR-10 | Epic 2, 8, 9 | 2.4, 8.3, 9.4 |
| FR-11 | Epic 9 | 9.2, 9.4 |
| FR-12 | Epic 9 | 9.2, 9.4 |
| FR-13 | Epic 8, 9 | 8.1, 9.1 |
| FR-14 | Epic 9 | 9.3 |
| FR-15 | Epic 10 | 10.1, 10.2 |
| FR-16 | Epic 9 | 9.3, 9.4 |
| FR-17 | Epic 9 | 9.5 |
| FR-18 | Epic 9 | 9.3 |
| FR-19 | Epic 3 | 3.2, 3.3, 3.7 |
| FR-20 | Epic 6 | 6.6 |
| FR-21 | Epic 6 | 6.7 |
| FR-22 | Epic 11 | 11.2–11.6 |
| FR-23 | Epic 11, 12 | 11.1, 12.2 |
| FR-24 | Epic 13 | 13.1–13.2 |
| FR-25 | Epic 12 | 12.1, 12.4 |
| FR-26 | Epic 12, 13 | 12.2, 13.4 |
| FR-27 | Epic 13 | 13.3–13.4 |
| FR-28 | Epic 13 | 13.3 |
| FR-29 | Epic 14 | 14.2 |
| FR-30 | Epic 11, 14 | 11.6, 14.3 |
| FR-31 | Epic 11, 13 | 11.4, 11.6, 13.1 |
| FR-32 | Epic 13 | 13.4 |
| FR-33 | Epic 15 | 15.1, 15.2, 15.3 |
| FR-34 | Epic 16 | 16.1, 16.2, 16.3 |
| FR-35 | Epic 17 | 17.1, 17.2, 17.3, 17.4 |

## Epic List

| # | Epic | Ziel | Stories |
|---|---|---|---|
| 1 | Technische Projektgrundlage | Monorepo, Backend-/Frontend-Grundgerüst, Dev-Setup | 1.1–1.4 |
| 2 | CGI-konforme Anwendungshülle | Shell, Routing, Layout-Raster | 2.1–2.4 |
| 3 | Mock-Daten und Backend-Grundlage | Domain, Flyway, Seed, KPI-Modul, Domain-Erweiterung, Basis-API | 3.1–3.7 |
| 4 | Portfolio-KPI-Übersicht | KPI-Berechnung, Karten, Filter | 4.1–4.4 |
| 5 | Portfolio-Zeitleiste und Projekttabelle | Gantt, Management-Tabelle, Trend, Navigation | 5.1–5.5 |
| 6 | Projekt-Detailseite | Stammdaten, KPIs, Phasen/Gantt, Issues/Kapazität, Berichtsvergleich (Insights-API ohne Detail-UI) | 6.1–6.7 |
| 7 | Risiken, Probleme, Maßnahmen und Kapazität | Kombinierte Issues-/Maßnahmen-UI, Kapazitätssicht, Status-Badges | 7.1–7.5 |
| 8 | KI-Portfolioanalyse | Trendanalyse Backend + Panel | 8.1–8.4 |
| 9 | KI-Projektanalyse und Q&A | Summary, Prognose, Q&A, Chips | 9.1–9.5 |
| 10 | Fehlerbehandlung, Barrierefreiheit, Qualität | AD-7, FR-15, WCAG | 10.1–10.4 |
| 11 | Authentifizierung und Benutzerbasis | Session-Login, Bootstrap, Guards | 11.1–11.6 |
| 12 | Autorisierung und Datenisolierung | Workspace, IDOR, AI-Cache | 12.1–12.4 |
| 13 | Administration und API-Key | User-Mgmt, verschlüsselte KI-Config | 13.1–13.4 |
| 14 | Security Hardening und Betrieb | CSRF, Rate Limit, Audit, Scanning | 14.1–14.3 |
| 15 | ISO-orientierte Projektsteuerung | ISO-21502-Steuerungsfelder auf Projekt-Detail (5 Cards) | 15.1–15.3 |
| 16 | Kapazitaetsdaten anbinden | Team & Kapazitaet mit rollenbasierten Daten fuer ISO-orientierte Projektbeispiele befuellen | 16.1–16.3 |
| 17 | Agile Projektsteuerung und externe Projektdaten | Vorgehensmodell im Portfolio; Agile Delivery (Sprint-Karten, Sprint-Entwicklung, KPI-Leiste) für AGILE/HYBRID; Mock + Provider | 17.1–17.4 |

Detaillierte Security-Stories: [`stories/stories-security-multi-user.md`](stories/stories-security-multi-user.md).  
Detaillierte ISO-Stories: [`stories/stories-iso-management.md`](stories/stories-iso-management.md).
Detaillierte Capacity-Stories: [`stories/stories-capacity-data-binding.md`](stories/stories-capacity-data-binding.md).
Detaillierte Agile-Stories: [`stories/stories-agile-delivery.md`](stories/stories-agile-delivery.md).

### Epic 11 — Authentifizierung und Benutzerbasis

- **Ziel:** Schema, Env-Bootstrap-Admin, Spring Session-Auth, Login-UI, Basis-Tests.
- **Nutzen:** Identifizierbare Nutzer; Grundlage für AuthZ.
- **Abhängigkeiten:** MVP Epics 1–9; empfohlen nach/parallel Epic 10.
- **Stories:** 11.1 Datenmodell+Flyway-Plan · 11.2 Bootstrap-Admin · 11.3 Security-Grundconfig · 11.4 Login/Logout/me · 11.5 Angular Auth · 11.6 Session-/Auth-Tests
- **AC (Epic):** FR-22; NFR-12/13; kein Flyway-Default-Passwort; kein JWT/localStorage.
- **Risiken:** Bootstrap falsch → unsicherer Admin; Session-Fixation.
- **DoD:** Auth-Tests grün; AD-12 DECIDED umgesetzt.

### Epic 12 — Autorisierung und Datenisolierung

- **Ziel:** Workspace-Scope, Rollen+Objekt-AuthZ, Cache-Isolation, KI nur ADMIN.
- **Nutzen:** Kein Fremdzugriff / keine Datenvermischung.
- **Abhängigkeiten:** Epic 11 (mind. 11.3–11.4).
- **Stories:** 12.1 Scope+Private Settings · 12.2 Policies/KI-ADMIN · 12.3 Cache · 12.4 Isolationstests
- **AC:** FR-23/25/26; NFR-11/16; P0 Workspace-Modell.
- **Risiken:** Unscoped Queries; unsicherer Cache.
- **DoD:** IDOR-/Role-/Cache-Tests grün.

### Epic 13 — Administration und API-Key

- **Ziel:** User-Admin; verschlüsselte Provider-Config; Connection-Test; Cache-Bust.
- **Nutzen:** Operative Key-/User-Verwaltung ohne Klartext-Leak.
- **Abhängigkeiten:** Epic 11; **empfohlen nach 12.2** (KI-Policy); 13.4 nach 12.3.
- **Stories:** 13.1 User-API · 13.2 User-UI · 13.3 Crypto+Config · 13.4 Admin-AI+Test+Rotation
- **AC:** FR-24/27/28/32; NFR-15/17; Last-Admin-Guards; MK∉PG.
- **Risiken:** Key-Logging; fehlender Master-Key; Prod-KMS Blocker.
- **DoD:** Ciphertext; maskierte API; Connection-Test ohne Key-Echo.

### Epic 14 — Security Hardening und Betrieb

- **Ziel:** CSRF/CORS/Headers, Rate Limits, Audit-UI, Scanning, Doku (Session-Scale, KMS-Blocker).
- **Nutzen:** Nachweisbare Härtung vor Betrieb.
- **Abhängigkeiten:** Epic 11–13.
- **Stories:** 14.1 Transport/Cookie/CSRF · 14.2 Rate-Limit+Audit · 14.3 Scans+Last+Runbooks
- **AC:** FR-29/30; NFR-14/18/19/20.
- **Risiken:** CORS; Retention OFFEN; Prod Secret Store OFFEN.
- **DoD:** Threat-Mitigations nachweisbar; Betriebsdoku.

### Epic 15 — ISO-orientierte Projektsteuerung

- **Ziel:** Projekt-Detailseite um kompakte Sektion „ISO 21502 – ergänzende Steuerungsfelder“ mit fünf gleichartigen Management-Cards erweitern — ohne KPI-Overload und ohne neue Charts.
- **Nutzen:** Führungskräfte und Projektleiter sehen Nutzen, Scope, Changes, Qualität und Stakeholder auf einen Blick ergänzend zu bestehenden KPIs.
- **Abhängigkeiten:** Epics 1–14 (vorausgesetzt abgeschlossen); Epic 6 (Detailseite), Epic 9 (AI Facts), Epic 12 (Workspace-Isolation).
- **Stories:** 15.1 Domain & API · 15.2 UI · 15.3 Integration, Tests & AI Facts
- **AC (Epic):** FR-33; fünf Cards; Position nach Berichtsstandsvergleich; kein ISO-Compliance-Claim; Workspace-404; AI Facts mit `fact-iso-management`.
- **Risiken:** KPI-Overload trotz Vorgabe; Verwechslung mit Normzertifizierung — durch UX-Copy und DESIGN.md mitigieren.
- **DoD:** V12/V13 Flyway; Production Build grün; bestehende Detail-Sections unverändert.

### Epic 16 - Kapazitaetsdaten anbinden

- **Ziel:** Bestehende Team-&-Kapazitaet-Section mit echten rollenbasierten Kapazitaetsdaten fuer mehrere Projektbeispiele befuellen.
- **Nutzen:** Projektleiter sehen benoetigte vs. verfuegbare Rollen-/Skill-Abdeckung, ohne sensible Personaldetails.
- **Abhaengigkeiten:** Epic 7.5 (`/capacity`), Epic 15 (visuelles Management-Pattern), Epic 12 (Workspace-Isolation).
- **Stories:** 16.1 Capacity-Seeds · 16.2 UI-Angleichung · 16.3 Tests und Schnittstellennachweis
- **AC (Epic):** Bestehender Endpoint `GET /api/projects/{id}/capacity`; mehrere Projekte mit Daten; Empty-State nur bei fehlenden Daten; keine Personen-/Gehaltsdaten.
- **Risiken:** Doppeltes Capacity-Modell neben Story 7.5; mitigiert durch Wiederverwendung der vorhandenen API/DTOs.
- **DoD:** V14 Seed; Component-/Integrationstests; Production Build grün.

### Epic 17 — Agile Projektsteuerung und externe Projektdaten

- **Ziel:** Projekte mit `deliveryMethod` (AGILE/HYBRID/WATERFALL) im Portfolio kennzeichnen; bei AGILE/HYBRID auf der Detailseite „Agile Delivery“ mit Sprint-Karten, Grafik „Sprint-Entwicklung“ und KPI-Leiste bereitstellen. Mock-Daten + Provider-Abstraktion für spätere Jira-/PPM-Anbindung.
- **Nutzen:** Schnelle Einordnung des Vorgehensmodells; transparente Sprint-Fortschritte, Velocity, Carry-over und Blocker ohne Board-Klon.
- **Abhängigkeiten:** Epic 3 (Domain/Flyway/Seed), Epic 5 (Portfolio-Tabelle/Filter), Epic 6 (Detailseite), Epic 12 (Workspace-Isolation), Epic 15 (Section-Position nach ISO).
- **Stories:** 17.1 Agiles Datenmodell und Mock-Daten · 17.2 Backend-Abfrage und KPI-Berechnung · 17.3 Vorgehensmodell und Agile Delivery im Frontend · 17.4 QA und Jira-Readiness
- **AC (Epic):** FR-35; Portfolio-Spalte „Vorgehensmodell“ mit Badges Agil/Hybrid/Klassisch, sortier- und filterbar; Agile-Delivery-Sektion nur AGILE/HYBRID (Titel/Untertitel, Sprint-Karten, Combo-Chart, KPI-Leiste); WATERFALL ohne Sektion; keine Lieferprognose/Abschlussprognose/Prognosegenauigkeit/Risikoübersicht/„Nächste Risiken“.
- **Risiken:** Chart ohne neues Framework; Health-Schwellen `[OFFEN]`; Overengineering Provider — mitigiert durch schmales Interface und bestehende Visualisierungsansätze.
- **DoD:** V15/V16; Portfolio-Spalte + Detail-Sektion gemäß Stories; Production Builds grün; Provider-Handoff dokumentiert.

## Implementierungsreihenfolge

1. **1.1 → 1.4** — Technische Grundstruktur  
2. **3.1 → 3.7** — Backend + Mock-Daten inkl. Domain-Erweiterung (parallel ab 1.2: **2.1 → 2.4** Shell)  
3. **4.1 → 4.4** — Portfolio-KPIs  
4. **5.1 → 5.5** — Zeitleiste + Management-Tabelle  
5. **6.1 → 6.7** + **7.1 → 7.5** — Projekt-Detail, Berichtsvergleich, Issues/Kapazität (Management-Insights-API ohne Detail-UI)  
6. **8.1 → 8.4** — KI Portfolio (nach KPI-Readern)  
7. **9.1 → 9.5** — KI Projekt (nach ApprovedProjectDataReader)  
8. **10.1 → 10.4** — Querschnitt A11y/Fehler (forlaufend, Abschluss-Review)
9. **11.1 → 11.6** — Auth-Basis (Post-MVP Security; P0 geschlossen)
10. **12.1 → 12.4** — AuthZ + Isolation
11. **13.1 → 13.4** — Admin + API-Key
12. **14.1 → 14.3** — Hardening + Betrieb
13. **15.1 → 15.3** — ISO-Steuerungsfelder auf Projekt-Detail (nach Epics 1–14)
14. **17.1 → 17.4** — Agile Projektsteuerung (nach Epic 15/16)

## Deferred Decisions

jpackage · Windows-EXE · produktives Deployment · CGI-Infrastruktur · echte Kundendaten · **Auth/Rollen (SUPERSEDED — jetzt Epics 11–14)** · produktive Gemini-Config / **Prod-KMS Vendor (Blocker)** · **endgültige Chart-Bibliothek** · CI/CD · Docker · eingeschränkter Arbeitsrechner-Betrieb · KPI-Formeln (fachlich OFFEN) · CGI-SSO Timing · Session-TTL-Feinwerte · Audit-Retention

## Course Correction (2026-07-17) — Epic 8 Portfolio-KI

**Shift Story 8.2 / 8.4:** Response-Modell von narrativem `{ text, topProjects[3] }` auf typisiertes **Portfolio-Insight-Modell** (`PortfolioInsight[]` mit Mustertyp, Belegen, Confidence/DataQuality). UI-Titel „Portfolio-Muster und systemische Risiken“ statt „KI-Einschätzung“ / Top-3-Handlungsbedarf.

**Reduzierter Mustertyp-Scope (initial):** Nur Mustertypen mit echter Seed-Basis aktiv: `DETERIORATING_TREND`, `REPORTING_PATTERN`. Deaktiviert bis Seed/Datenbasis nachgerüstet: `CAPACITY_CONFLICT`, `SHARED_DEPENDENCY`, `MEASURE_INEFFECTIVENESS` (sowie weitere Typen ohne belastbare Querschnitt-Daten). Kein Seed-Ausbau nur zur Demo-Belegproduktion — Detector liefert lieber keinen Insight als einen unbelegten.

**Pipeline:** Deterministische Kandidatenerkennung vor LLM-Formulierung; Insights ohne ≥2 betroffene Projekte bzw. ohne ausreichende Belege werden verworfen. Story-Status bleibt **review** (nicht automatisch `done`).

## Post-MVP Security (2026-07-21)

Planungsartefakte: `prds/prd-cgi-kpi-dashboard-security-multi-user/`, `architecture/architecture-cgi-kpi-dashboard-security/`, `ux-designs/ux-cgi-kpi-dashboard-admin/`, `stories/stories-security-multi-user.md`.

**AD-6 / NFR-8 SUPERSEDED.** Security-ADs: **AD-12..AD-18** (AD-11 = CGI EDS).  
**P0-Entscheidungen geschlossen 2026-07-21** (Workspace, Bootstrap-Env, Session, KI-ADMIN, Cache, Master-Key Dev).  
Sprint-Tracking: `sprint-status.yaml` aktualisiert via Sprint Planning (2026-07-21) — Epics 1–9 `done`, 10–14 `backlog`, Retrospectives `optional`.  
Nächster Workflow: `bmad-create-story` für **11-1-datenmodell-workspace-user-flyway**.

## Offene Fragen

| Thema | Status |
|---|---|
| Sortierbare Tabellenspalten | FR-2: Status, Fortschritt, Terminabweichung, Budgetabweichung, kritische Risiken, letzte Aktualisierung |
| Zeitraum-Filter-Definition | `[OFFEN]` — MVP: Berichtsmonat |
| Risikostufe-Filter | `[OFFEN]` |
| Management-Insight-Regeln | `[OFFEN]` — Engine in `kpi.insights` |
| Berichtsstand-Snapshots | `[ASSUMPTION]` 2 je Projekt im MVP |
| Top-3-Logik (KI vs. deterministisch) | **Course Correction 2026-07-17:** ersetzt durch typisierte Portfolio-Insights; Top-3-Narrativ nicht mehr Zielmodell |
| Leerzustands-Microcopy Portfolio | `[OFFEN]` |
| CGI EDS Paket-Installation (npm registry vs. lokal) | Implementierungsphase |

## Bekannte Risiken

| Risiko | Mitigation |
|---|---|
| CGI EDS Paket noch nicht installiert | Referenz-TGZ; Story 2.1 mit Stub/Material bis Integration |
| Gantt ohne Chart-Lib aufwendig | MVP-Scope HTML/CSS/SVG; Deferred für Lib |
| Gemini-Latenz blockiert UX | AD-7: unabhängige Panels |
| Kontrast historischer Mockups | Spines + DESIGN.md CGI-Farben maßgeblich |
