---
name: KPI-Board
description: Visuelle Identität für cgi-kpi-dashboard — CGI-konformes Enterprise-Dashboard auf Basis CGI Experience Design System 19.0.0. Light-Mode, Desktop-first.
status: final
created: 2026-07-13
updated: 2026-07-14
sources:
  - ../../prds/prd-cgi-kpi-dashboard-2026-07-13/prd.md
  - ../../prds/prd-cgi-kpi-dashboard-2026-07-13/addendum.md
  - CGI UX Guidelines
  - CGI Color System
  - CGI Layout Guidelines
  - CGI Header and Navigation Guidelines
  - CGI Experience Design System 19.0.0
designSystem:
  package: cgi-sentry-angular-components-lib
  version: 19.0.0
  reference: frontend/vendor/cgi-sentry-angular-components-lib-19.0.0.tgz
colors:
  # CGI Primärfarben
  primary-darkest: '#200A58'
  primary: '#5236AB'
  primary-light: '#9E83F5'
  primary-lighter: '#CBC3E6'
  primary-lightest: '#E6E3F3'
  primary-surface: '#F2F1F9'
  # CGI Neutralfarben
  neutral-900: '#151515'
  neutral-700: '#333333'
  neutral-400: '#A8A8A8'
  neutral-200: '#E8E8E8'
  neutral-100: '#EFEFEF'
  neutral-50: '#F8F8F8'
  neutral-0: '#FFFFFF'
  # Semantische Farben (CGI)
  success: '#1AB977'
  warning: '#FFAC25'
  error: '#B00020'
  # KI-Ebene — abgeleitet aus CGI-Violett, klar von Status getrennt
  ki: '#5236AB'
  ki-surface: '#F2F1F9'
  ki-border: '#CBC3E6'
  ki-ink: '#200A58'
  # Status (Ampel) — semantisch + ausgeschrieben
  status-on: '#1AB977'
  status-on-surface: '#E8F8F2'
  status-on-ink: '#0F7A52'
  status-watch: '#FFAC25'
  status-watch-surface: '#FFF6E8'
  status-watch-ink: '#8A5E00'
  status-crit: '#B00020'
  status-crit-surface: '#FBECEF'
  status-crit-ink: '#8A0018'
  # Flächen & Text (Mapping auf CGI Neutral)
  app-background: '#F8F8F8'
  surface: '#FFFFFF'
  foreground: '#151515'
  foreground-muted: '#333333'
  caption: '#333333'
  border: '#E8E8E8'
  border-subtle: '#EFEFEF'
  # Diagramm-Serien
  chart-progress: '#5236AB'
  chart-budget: '#FFAC25'
  chart-plan: '#A8A8A8'
  # Phasen (monochrome Violett-Rampe)
  phase-analyse: '#E6E3F3'
  phase-konzept: '#CBC3E6'
  phase-umsetzung: '#9E83F5'
  phase-pilot: '#5236AB'
  phase-abschluss: '#200A58'
  overrun: '#B00020'
typography:
  # CGI EDS Typografie — konkrete Token-Namen folgen Paket-Integration
  display:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 28px
    fontWeight: '700'
  title:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 20px
    fontWeight: '600'
  section:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 16px
    fontWeight: '600'
  metric:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 26px
    fontWeight: '700'
  body:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 12px
    fontWeight: '600'
  overline:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 11px
    fontWeight: '700'
    letterSpacing: 0.06em
  caption:
    fontFamily: 'CGI Sans / System Sans'
    fontSize: 12px
    fontWeight: '400'
    lineHeight: '1.45'
    color: '{colors.caption}'
spacing:
  '1': 4px
  '2': 8px
  '3': 12px
  '4': 16px
  '5': 20px
  '6': 24px
  '8': 32px
  gutter: 24px
  page-padding: 24px
components:
  shell-top-nav: CGI Top Navigation
  shell-side-nav: CGI Side Navigation
  shell-breadcrumbs: CGI Breadcrumbs
  kpi-card: shared KPI card
  status-badge: redundant status indicator
  ki-panel: AI assessment panel
  trend-chart: labeled time series (no decorative donuts)
  gantt-timeline: HTML/CSS/SVG timeline
  project-table: CGI table pattern
  filter-bar: CGI filter controls
  quick-reply-chip: AI question chip
  chat: project Q&A thread
---

# KPI-Board — Design Spine

> Visuelle Identität für das cgi-kpi-dashboard. **CGI Experience Design System 19.0.0**, Light-Mode-only, Desktop-first. Referenz-Komposition (historisch): [`mockups/portfolio-und-projekt-detail.html`](mockups/portfolio-und-projekt-detail.html) — bei Konflikt gewinnt **diese Spine** und die CGI-Vorgaben.

## Brand & Style

KPI-Board ist ein **internes CGI-Management-Werkzeug**. Die visuelle Haltung ist **modern, hell, enterprise-tauglich**: neutrale Flächen dominieren, CGI Purple für primäre Aktionen, Auswahl und Fokus, sekundäre Violetttöne sparsam, großzügiger Weißraum, klare Hierarchie.

Zwei Gestaltungsprinzipien:

1. **Fakten sind neutral, KI ist markiert.** Deterministische KPIs auf weißen/neutralen Flächen. KI-Ebene mit eigener Kennzeichnung (Badge, `{colors.ki-surface}`, Disclaimer) — nie mit Status verwechselbar (FR-10).
2. **Signalfarbe ist sparsam und ausgeschrieben.** Status als Punkt + Wort („Auf Kurs / Beobachten / Kritisch"), nie als unbeschriftete Ampel oder dekorativer Donut.

**Verboten im MVP:** dekorative Donut-Charts, generische Tacho-Gauges, übermäßige Farbverläufe, unbeschriftete Statusampeln, bunte Vollflächen-Ampel.

## Colors

Verbindliche CGI-Palette (siehe Frontmatter `colors`).

- **Primary (`{colors.primary}` / `{colors.primary-darkest}`)** — primäre Aktionen, aktive Navigation, Fokus, Fortschrittsbalken. Nicht für Status.
- **KI (`{colors.ki}`, `{colors.ki-surface}`)** — ausschließlich KI-Ebene: Panel, Badge, Quick-Reply-Chips. Bedeutet „KI-Einschätzung, kein Fakt".
- **Status** — `{colors.status-on}` (Erfolg/Auf Kurs), `{colors.status-watch}` (Warnung/Beobachten), `{colors.status-crit}` (Fehler/Kritisch). Immer mit Wortlabel.
- **Neutral** — `{colors.app-background}`, `{colors.surface}`, `{colors.border}` für Flächen und Trenner.
- **Text** — `{colors.foreground}` primär, `{colors.foreground-muted}` sekundär, **`{colors.caption}` für Captions/Achsen/Legenden** (Kontrast-Fix: nicht `#A8A8A8` auf Weiß für Fließtext).

**Kontrast (WCAG 2.1 AA):** Body/Metrik `{colors.foreground}` auf `{colors.surface}` ≥ 4.5:1. Captions/Achsen nutzen `{colors.caption}` (`#333333`) auf Weiß ≥ 4.5:1. Status-Text nutzt `*-ink` auf `*-surface`. Fokusindikatoren sichtbar gegen `{colors.surface}`.

## Typography

CGI EDS Typografie (Details bei Paket-Integration). Ein Schriftsystem, tabellarische Zahlen (`font-variant-numeric: tabular-nums`) für KPIs und Tabellen.

| Rolle | Einsatz |
|---|---|
| `display` | Große Kennzahlen (sparsam) |
| `title` | Seiten- und Projekttitel |
| `section` | Kartenüberschriften |
| `metric` | KPI-Kartenwerte |
| `body` | Fließtext, KI-Texte, Tabellenzellen |
| `label` | Feld- und Statuslabels |
| `overline` | Bereichslabels (z. B. „BERECHNETE KENNZAHLEN") |
| `caption` | Hilfstext, Disclaimer, Achsen, Legende |

## Layout & Spacing

4er/8er-Spacing-Skala. Standard-Kartenabstand `{spacing.gutter}` (24px).

**Desktop (≥1200px):** CGI Shell mit Side Navigation · Hauptbereich **8–9/12 Spalten** · KI-Spalte **3–4/12 Spalten** rechts.

**Portfolio-Seitenaufbau (Hauptbereich, top-down):**
1. Seitentitel und Kontext
2. Filterbereich
3. KPI-Karten
4. Gantt-artige Portfolio-Zeitleiste
5. Projekte mit Handlungsbedarf (Top-3-Faktenblock + KI ergänzt in rechter Spalte)
6. Projekttabelle

**Projekt-Detail (Hauptbereich):**
1. Breadcrumb
2. Projekttitel und Metadaten
3. Ausgeschriebener Projektstatus
4. KPI-Karten
5. Projektzeitleiste mit Meilensteinen
6. Budget und Aufwand
7. Risiken und Maßnahmen

**Tablet/Mobil:** KI-Bereich **unter** Hauptinhalt; einklappbare Side Navigation.

## Application Shell (CGI EDS)

| Element | Quelle | Einsatz |
|---|---|---|
| Top Navigation | CGI EDS | App-Header, Kontext |
| Side Navigation | CGI EDS | Portfolio, Projekte |
| Navigation Content | CGI EDS | Seiteninhalt-Wrapper |
| Breadcrumbs | CGI EDS | Projekt-Detail, Navigation zurück |

Hauptnavigation: **Portfolio**, **Projekte**.

## Components

Visuelle Spezifikationen. Verhalten: `EXPERIENCE.md`.

| Komponente | Visuelle Spezifikation |
|---|---|
| `kpi-card` | Weiße Karte auf `{colors.surface}`, `{colors.border}`, dezenter Schatten. Label `{typography.label}`, Wert `{typography.metric}`, optional Delta `{typography.caption}`. |
| `status-badge` | Punkt + Wort auf neutraler/`status-*-surface`-Fläche. Farbe im Punkt und `*-ink`-Text. |
| `ki-panel` | `{colors.ki-surface}`, Rahmen `{colors.ki-border}`, Kopf-Badge „KI-Einschätzung", Gemini-Sublabel, Footer-Disclaimer kursiv `{typography.caption}`. |
| `trend-chart` | Beschriftete Achsen, Legende, Grid `{colors.border-subtle}`. **Kein dekorativer Donut.** Statusverteilung als **Zahlenzeile oder Balken mit Labels**. |
| `gantt-timeline` | Eine Zeile pro Projekt; Phasensegmente `{phase-*}`; heute-Linie `{colors.primary}`; Verzug schraffiert `{colors.overrun}`. |
| `project-table` | CGI-Tabellenmuster; Fortschritts-Spur `{colors.primary}`; Status-Badge; Hover `{colors.neutral-50}`. |
| `filter-bar` | CGI-Filter-Controls; Chips/Dropdowns für Kunde, Projekt, Zeitraum, Status. |
| `quick-reply-chip` | `{colors.ki-border}`-Rand, `{colors.ki}`-Text — gehört zur KI-Ebene. |
| `chat` | Nutzer- und KI-Blasen im KI-Panel; Senden-Button `{colors.primary}`. |

## Do's and Don'ts

| Do | Don't |
|---|---|
| CGI Purple für Interaktion und Fokus | Ad-hoc-Farben außerhalb CGI-Palette |
| KI klar gekennzeichnet in eigener Zone | KI und Fakten visuell vermischen (FR-10) |
| Status als Punkt + ausgeschriebenes Wort | Farbe allein als Statusinformation |
| Beschriftete Diagramme und Zeitleisten | Donut-, Tacho- oder Platzhalter-Charts |
| `{colors.caption}` für kleine Texte | `#A8A8A8` für Captions auf Weiß (Kontrast-Fail) |
| Ausreichend Weißraum | Enge, bunte KPI-Raster |

## Amendment 2026-07-14

Stack-Pivot von eigenständigem „Clear Executive"-Token-System (Blau/Violett) auf **CGI Experience Design System 19.0.0**. Historische Mockups behalten Referenzcharakter; Implementierung folgt CGI-Farben und Shell-Komponenten.
