---
name: KPI-Board
description: Experience Spine für cgi-kpi-dashboard — Informationsarchitektur, Verhalten, Zustände, Interaktion, Barrierefreiheit und Kern-Flows. Visuelle Identität: DESIGN.md (CGI EDS).
status: final
created: 2026-07-13
updated: 2026-07-15
sources:
  - ../../prds/prd-cgi-kpi-dashboard-2026-07-13/prd.md
  - ../../prds/prd-cgi-kpi-dashboard-2026-07-13/addendum.md
  - ../architecture/architecture-cgi-kpi-dashboard-2026-07-13/ARCHITECTURE-SPINE.md
---

# KPI-Board — Experience Spine

> Verhalten, IA und Flows. Visuelle Tokens: `DESIGN.md`. Referenz-Komposition (historisch): [`mockups/portfolio-und-projekt-detail.html`](mockups/portfolio-und-projekt-detail.html). Bei Konflikt gewinnen **Spines + CGI EDS-Vorgaben**.

## Foundation

Desktop-first Web-Dashboard (**Angular 20**, TypeScript, SCSS, **Angular Material**, **Angular CDK**, **CGI Experience Design System 19.0.0**). Primärer Kontext: großer Bildschirm, Meeting/Beamer. Interner Pilot mit Mock-Daten (~20 Projekte, 2–5 Nutzer); kein Auth im MVP (`AD-6`).

Tragendes Prinzip: **links/Hauptbereich berechnete Fakten, rechts (Desktop) KI-Einschätzung** — räumlich, farblich (`{colors.ki-surface}`) und mit Label getrennt. Auf Tablet/Mobil: KI **unter** dem Hauptinhalt, weiterhin klar gekennzeichnet. KPIs aus Backend; KI aus Gemini, immer als Einschätzung/Prognose (FR-10).

## Information Architecture

| Oberfläche | Erreichbar über | Zweck |
|---|---|---|
| Portfolio-Übersicht | App-Start / Side Nav „Portfolio" | KPI-Karten, Gantt-Zeitleiste, **Management-Projekttabelle** (FR-2), Filter + KI-Trendanalyse (UJ-1) |
| Projekt-Detailansicht | Tabellenzeile / Side Nav „Projekte" | Stammdaten, Management-KPIs, **Management Insights**, Phasen/Meilensteine, Budget/Aufwand, Risiken/Probleme getrennt, Berichtsstandsvergleich + KI (UJ-2) |

**Anwendungshülle:** CGI Top Navigation · CGI Side Navigation · CGI Navigation Content · CGI Breadcrumbs (Projekt-Detail).

Navigation Portfolio ↔ Projekt (FR-7); Zurück erhält Filterzustand `[ASSUMPTION]`. Kein Portfolio-Q&A (FR-18).

## Page Composition

### Portfolio-Seite (Hauptbereich + KI-Spalte)

1. Header und Navigation (CGI Shell)
2. Seitentitel und Kontext
3. Filterbereich
4. KPI-Karten (FR-1)
5. Gantt-artige Portfolio-Zeitleiste (FR-3, FR-5 analog)
6. Projekte mit Handlungsbedarf (Fakten-Top-3; KI-Trend ergänzt in rechter Spalte, FR-4)
7. **Management-Projekttabelle** (FR-2) — sortierbar, klick → Detail
8. **KI-Spalte rechts:** Portfolio-Trendanalyse (FR-4)

**NFR Detail-Erfassbarkeit:** Die wichtigsten Fakten (Ampel, Fortschritt, Top-Insights, kritische Risiken) müssen auf Desktop **ohne Scrollen** oder innerhalb weniger Sekunden erfassbar sein `[ASSUMPTION: Above-the-fold auf ≥1200px]`.

### Projekt-Detailseite (Informationshierarchie — FR-5, FR-6, FR-20, FR-21)

1. **Projektkopf und Gesamtstatus** — Name, ID, Kunde/Geschäftsbereich, Projektleitung, Phase, Ampel, letzte Aktualisierung
2. **Zentrale KPI-Karten** — Plan/Ist/Prognose klar getrennt (FR-5)
3. **Management Insights** — deterministische Auffälligkeiten mit Begründung (FR-20)
4. **Berichtsstandsvergleich** — Delta zu vorherigem Stand, sofern vorhanden (FR-21)
5. **Phasen und Meilensteine** — beschriftete Gantt/Timeline (FR-5)
6. **Budget und Aufwand** — Plan, Ist, Rest, Hochrechnung (FR-5)
7. **Risiken und Probleme** — getrennte Bereiche (FR-6)
8. **KI-Spalte (abgegrenzt):** Zusammenfassung (FR-11), Prognose (FR-12), Q&A (FR-16, FR-17)

## Voice and Tone

- Formelles **„Sie"**.
- KI als **Einschätzung**, nie Anweisung.
- Status in Worten: **„Auf Kurs / Beobachten / Kritisch"**.

## Component Patterns

| Komponente | Einsatz | Verhaltensregeln |
|---|---|---|
| `kpi-card` | Beide | Read-only Backend-Werte. Delta optional Vorperiode. Nicht klickbar als Filter. |
| `status-badge` | Tabelle, Header, Risiken | Punkt + Wort; redundant (Farbe + Text) für A11y. |
| `project-table` | Portfolio | Management-Tabelle (FR-2). Zeilenklick → Detail (FR-7). Sortierbar: Status, Fortschritt, Terminabweichung, Budgetabweichung, kritische Risiken, letzte Aktualisierung. Horizontal scroll auf schmalen Viewports. |
| `insight-list` | Projekt-Detail | Deterministische Management Insights (FR-20). Fakt-Badge, Aussage + Kennzahlen + Begründung. |
| `report-comparison` | Projekt-Detail | Berichtsstandsvergleich (FR-21). Delta oder Hinweis bei fehlendem Vorstand. |
| `trend-chart` | Portfolio, Detail | Beschriftete Achsen/Legende. Segment 3M/6M/12M nur Visualisierung. **Kein Donut.** Statusverteilung als Zahlen/Balken mit Labels. |
| `gantt-timeline` | Portfolio, Detail | Phasen/Meilensteine; heute-Marker; Plan/Ist/Prognose; Überfällig; Legende; sr-only; Tastatur. |
| `ki-panel` | Rechte Spalte / unter Hauptinhalt | Nur KI; Badge + Disclaimer; **eigenständiges Laden** via separater API-Subscription. |
| `filter-bar` | Portfolio | Kunde/Geschäftsbereich, Projektleitung, Ampelstatus, Phase, aktiv/abgeschlossen, Zeitraum, Risikostufe (FR-8). Aktualisiert Fakten **und** KI-Trend konsistent. |
| `quick-reply-chip` | Detail-Q&A | Gleiche Gemini-Pipeline wie Freitext (FR-17). |
| `chat` | Detail-Q&A | Nur aktuelles Projekt (FR-16). Enter sendet. |
| `button-primary` | Q&A Senden, Retry | Disabled während Request; `{colors.primary}`. |
| CGI Shell | Global | Top Nav, Side Nav, Breadcrumbs; Side Nav einklappbar auf Tablet. |

## State Patterns

| Zustand | Oberfläche | Behandlung |
|---|---|---|
| Kaltstart / Laden | Beide | Skeleton in Fakten; KI-Panel eigener Ladezustand („Analyse wird erstellt …"). |
| Leeres Portfolio | Portfolio | Definierter Leerzustand `[OFFEN: UX-Text]`. |
| Gefilterte Leermenge | Portfolio | „Keine Projekte für diesen Filter" + „Filter zurücksetzen". |
| **Gemini nicht erreichbar** | KI-Panel | Fehlermeldung + „Erneut versuchen"; `aria-live="polite"`; Fokus auf Retry optional. **Fakten unberührt** (FR-15). |
| **Fakten-API-Fehler** | Fakten-Bereich | Betroffenes Panel mit Fehler + Retry; andere Fakten-Bereiche weiter nutzbar wo möglich. |
| Unzureichende Datengrundlage | KI | Hinweis ohne erfundene Werte (FR-14). |
| Q&A wartet | Detail | Eingabe disabled; Frage im Verlauf sichtbar. |

## Interaction Primitives

- Maus-primär, Desktop-Management-Kontext.
- Tabellenzeile klickbar; Enter öffnet Detail; Breadcrumb/Zurück mit Filter-Persistenz.
- Filter aktualisieren Fakten und KI-Trendanalyse.
- Q&A: Chip oder Freitext + Senden.
- **`prefers-reduced-motion`:** Animationen reduzieren/deaktivieren.
- **Verboten:** KI ändert Daten (FR-13); Portfolio-Q&A (FR-18); Frontend-KPI-Berechnung (FR-9); direkter Gemini-Aufruf aus Angular.

## Accessibility Floor

Ziel: **WCAG 2.1 AA**.

- Status nie nur über Farbe (Punkt + Wort).
- KI-Bereiche als Landmark/Region „KI-Einschätzung".
- Tab-Reihenfolge: Navigation → Fakten → KI.
- Sichtbare Fokusindikatoren (CGI/CDK Standard).
- Tabelle per Tastatur; Enter öffnet Detail.
- Q&A: `aria-live` für neue Antworten.
- Diagramme/Zeitleiste: textliche Achsen/Legende + **sr-only-Datenzusammenfassung** oder versteckte Datentabelle.
- Icon-Buttons: `aria-label`.
- Captions/Achsen: `{colors.caption}` (#333333), nicht `#A8A8A8`.

## Responsive & Platform

| Breite | Verhalten |
|---|---|
| Desktop (≥1200px) | Side Nav + Haupt 8–9/12 + KI 3–4/12 rechts |
| Tablet | Side Nav einklappbar; KI schmaler oder unter Hauptinhalt |
| Schmal (<1024px) | KI **unter** Hauptinhalt; Filter gestapelt; Tabelle horizontal scrollbar |

Kein natives Mobile-App-Ziel.

## Frontend Architecture (Experience-relevant)

- Feature-Module: `portfolio`, `project`, `ai` — lazy loaded.
- API-Services in `core/api`; **kein HttpClient in Präsentationskomponenten**.
- **RxJS** für Server-Streams; **Signals** für Loading/Error pro Panel.
- Getrennte Subscriptions Fakten vs. KI — kein gekoppelter Error-State (AD-7).

## Key Flows

### Flow 1 — UJ-1: Portfolio vor Steering

1. Portfolio öffnen → Fakten laden (KPIs, Gantt, Tabelle).
2. KPI-Reihe und Zeitleiste erfassen; Statusverteilung als **beschriftete Zahlen/Balken** (kein Donut).
3. Rechts KI-Trendanalyse + Top 3 (FR-4).
4. Optional filtern; alles konsistent aktualisiert.
5. Zeile klicken → Projekt-Detail.

**Failure:** Gemini down → KI-Fehlerpanel; Fakten nutzbar (FR-15).

### Flow 2 — UJ-2: Projekt vertiefen + Q&A

1. Detail: Kopf, KPIs, **Insights**, ggf. Berichtsvergleich.
2. Phasen/Meilensteine, Budget/Aufwand, Risiken/Probleme.
3. KI-Summary, Prognose, Q&A — referenzieren sichtbare Fakten/Insights (FR-14).

## Amendment 2026-07-15

- Fokus: **nachvollziehbare Einzelprojekt-Analyse** für Führungskräfte (FR-5/6/20/21 erweitert).
- Projekt-Detail-Hierarchie: Insights vor Phasen/Budget; Risiken/Probleme getrennt.
- Neue Komponenten: `insight-list`, `report-comparison`; erweiterte `project-table`, `filter-bar`.

- Stack: React/Vite/Recharts → **Angular 20 + CGI EDS**.
- Donut-Charts **entfernt**; Statusverteilung als beschriftete Alternativen.
- TanStack Query → **RxJS + Signals**.
- CGI Shell-Komponenten dokumentiert.
- Kontrast-Fix: Captions auf `{colors.caption}`.
