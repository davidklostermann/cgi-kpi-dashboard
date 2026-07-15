---
name: KPI-Board
description: Experience Spine für cgi-kpi-dashboard — Informationsarchitektur, Verhalten, Zustände, Interaktion, Barrierefreiheit und Kern-Flows. Visuelle Identität: DESIGN.md (CGI EDS).
status: final
created: 2026-07-13
updated: 2026-07-14
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
| Portfolio-Übersicht | App-Start / Side Nav „Portfolio" | KPI-Karten, Gantt-Zeitleiste, Handlungsbedarf, Projekttabelle + KI-Trendanalyse (UJ-1) |
| Projekt-Detailansicht | Tabellenzeile / Side Nav „Projekte" | Kernkennzahlen, Meilenstein-Zeitleiste, Budget/Aufwand, Risiken + KI-Summary, Prognose, Q&A (UJ-2) |

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
7. Projekttabelle (FR-2)
8. **KI-Spalte rechts:** Portfolio-Trendanalyse (FR-4)

### Projekt-Detailseite

1. Breadcrumb
2. Projekttitel und Metadaten
3. Ausgeschriebener Projektstatus
4. KPI-Karten (FR-5)
5. Projektzeitleiste mit Meilensteinen
6. Budget und Aufwand
7. Risiken und Maßnahmen (FR-6)
8. **KI-Spalte:** Zusammenfassung (FR-11)
9. **KI-Spalte:** Prognose (FR-12)
10. **KI-Spalte:** projektbezogenes Q&A (FR-16, FR-17)

## Voice and Tone

- Formelles **„Sie"**.
- KI als **Einschätzung**, nie Anweisung.
- Status in Worten: **„Auf Kurs / Beobachten / Kritisch"**.

## Component Patterns

| Komponente | Einsatz | Verhaltensregeln |
|---|---|---|
| `kpi-card` | Beide | Read-only Backend-Werte. Delta optional Vorperiode. Nicht klickbar als Filter. |
| `status-badge` | Tabelle, Header, Risiken | Punkt + Wort; redundant (Farbe + Text) für A11y. |
| `project-table` | Portfolio | Zeilenklick → Detail (FR-7). Sortierbar `[OFFEN: Spalten]`. Horizontal scroll auf schmalen Viewports. |
| `trend-chart` | Portfolio, Detail | Beschriftete Achsen/Legende. Segment 3M/6M/12M nur Visualisierung. **Kein Donut.** Statusverteilung als Zahlen/Balken mit Labels. |
| `gantt-timeline` | Portfolio, Detail | Zeile pro Projekt/Phase; heute-Marker; Plan-Ist-Abweichung; Legende; sr-only-Zusammenfassung; Tastatur + horizontales Scrollen. |
| `ki-panel` | Rechte Spalte / unter Hauptinhalt | Nur KI; Badge + Disclaimer; **eigenständiges Laden** via separater API-Subscription. |
| `filter-bar` | Portfolio | Kunde, Projekt, Zeitraum, Ampelstatus (FR-8). Aktualisiert Fakten **und** KI-Trend konsistent. |
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

1. Detail: Status, KPIs, Zeitleiste, Budget, Risiken.
2. KI-Summary und Prognose lesen.
3. Quick-Reply oder Freitext → Gemini via Backend.
4. Antwort bezieht sich auf freigegebene Daten (FR-14).

## Amendment 2026-07-14

- Stack: React/Vite/Recharts → **Angular 20 + CGI EDS**.
- Donut-Charts **entfernt**; Statusverteilung als beschriftete Alternativen.
- TanStack Query → **RxJS + Signals**.
- CGI Shell-Komponenten dokumentiert.
- Kontrast-Fix: Captions auf `{colors.caption}`.
