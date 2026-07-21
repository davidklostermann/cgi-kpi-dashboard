# User Stories — MVP cgi-kpi-dashboard

> Stack: Angular 20 + CGI EDS · Spring Boot · PostgreSQL · Gemini serverseitig  
> Format: Given-When-Then wo sinnvoll

---

## Epic 1 — Technische Projektgrundlage

### Story 1.1 — Backend-Grundgerüst Spring Boot

**Als** Entwickler **möchte ich** ein lauffähiges Spring-Boot-Backend im Monorepo **damit** REST-APIs bereitgestellt werden können.

**Nutzen:** Technische Basis für alle KPI- und KI-Endpunkte.

**Akzeptanzkriterien:**
- Gegeben ein leeres `backend/`-Modul, wenn `mvn spring-boot:run` ausgeführt wird, dann startet die Anwendung auf Port 8080.
- Gegeben der laufende Server, wenn `GET /actuator/health` aufgerufen wird, dann antwortet er mit Status UP.
- Gegeben die Paketstruktur, wenn geprüft wird, dann existieren Seeds für `api`, `application`, `domain`, `kpi`, `ai`, `infrastructure`.

**UX:** —  
**Architektur:** AD-1, Stack Java 21 / Spring Boot 3.5.16  
**Abhängigkeiten:** —  
**Nicht enthalten:** Business-Endpunkte, Flyway-Daten  
**Tests:** `@SpringBootTest` Context Load; Health-Endpoint Integrationstest

---

### Story 1.2 — Angular-Grundstruktur feature-basiert

**Als** Entwickler **möchte ich** die dokumentierte Angular-Ordnerstruktur **damit** Features isoliert entwickelt werden können.

**Akzeptanzkriterien:**
- Gegeben `frontend/`, wenn die App startet (`ng serve`), dann lädt die Startseite ohne Fehler.
- Gegeben die Struktur, dann existieren `core/`, `shared/`, `features/portfolio`, `features/project`, `features/ai`.
- Gegeben eine Präsentationskomponente, dann enthält sie keinen direkten HttpClient-Import.

**UX:** —  
**Architektur:** AD-10, Structural Seed  
**Abhängigkeiten:** 1.1 (Proxy optional)  
**Nicht enthalten:** CGI EDS Installation, Feature-Inhalte  
**Tests:** Angular Unit Test AppComponent bootstrap

---

### Story 1.3 — Lokales Entwicklungs-Setup

**Als** Entwickler **möchte ich** dokumentierte Startschritte **damit** Backend, DB und Frontend lokal starten.

**Akzeptanzkriterien:**
- Gegeben README, wenn befolgt, dann sind PostgreSQL, Backend und Frontend startbar.
- Gegeben Angular Dev-Server, wenn `/api/*` aufgerufen wird, dann proxied er zum Backend `:8080`.
- Gegeben `environment.ts`, dann enthält er nur `apiBaseUrl`, keine Secrets.

**Architektur:** AD-8, AD-9  
**Abhängigkeiten:** 1.1, 1.2  
**Nicht enthalten:** Docker, CI/CD  
**Tests:** Manueller Smoke-Test Proxy

---

### Story 1.4 — Einheitliches API-Fehlerformat

**Als** Frontend-Entwickler **möchte ich** strukturierte Fehlerantworten **damit** Fehlerpanels konsistent sind.

**Akzeptanzkriterien:**
- Gegeben ein Backend-Fehler, wenn die API antwortet, dann enthält der Body `{ "code", "message" }`.
- Gegeben ein Global Exception Handler, wenn eine unbehandelte Exception auftritt, dann wird 500 mit strukturiertem Body zurückgegeben.

**Architektur:** Consistency Conventions  
**Abhängigkeiten:** 1.1  
**Nicht enthalten:** Frontend-Fehler-UI  
**Tests:** WebMvcTest für Fehler-Controller

---

## Epic 2 — CGI-konforme Anwendungshülle

### Story 2.1 — CGI Shell (Top Nav, Side Nav)

**Als** Nutzer **möchte ich** eine CGI-konforme Anwendungshülle **damit** ich mich orientieren kann.

**Akzeptanzkriterien:**
- Gegeben die App, wenn sie geladen wird, dann sind Top Navigation und Side Navigation sichtbar.
- Gegeben Side Navigation, dann enthält sie Einträge „Portfolio" und „Projekte".
- Gegeben CGI EDS noch nicht installiert, dann nutzt die Shell Angular Material als temporären Stub mit CGI-Farben aus DESIGN.md.

**UX:** UX-DR2, CGI Shell  
**Architektur:** AD-11  
**Abhängigkeiten:** 1.2  
**Nicht enthalten:** Feature-Inhalte  
**Tests:** Component Test Shell; visueller Abgleich CGI-Farben

---

### Story 2.2 — Routing und Lazy Loading

**Als** Nutzer **möchte ich** zwischen Portfolio und Projekten navigieren **damit** ich Oberflächen wechseln kann.

**Akzeptanzkriterien:**
- Gegeben Side Nav „Portfolio", wenn geklickt, dann wird `/portfolio` geladen (lazy).
- Gegeben Side Nav „Projekte", wenn geklickt, dann wird `/projects` geladen.
- Gegeben Route `/projects/:id`, dann lädt Projekt-Detail lazy.

**UX:** IA Portfolio/Projekte  
**Architektur:** AD-10  
**Abhängigkeiten:** 2.1  
**Nicht enthalten:** API-Daten  
**Tests:** Router Testing Module Navigation

---

### Story 2.3 — Breadcrumbs und Seitentitel

**Als** Nutzer **möchte ich** Breadcrumbs auf der Projekt-Detailseite **damit** ich zurück zum Portfolio navigieren kann.

**Akzeptanzkriterien:**
- Gegeben Projekt-Detail, wenn angezeigt, dann zeigt Breadcrumb „Portfolio > [Projektname]".
- Gegeben Breadcrumb „Portfolio", wenn geklickt, dann navigiert zurück zu `/portfolio`.

**UX:** Projekt-Detail Aufbau Punkt 1  
**Abhängigkeiten:** 2.2  
**Nicht enthalten:** Filter-Persistenz  
**Tests:** Component Test Breadcrumb-Klick

---

### Story 2.4 — Layout-Raster Haupt vs. KI-Spalte

**Als** Nutzer **möchte ich** Fakten und KI räumlich getrennt **damit** ich Fakten von Einschätzungen unterscheide (FR-10).

**Akzeptanzkriterien:**
- Gegeben Desktop ≥1200px, wenn Portfolio/Detail geladen, dann ist Hauptbereich 8–9/12 und KI-Spalte 3–4/12 rechts.
- Gegeben Viewport <1024px, dann erscheint KI-Bereich unter Hauptinhalt, weiterhin mit KI-Badge.
- Gegeben KI-Spalte, dann hat sie `{colors.ki-surface}` und Disclaimer.

**UX:** UX-DR1, UX-DR5  
**Architektur:** AD-7, Frontend Layout Architecture  
**Abhängigkeiten:** 2.1  
**Nicht enthalten:** KI-Daten laden  
**Tests:** Responsive CSS/Layout Test

---

## Epic 3 — Mock-Daten und Backend-Grundlage

### Story 3.1 — Domain-Modell und JPA

**Als** System **möchte ich** persistierte Projekt-Entitäten **damit** KPIs berechnet werden können.

**Akzeptanzkriterien:**
- Gegeben Flyway Start, wenn Migrationen laufen, dann existieren Tabellen für Projekte, Phasen, Meilensteine, Risiken, Budget/Aufwand.
- Gegeben Entitäten, dann nutzen PKs UUID.

**Architektur:** AD-3, UUID Convention  
**Abhängigkeiten:** 1.1  
**Nicht enthalten:** KPI-Logik, Seed-Daten  
**Tests:** Repository Integration Tests

---

### Story 3.2 — Flyway-Migrationen

**Als** Entwickler **möchte ich** versionierte DB-Schema-Migrationen **damit** das Schema reproduzierbar ist.

**Akzeptanzkriterien:**
- Gegeben leere DB, wenn App startet, dann wendet Flyway alle Migrationen an.
- Gegeben erneuter Start, dann sind Migrationen idempotent (kein erneutes Anlegen).

**Abhängigkeiten:** 3.1  
**Tests:** Flyway Migrate Test

---

### Story 3.3 — Mock-Seed ~20 Projekte (FR-19)

**Als** Demo-Nutzer **möchte ich** ein realistisches Mock-Portfolio **damit** der Pilot Szenarien zeigt.

**Akzeptanzkriterien:**
- Gegeben Seed ausgeführt, dann existieren ca. 20 Projekte.
- Gegeben Szenario-Typen, dann ist mindestens je ein Projekt: im Plan, Terminverzug, Budgetüberzug, offene Risiken, widersprüchliche Signale, abgeschlossen.
- Gegeben Seed, dann enthält er keine echten Kundennamen.

**Architektur:** FR-19, reproduzierbarer Seed  
**Abhängigkeiten:** 3.2  
**Tests:** Integration Test Zählung + Szenario-Abdeckung

---

### Story 3.4 — kpi.* Modul Grundstruktur

**Als** Architekt **möchte ich** ein isoliertes KPI-Modul **damit** Berechnungen zentral sind (AD-3).

**Akzeptanzkriterien:**
- Gegeben `kpi.*`, dann importiert es nicht `ai.*`.
- Gegeben KPI-Service, dann liefert er berechnete DTOs, keine Rohentitäten an API.
- Gegeben `kpi/reader/`, dann existieren Interface-Stubs `PortfolioKpiReader`, `ApprovedProjectDataReader`.

**Architektur:** AD-2, AD-3  
**Abhängigkeiten:** 3.1  
**Tests:** ArchUnit oder Package-Import-Test

---

### Story 3.5 — Basis-REST Projekte und Portfolio

**Als** Frontend **möchte ich** Basis-Endpunkte **damit** Listen geladen werden können.

**Akzeptanzkriterien:**
- Gegeben Mock-Daten, wenn `GET /api/projects`, dann Liste mit erweiterten Tabellenfeldern (FR-2) — mindestens UUID, Name, Kunde, Status.
- Gegeben UUID, wenn `GET /api/projects/{id}`, dann Projektdetails inkl. Stammdaten-Basis.
- Gegeben KPI-Responses, dann enthalten sie kein `aiGenerated`.

**Architektur:** AD-5  
**Abhängigkeiten:** 3.3, 3.4  
**Tests:** MockMvc Tests

---

### Story 3.6 — Domain-Erweiterung Stammdaten, Probleme, Snapshots

**Als** System **möchte ich** erweiterte Projekt-Stammdaten und getrennte Probleme **damit** Management-Analyse möglich ist (FR-5, FR-6, FR-21).

**Akzeptanzkriterien:**
- Gegeben Flyway V3, dann existieren Felder/Entitäten: `project_lead`, `last_data_update`, `predicted_end_date` `[OFFEN]`, `problems`, `project_report_snapshots` `[ASSUMPTION]`.
- Gegeben `problems`, dann getrennt von `risks` mit Mindestfeldern laut FR-6.
- Gegeben bestehende 3.1-Tabellen, dann bleiben additive Migration — kein Datenverlust.

**Architektur:** FR-5, FR-6, FR-21, PRD-Addendum Domain  
**Abhängigkeiten:** 3.2  
**Tests:** Flyway + Repository Integration Tests

---

### Story 3.7 — Mock-Seed Erweiterung

**Als** Demo-Nutzer **möchte ich** erweiterte Mock-Daten **damit** Tabellen, Insights und Berichtsvergleich demonstriert werden können.

**Akzeptanzkriterien:**
- Gegeben Seed V4, dann haben Projekte Projektleitung, Probleme, erweiterte Risiko-Felder (soweit modelliert).
- Gegeben Seed, dann existieren je Projekt 2 Berichtsstand-Snapshots (aktuell + vorherig) `[ASSUMPTION]`.
- Gegeben Seed, dann mindestens je ein Projekt pro Management-Insight-Typ `[OFFEN: Regeln]`.

**Architektur:** FR-19, FR-20, FR-21  
**Abhängigkeiten:** 3.6  
**Tests:** Integration Test Insight- + Snapshot-Abdeckung

---

## Epic 4 — Portfolio-KPI-Übersicht

### Story 4.1 — Portfolio-KPI-Berechnung API (FR-1, FR-9)

**Als** Führungskraft **möchte ich** aggregierte Portfolio-KPIs **damit** ich die Portfolio-Gesundheit sehe.

**Akzeptanzkriterien:**
- Gegeben Mock-Portfolio, wenn `GET /api/portfolio/kpis`, dann liefert: aktive Projekte, Ø-Fortschritt, Budgetabweichung, Termintreue, kritische Risiken, Statusverteilung (Zahlen).
- Gegeben leeres Portfolio, dann definierter Leerzustand in Response.
- Gegeben Response, dann stammen alle Werte aus `kpi.*`.

**Architektur:** AD-3  
**Abhängigkeiten:** 3.4, 3.5  
**Tests:** KPI Unit Tests + API Test

---

### Story 4.2 — kpi-card Komponente

**Als** Nutzer **möchte ich** einheitliche KPI-Karten **damit** Kennzahlen schnell erfassbar sind.

**Akzeptanzkriterien:**
- Gegeben KPI-Daten, wenn Karte rendert, dann zeigt sie Label, Wert, Einheit tabular-nums.
- Gegeben Karte, dann ist sie read-only und CGI-konform gestylt.

**UX:** UX-DR3, kpi-card  
**Abhängigkeiten:** 2.4  
**Tests:** Component Test Inputs/Outputs

---

### Story 4.3 — Portfolio KPI-Sektion

**Als** Führungskraft **möchte ich** KPI-Karten auf der Portfolio-Seite **damit** ich den Aha-Moment erlebe (NFR-1).

**Akzeptanzkriterien:**
- Gegeben Portfolio-Seite, wenn geladen, dann erscheinen KPI-Karten aus Backend-Daten innerhalb von 30s Gesamterlebnis.
- Gegeben API-Fehler KPI, dann Fehlerpanel nur im KPI-Bereich mit Retry.

**UX:** Portfolio Aufbau Punkt 4  
**Abhängigkeiten:** 4.1, 4.2, 2.2  
**Tests:** Component + API Integration

---

### Story 4.4 — Portfolio-Filter (FR-8)

**Als** Führungskraft **möchte ich** filtern **damit** ich das Portfolio eingrenze.

**Akzeptanzkriterien:**
- Gegeben Filter Kunde/Geschäftsbereich, Projektleitung, Ampelstatus, Phase, aktiv/abgeschlossen, Zeitraum, Risikostufe (FR-8), wenn gesetzt, dann aktualisieren KPI-API-Aufruf und folgende Views konsistent.
- Gegeben Mehrfachfilter, dann sind sie kombinierbar.
- Gegeben gefilterte Leermenge, dann Hinweis + „Filter zurücksetzen".

**UX:** filter-bar  
**Architektur:** Query-Parameter oder Request-Body Filter in API  
**Abhängigkeiten:** 4.1  
**Nicht enthalten:** KI-Trend-Filter (Epic 8)  
**Tests:** API Filter Tests; UI Filter Test

---

## Epic 5 — Portfolio-Zeitleiste und Projekttabelle

### Story 5.1 — Portfolio-Gantt API

**Als** Führungskraft **möchte ich** Zeitleisten-Daten **damit** ich Terminlage im Portfolio sehe.

**Akzeptanzkriterien:**
- Gegeben Portfolio, wenn `GET /api/portfolio/timeline`, dann je Projekt: Name, Start, Ende, Phasen, Meilensteine, Plan-Ist-Abweichung, Status (Wort).
- Gegeben Response, dann berechnet in `kpi.*`.

**Architektur:** Gantt-Anforderungen Architecture Spine  
**Abhängigkeiten:** 3.4  
**Tests:** API + KPI Tests

---

### Story 5.2 — Gantt-Timeline Komponente

**Als** Nutzer **möchte ich** eine Gantt-artige Zeitleiste **damit** ich Verzüge erkenne.

**Akzeptanzkriterien:**
- Gegeben Timeline-Daten, wenn Komponente rendert, dann eine Zeile pro Projekt mit Zeitachse, heute-Marker, Legende.
- Gegeben Tastatur, dann ist horizontales Scrollen bedienbar.
- Gegeben Screenreader, dann existiert sr-only-Zusammenfassung der Zeilen.

**UX:** UX-DR8, gantt-timeline; **kein Donut**  
**Abhängigkeiten:** 5.1, 2.4  
**Nicht enthalten:** Chart-Bibliothek  
**Tests:** Component A11y Test

---

### Story 5.3 — Portfolio-Projekttabelle (FR-2)

**Als** Führungskraft **möchte ich** alle Projekte in einer Management-Tabelle **damit** ich Projekte vergleiche und Kritisches erkenne.

**Akzeptanzkriterien:**
- Gegeben Portfolio, wenn Tabelle lädt, dann mindestens Spalten: Name, Kunde/Geschäftsbereich, Projektleitung, Status (Badge), Phase, Fortschritt, geplantes Enddatum, prognostiziertes Enddatum (wenn berechenbar), Terminabweichung, Budget %, Budgetabweichung, Aufwandsabweichung, offene Risiken, kritische Risiken/Probleme, letzte Aktualisierung.
- Gegeben schmaler Viewport, dann horizontales Scrollen der Tabelle.
- Gegeben Sortierung, dann mindestens nach: Ampelstatus, Fortschritt, Terminabweichung, Budgetabweichung, kritische Risiken, letzte Aktualisierung.
- Gegeben Zeilenklick, dann Navigation zu Projekt-Detail (FR-7).

**UX:** project-table, status-badge  
**Abhängigkeiten:** 3.5, 3.7, 2.4  
**Tests:** Table Component Test; API Feld-Abdeckung

---

### Story 5.4 — Trend- und Statusvisualisierung (FR-3)

**Als** Führungskraft **möchte ich** Verlauf und Statusverteilung **damit** ich Trends erkenne.

**Akzeptanzkriterien:**
- Gegeben Portfolio, wenn Visualisierungen laden, dann mindestens Fortschrittstrend und Budgetentwicklung mit beschrifteten Achsen.
- Gegeben Statusverteilung, dann als **Zahlenzeile oder Balken mit Labels**, kein Donut.
- Gegeben Segment-Umschalter 3M/6M/12M, dann filtert nur die Darstellung.

**UX:** UX-DR14 (kein Donut), trend-chart  
**Abhängigkeiten:** 4.1  
**Tests:** Component + sr-only Summary

---

### Story 5.5 — Navigation Tabelle → Detail (FR-7)

**Als** Nutzer **möchte ich** per Zeilenklick ins Projekt **damit** ich Details sehe.

**Akzeptanzkriterien:**
- Gegeben Tabellenzeile, wenn Klick oder Enter, dann Navigation zu `/projects/{uuid}`.
- Gegeben Navigation, dann bleibt Filterzustand in Session/Service erhalten.

**UX:** project-table-row Verhalten  
**Abhängigkeiten:** 5.3, 2.2  
**Tests:** Router Navigation Test

---

## Epic 6 — Projekt-Detailseite

### Story 6.1 — Projekt-KPI API (FR-5, FR-9)

**Als** Projektleiter **möchte ich** Management-Kernkennzahlen **damit** ich Projektstatus beurteile.

**Akzeptanzkriterien:**
- Gegeben Projekt-UUID, wenn `GET /api/projects/{id}/kpis`, dann: Status, Fortschritt, Phase, Zeitverbrauch, Terminabweichung, prognostiziertes Enddatum (wenn berechenbar), Budget Plan/Ist/Verbrauch/Abweichung/Rest/Hochrechnung, Aufwand Plan/Ist/Abweichung/Rest, offene und kritische Risiken/Probleme — Plan/Ist/Prognose getrennt.
- Gegeben Response, dann ausschließlich `kpi.*`-berechnet.

**Abhängigkeiten:** 3.4, 3.6  
**Tests:** API Test + KPI Unit Tests

---

### Story 6.2 — Projekt-Stammdaten und Header (FR-5)

**Als** Nutzer **möchte ich** Stammdaten und ausgeschriebenen Status **damit** ich Projektkontext habe.

**Akzeptanzkriterien:**
- Gegeben Detailseite, dann Breadcrumb, Projektname, Projekt-ID, Kunde/Geschäftsbereich, Projektleitung, Start-/Plan-/Prognose-Enddatum, Phase, status-badge, letzte Datenaktualisierung.
- Gegeben `GET /api/projects/{id}/master-data`, dann Stammdaten-Felder.

**UX:** Projekt-Detail Punkt 1  
**Abhängigkeiten:** 6.1, 2.3  
**Tests:** Component + API Test

---

### Story 6.3 — Projekt-KPI-Karten und Budget/Aufwand (FR-5)

**Als** Nutzer **möchte ich** KPI-Karten und Budget/Aufwand **damit** ich Plan/Ist/Prognose verstehe.

**Akzeptanzkriterien:**
- Gegeben KPI-Daten, dann kpi-cards für Management-KPIs mit klarer Plan/Ist/Prognose-Kennzeichnung.
- Gegeben Budget/Aufwand, dann tabellarisch oder KPI-Karten mit Restwerten und Hochrechnung (deterministisch).

**UX:** Punkte 2, 6  
**Abhängigkeiten:** 6.1, 4.2  
**Tests:** Component Test

---

### Story 6.4 — Projekt-Phasen und Meilensteine (FR-5)

**Als** Nutzer **möchte ich** Phasen und Meilensteine **damit** ich Terminplan und Verzüge verstehe.

**Akzeptanzkriterien:**
- Gegeben Projekt, dann gantt-timeline mit Phasen/Meilensteinen: Bezeichnung, Status, Planbeginn/-ende, Ist/Prognose, Abweichung, Blockaden `[OFFEN]`, Überfällig-Markierung.
- Gegeben sr-only, dann textliche Zusammenfassung inkl. überfälliger Meilensteine.
- Gegeben API, dann `GET /api/projects/{id}/phases` liefert strukturierte Daten aus `kpi.*`.

**UX:** Punkt 5  
**Abhängigkeiten:** 5.2, 6.1, 3.6  
**Tests:** Component + API Test

---

### Story 6.5 — Zurück-Navigation mit Filter (FR-7)

**Als** Nutzer **möchte ich** zurück zum Portfolio **damit** mein Filter erhalten bleibt.

**Akzeptanzkriterien:**
- Gegeben gesetzte Portfolio-Filter, wenn Detail → Portfolio zurück, dann sind Filter unverändert.
- Gegeben Breadcrumb/Zurück, dann gleiches Verhalten.

**Abhängigkeiten:** 5.5, 4.4  
**Tests:** E2E oder Service Test Filter-State

---

### Story 6.6 — Management Insights API und UI (FR-20)

**Als** Führungskraft **möchte ich** deterministische Auffälligkeiten **damit** ich verstehe, warum ein Projekt kritisch ist.

**Akzeptanzkriterien:**
- Gegeben Projekt, wenn `GET /api/projects/{id}/insights`, dann Liste mit: Aussage, Kennzahlen, Vergleichswert `[OFFEN]`, Zeitraum, Begründung, Typ „deterministisch".
- Gegeben Insight-Regeln, dann Implementierung in `kpi.insights` — Schwellenwerte `[OFFEN]`.
- Gegeben Codebase, dann bleibt `app-project-insights-section` erhalten (Loading/Error/Empty).
- **Produktstand:** Die Insight-UI wird auf der Projekt-Detailseite **nicht mehr gerendert**; FR-20 ist aktuell nicht sichtbar im MVP-UI. API und Regel-Engine bleiben für KI/ spätere Nutzung bestehen.

**UX:** insight-list (nicht aktiv auf Detailseite)  
**Abhängigkeiten:** 3.6, 3.7, 6.1  
**Tests:** KPI Rule Tests + API Test (`ProjectInsightEngineTest`, `GET /insights`)

**Change Log (2026-07-16):** Management-Insights-UI aus fachlicher/UX-Entscheidung aus der Projekt-Detailseite entfernt.

---

### Story 6.7 — Berichtsstandsvergleich (FR-21)

**Als** Führungskraft **möchte ich** Entwicklung seit dem letzten Berichtsstand **damit** ich Veränderungen erkenne.

**Akzeptanzkriterien:**
- Gegeben Snapshots, wenn `GET /api/projects/{id}/trends`, dann Deltas für Fortschritt, Budgetverbrauch, Terminprognose, Ampelstatus, Risiken.
- Gegeben fehlender Vorstand, dann definierter UI-Hinweis — kein erfundener Vergleich.
- Gegeben UI, dann `app-project-report-comparison` als **eigenständiger Faktenbereich** auf der Detailseite (nicht unter Management Insights).

**UX:** report-comparison (eigenständige Section)  
**Abhängigkeiten:** 3.7, 6.1  
**Tests:** API Test mit 2 Snapshot-Seed

---

## Epic 7 — Risiken, Probleme und Maßnahmen

### Story 7.1 — Risiken API (FR-6)

**Als** System **möchte ich** Risiken bereitstellen **damit** sie angezeigt werden.

**Akzeptanzkriterien:**
- Gegeben Projekt, wenn `GET /api/projects/{id}/risks`, dann Liste mit Mindestfeldern FR-6 (Titel, Beschreibung, Wahrscheinlichkeit, Auswirkung, Schweregrad, Status, Verantwortlichkeit, Gegenmaßnahme, Fälligkeit — soweit modelliert).
- Gegeben Response, dann Anzahl offener/kritischer Risiken aggregierbar für Tabelle und KPIs.

**Implementierungsstand:** Kein separater `/risks`-Endpunkt; offene Risiken werden über `GET /api/projects/{id}/issues-actions` (Typ `RISK`) und KPI-Aggregation bereitgestellt.

**Abhängigkeiten:** 3.6, 3.5  
**Tests:** API Test (`issues-actions`, KPI-Reader)

---

### Story 7.2 — Risiken-UI (FR-6)

**Als** Projektleiter **möchte ich** Risiken sehen **damit** ich Maßnahmen einordnen kann.

**Akzeptanzkriterien:**
- Gegeben Detailseite, dann Risiko-Liste/Karten getrennt von Problemen.
- Gegeben Eintrag, dann status-badge für Schwere.

**Implementierungsstand:** Kombinierte Section `app-project-issues-actions-section` („Probleme, Risiken & Maßnahmen“) statt separater Risiko-Liste.

**UX:** Punkt 7 (Risiken) — kombiniert mit Problemen  
**Abhängigkeiten:** 7.1, 6.2  
**Tests:** Component Test (`project-issues-actions-section.component.spec.ts`)

---

### Story 7.4 — Probleme API und UI (FR-6)

**Als** Projektleiter **möchte ich** eingetretene Probleme getrennt von Risiken sehen **damit** ich den Ist-Zustand verstehe.

**Akzeptanzkriterien:**
- Gegeben Projekt, wenn `GET /api/projects/{id}/problems`, dann Liste mit FR-6 Mindestfeldern.
- Gegeben Detailseite, dann Problem-Bereich getrennt vom Risiko-Bereich.
- Gegeben UI, dann kein Maßnahmen-Workflow (Anzeige only).

**Implementierungsstand:** Kein separater `/problems`-Endpunkt; Probleme über `GET /api/projects/{id}/issues-actions` (Typ `PROBLEM`); UI kombiniert in derselben Section.

**Abhängigkeiten:** 3.6, 7.2  
**Tests:** API + Component Test

---

### Story 7.3 — Status-Badge konsistent (FR-10, NFR-6)

**Als** Nutzer mit Sehschwäche **möchte ich** Status als Wort + Farbe **damit** ich Status verstehe.

**Akzeptanzkriterien:**
- Gegeben status-badge überall, dann Punkt + Wort, nie nur Farbe.
- Gegeben Kontrast, dann WCAG 2.1 AA für Badge-Text.

**UX:** UX-DR4, status-badge  
**Abhängigkeiten:** 4.2  
**Tests:** A11y Kontrast-Check, `status-badge.component.spec.ts`

---

### Story 7.5 — Team- und Kapazitätssicht (MVP-Scope-Erweiterung)

**Als** Projektleiter **möchte ich** projektbezogene Rollen- und Kapazitätsübersicht **damit** ich Terminwirkungen aus Ressourcenlücken erkenne.

**Akzeptanzkriterien:**
- Gegeben Detailseite, dann Section „Team & Kapazität“ mit projektbezogenen Rollen und Skills.
- Gegeben Rolle, dann Besetzungsgrad in Prozent und Fortschrittsbalken.
- Gegeben Zusammenfassung, dann fehlende Kapazitäten/FTE, nächste Verfügbarkeit, überlastete Rollen, externe Optionen, Auswirkung auf den Projekttermin.
- Gegeben Darstellung, dann **keine sensiblen Personaldetails** (keine Namen, Krankheiten o. Ä.).
- Gegeben API, wenn `GET /api/projects/{id}/capacity`, dann Rollenliste und Kapazitäts-Summary aus Backend/Mock-Seed.
- Gegeben UI, dann Loading-, Error- und Empty-State; responsive Darstellung.

**UX:** `app-project-team-capacity-section`  
**Abhängigkeiten:** 3.6, 6.2  
**Tests:** API Test + Component Test (`project-team-capacity-section.component.spec.ts`)

---

## Epic 8 — KI-Portfolioanalyse

### Story 8.1 — PortfolioKpiReader für AI (FR-13, FR-14)

**Als** KI-Modul **möchte ich** freigegebene Portfolio-DTOs **damit** ich keine JPA-Entities lese.

**Akzeptanzkriterien:**
- Gegeben `ai.*`, dann importiert es nur `kpi.reader.PortfolioKpiReader`.
- Gegeben Reader-Aufruf, dann DTOs ohne sensitive echte Daten.
- Gegeben `ai.*`, dann kein JPA-Repository-Import.

**Architektur:** AD-2, AD-4  
**Abhängigkeiten:** 3.4, 4.1  
**Tests:** ArchUnit / Import Test

---

### Story 8.2 — Portfolio-Trendanalyse Use Case + API (FR-4)

**Als** Führungskraft **möchte ich** KI-Trendtext **damit** ich Entwicklungen verstehe.

**Akzeptanzkriterien:**
- Gegeben Portfolio-Daten, wenn `GET /api/portfolio/ai/trend-analysis`, dann typisiertes Insight-Modell `{ insights[], aiGenerated, disclaimer, generatedAt }` (Course Correction 2026-07-17; ersetzt `{ text, topProjects }`).
- Gegeben Gemini-Ausfall, dann HTTP 503 mit `{ code, message }` — sofern keine deterministischen Muster vorliegen; sonst Fallback auf Detector-Insights.
- Gegeben Response, dann kein KPI-Fakt ohne Reader-Quelle; Insights ohne ≥2 Projekte/Belege werden verworfen.
- Aktive Mustertypen initial: `DETERIORATING_TREND`, `REPORTING_PATTERN`.

**Architektur:** AD-5  
**Abhängigkeiten:** 8.1  
**Tests:** Mock Gemini Client Test; Pattern-Detector Unit Tests

---

### Story 8.3 — Portfolio KI-Panel Frontend

**Als** Führungskraft **möchte ich** Trendanalyse in KI-Spalte **damit** Fakten und KI getrennt sind.

**Akzeptanzkriterien:**
- Gegeben Portfolio-Seite, wenn KI-Panel lädt, dann separater RxJS-Stream, unabhängig von KPI-Karten.
- Gegeben Insights, dann Titel „Portfolio-Muster und systemische Risiken“, max. 3–5 Karten, Belege eingeklappt.
- Gegeben Gemini 503 / AI-Fehler, dann feste DE-Fehlermeldung + Retry; KPI-Bereich unberührt (FR-15); nie „Failed to fetch“.

**UX:** ki-panel, UX-DR12  
**Architektur:** AD-7  
**Abhängigkeiten:** 8.2, 2.4  
**Tests:** Component Test isolierter Error

---

### Story 8.4 — Top-3 Handlungsbedarf in Trend (FR-4)

**Als** Führungskraft **möchte ich** Top-3-Projekte **damit** ich Prioritäten setze.

**Akzeptanzkriterien (Course Correction 2026-07-17):**
- Narrative Top-3-Liste ist **nicht** mehr das Zielmodell; ersetzt durch typisierte Portfolio-Insight-Karten mit betroffenen Projekten (≥2) und Belegen.
- Gegeben Insight-Karten, dann Klick auf Projektname navigiert zu Detail.

**Abhängigkeiten:** 8.2, 8.3  
**Tests:** API Response Structure Test; Panel filtert Einzelprojekt-Insights

---

## Epic 9 — KI-Projektanalyse und Q&A

### Story 9.1 — ApprovedProjectDataReader (FR-13)

**Als** KI-Modul **möchte ich** freigegebene Projektdaten **damit** Q&A kontrolliert ist.

**Akzeptanzkriterien:**
- Gegeben `ai.*`, dann nur `ApprovedProjectDataReader` für Projekt-KI.
- Gegeben Reader, dann DTO mit KPIs, **Management Insights**, Risiken, **Probleme**, Meilensteine — kein Entity.

**Architektur:** AD-2, AD-4  
**Abhängigkeiten:** 3.4, 6.1, 6.6, 7.1, 7.4  
**Tests:** Unit Test Reader Contract

---

### Story 9.2 — Summary und Forecast API (FR-11, FR-12)

**Als** Nutzer **möchte ich** KI-Zusammenfassung und Prognose **damit** ich Einschätzungen erhalte.

**Akzeptanzkriterien:**
- Gegeben Projekt, wenn `GET …/ai/summary` und `GET …/ai/forecast`, dann KI-Text mit Kennzeichnung; referenziert freigegebene Fakten/Insights (FR-14).
- Gegeben Prognose, dann ersetzt sie keine KPI-Zahlen; erklärt deterministische Hochrechnungen, erfindet sie nicht.

**Abhängigkeiten:** 9.1  
**Tests:** MockMvc + Mock Gemini

---

### Story 9.3 — Projekt-Q&A API (FR-16, FR-18, FR-14)

**Als** Projektleiter **möchte ich** Fragen stellen **damit** ich projektbezogene Einschätzungen erhalte.

**Akzeptanzkriterien:**
- Gegeben Projekt, wenn `POST …/ai/qa` mit Frage, dann Antwort nur zu diesem Projekt.
- Gegeben unzureichende Daten, dann Hinweis ohne erfundene KPIs.
- Gegeben Portfolio-Route, dann kein Q&A-Endpoint/UI.

**Abhängigkeiten:** 9.1  
**Tests:** API Test FR-18, FR-14 Szenarien

---

### Story 9.4 — KI-Panels Projekt-Detail (FR-10, FR-11, FR-12)

**Als** Nutzer **möchte ich** Summary, Prognose und Q&A in KI-Spalte **damit** alles an einem Ort ist.

**Akzeptanzkriterien:**
- Gegeben Detailseite, dann drei ki-panels mit unabhängigem Laden.
- Gegeben ein Panel-Fehler, dann andere Panels und Fakten weiter nutzbar.

**Produktstand (2026-07-16):** Backend (`/ai/analysis`, `/ai/questions`) und `app-project-ai-panel` sind auf der Detailseite **aktiv**. Maßnahmen nur als Vorschläge. Provider `mock` (Default) oder `gemini` via Env.

**UX:** Projekt-Detail KI-Bereich  
**Architektur:** AD-7  
**Abhängigkeiten:** 9.2, 9.3, 2.4  
**Tests:** Component Test (`project-ai-panel.component.spec.ts`); Detailseite aktiviert Panel (`project-detail-page.component.spec.ts`)

---

### Story 9.5 — Quick-Reply-Chips (FR-17)

**Als** Nutzer **möchte ich** vorformulierte Fragen **damit** ich schneller fragen kann.

**Akzeptanzkriterien:**
- Gegeben mindestens 3 Chips, wenn Klick, dann gleiche Pipeline wie Freitext.
- Gegeben Chip-Text, dann als KI-Anfrage erkennbar; Antwort als KI-Einschätzung.

**UX:** quick-reply-chip  
**Abhängigkeiten:** 9.3, 9.4  
**Tests:** Component Test Chip → API Call

---

## Epic 10 — Fehlerbehandlung, Barrierefreiheit, Qualität

### Story 10.1 — Unabhängiges Laden Fakten/KI (FR-15, AD-7)

**Als** Nutzer **möchte ich** KPIs auch bei KI-Ausfall **damit** ich weiterarbeiten kann.

**Akzeptanzkriterien:**
- Gegeben simulierter Gemini-Ausfall, wenn Portfolio/Detail offen, dann laden KPI, Tabelle, Filter, Gantt erfolgreich.
- Gegeben KI-Streams, dann separate Signals pro Panel (loading/error).

**Architektur:** AD-7  
**Abhängigkeiten:** 8.3, 9.4  
**Tests:** Integration Test mit Gemini Stub 503

---

### Story 10.2 — Graceful Degradation UI

**Als** Nutzer **möchte ich** verständliche Fehlermeldungen **damit** ich weiß, was passiert.

**Akzeptanzkriterien:**
- Gegeben KI-Fehler, dann Meldung + „Erneut versuchen", `aria-live="polite"`.
- Gegeben Fakten-Fehler, dann panel-spezifischer Retry.
- Gegeben kein leeres Panel ohne Hinweis.

**UX:** State Patterns Gemini/Fakten-Fehler  
**Abhängigkeiten:** 10.1  
**Tests:** Component A11y Test

---

### Story 10.3 — Barrierefreiheit WCAG 2.1 AA

**Als** Nutzer **möchte ich** zugängliche Bedienung **damit** ich das Dashboard nutzen kann.

**Akzeptanzkriterien:**
- Gegeben Tastatur, dann erreichbar: Navigation, Tabelle, Filter, Q&A.
- Gegeben Icon-Buttons, dann aria-label.
- Gegeben Captions, dann `{colors.caption}` (#333333).
- Gegeben `prefers-reduced-motion`, dann reduzierte Animationen.

**UX:** Accessibility Floor, UX-DR13, UX-DR15  
**Abhängigkeiten:** 2.1, 5.2, 5.4  
**Tests:** axe/lighthouse A11y Scan; manuelle Tastaturprüfung

---

### Story 10.4 — Screenreader-Zusammenfassungen Visualisierungen

**Als** Screenreader-Nutzer **möchte ich** Textalternativen **damit** ich Diagramm-Inhalte verstehe.

**Akzeptanzkriterien:**
- Gegeben trend-chart oder gantt-timeline, dann sr-only-Zusammenfassung oder versteckte Datentabelle.
- Gegeben Zusammenfassung, dann enthält sie Kernzahlen und Status in Worten.

**UX:** Medium-Finding aus UX-Validation geschlossen  
**Abhängigkeiten:** 5.2, 5.4  
**Tests:** DOM Test sr-only content
