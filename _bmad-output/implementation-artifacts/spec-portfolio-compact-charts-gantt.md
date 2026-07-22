---
title: 'Portfolio-Layout ohne Seitenüberlauf und mit kompaktem Gantt'
type: 'bugfix'
created: '2026-07-22'
status: 'done'
review_loop_iteration: 0
baseline_commit: 'b562c1c530cf7409f0e3ee5c1a46c3c3ae1bd5d4'
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Die Portfolioseite kann bei mittleren Desktopbreiten horizontal überlaufen, weil die Shell- und Host-Breitenkette nicht vollständig schrumpfbar ist und die Statuszeile einer Trendkarte zu große Mindestbreiten besitzt. Die Portfolio-Zeitleiste zeigt mit bis zu 544 px zu viele Zeilen gleichzeitig und nimmt damit unverhältnismäßig viel vertikalen Platz ein.

**Approach:** Die vorhandenen Trend- und Gantt-Komponenten bleiben daten- und funktionsgleich. Ihre Layoutgrenzen, responsiven Umbrüche und die Breitenkette werden gezielt korrigiert, damit Charts und Gantt innerhalb des Portfolioinhalts scroll- bzw. umbrechbar sind, ohne die Projektansicht zu verändern.

## Boundaries & Constraints

**Always:** Drei Trend-/Statuskarten auf breiten Desktopinhalten in einer Reihe; bei knapperer Breite sinnvoller 2+1- und mobil einspaltiger Umbruch. SVGs behalten `440 × 160`-Geometrie, Achsen, Tooltips, deutsche Formate und 3M/6M/12M bei. Das Portfolio-Gantt bleibt intern horizontal und vertikal scrollbar, mit synchronisierten Namen, sticky Kopf, heute-/Phasen-/Verzugs- und Übergabemarkern. Die Scrollregion bleibt tastaturfokussierbar und screenreaderbeschrieben. Die Projekt-Gantt-Geometrie, Portfolio-AI-Drawer, Filter, Tabelle, Daten- und API-Logik bleiben erhalten.

**Ask First:** Änderungen an Datenmodellen, API-Aufrufen, fachlicher KPI-Berechnung, Routing, der Projekt-Gantt-Variante oder der Interaktion des KI-Drawers.

**Never:** Backend, Security, AI-Provider, Seed-Daten oder CGI-Farbwelt ändern; Abhängigkeiten hinzufügen; globales `overflow-x: hidden` als alleinige Fehlerbehebung einsetzen; Referenzdateien blind übernehmen.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Breiter Desktop | Portfolioinhalt oberhalb der responsiven Schwelle | Drei Karten bleiben innerhalb der Inhaltsbreite sichtbar; SVGs sind kompakt. | N/A |
| Mittlere Breite | Inhalt unterschreitet die sichere Dreispaltenbreite | Trends umbrechen zu 2+1; Statusinhalte erzeugen keinen Seitenüberlauf. | N/A |
| Breite Zeitachse | Gantt-Canvas ist breiter als der rechte Bereich | Nur die Timeline erhält horizontalen Scroll; die Seite wird nicht breiter. | N/A |
| Viele Projekte | Mehr Zeilen als der sichtbare Portfolio-Gantt-Rahmen | Namen und Timeline scrollen vertikal synchron, Köpfe bleiben sticky. | N/A |
| Projektansicht | `variant="project"` | Bestehende, höhere Projektzeilen und unbegrenzte Projekt-Gantt-Höhe bleiben wirksam. | N/A |

</frozen-after-approval>

## Code Map

- `frontend/src/app/core/layout/app-shell.component.scss` -- flexibler Hauptinhalt unter `mat-sidenav-content`; muss die Breite von Kindern schrumpfen lassen.
- `frontend/src/app/shared/components/trend-chart.component.scss` -- 12-Spalten-Kartenraster, SVG-Höhe und Statuszeilen-Mindestbreiten samt Breakpoints.
- `frontend/src/app/shared/components/trend-chart.component.spec.ts` -- Regressionen für drei Panels, SVG-Geometrie und Zeitraumwechsel.
- `frontend/src/app/shared/components/gantt-timeline.component.ts` -- Portfolio-/Projektvariante und vertikale Scroll-Synchronisierung.
- `frontend/src/app/shared/components/gantt-timeline.component.scss` -- Rahmenhöhe, interne Scrollbereiche, Canvas und Variantenregeln.
- `frontend/src/app/shared/components/gantt-timeline.component.spec.ts` -- Komponententests für Geometrie, Scroll-Sync und Varianten.
- `frontend/src/app/features/portfolio/portfolio-page.component.{scss,spec.ts}` -- Portfolio-Inhaltsbreite und Integrationsschutz für Trends, Gantt und AI-Drawer.
- `frontend/src/app/features/project/project-phases-section.component.scss` -- bestehender Projektvarianten-Override; nur als Regression-Grenze.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/app/core/layout/app-shell.component.scss` -- an Content und Main `min-width: 0` ergänzt -- breite Grid-/Gantt-Kinder können die Sidenav-Shell nicht verbreitern.
- [x] `frontend/src/app/shared/components/trend-chart.component.scss` -- Host schrumpfbar gemacht, Statusraster ohne übergroße feste Mindestbreiten definiert und den 2+1-Umbruch vor der unsicheren Dreispaltenbreite ausgelöst -- die vorhandenen kompakten SVGs bleiben sichtbar und überlauffrei.
- [x] `frontend/src/app/shared/components/trend-chart.component.spec.ts` -- alle drei Karten, die kompakte `440 × 160`-Geometrie und die Zeitraumschalter abgesichert -- der visuelle Fix löst keine Chartregression aus.
- [x] `frontend/src/app/shared/components/gantt-timeline.component.scss` -- Root und Frame vollständig schrumpfbar gemacht sowie die Frame-Höhe ausschließlich für die Portfolio-Variante auf etwa 360–420 px begrenzt -- der breite Canvas bleibt im vorhandenen internen Scrollcontainer.
- [x] `frontend/src/app/shared/components/gantt-timeline.component.spec.ts` -- vertikale Synchronisierung in beide Richtungen, Portfolio-Höhenregel und unveränderte Projektvariante geprüft -- Scrolling und Varianten bleiben funktionsfähig.
- [x] `frontend/src/app/core/layout/app-shell.component.spec.ts` und `frontend/src/app/features/portfolio/portfolio-page.component.spec.ts` -- die Breitenabsicherung sowie Trend-, Gantt- und Drawer-Integration überprüft -- der Seitenfix schützt die Gesamtansicht.

**Acceptance Criteria:**
- Given ein Portfolio auf 1536 × 864 oder 1600 × 900, when Trends geladen sind, then stehen Fortschritt, Ist-Kosten und Projektstatus in einer sichtbaren Reihe ohne horizontalen Browser-Scrollbar.
- Given eine mittlere Inhaltsbreite, when die Kartenbreite für drei Statusspalten nicht mehr genügt, then entsteht ein lesbares 2+1-Layout ohne Überlauf; bei Mobilbreite eine Spalte.
- Given 3M, 6M oder 12M, when der Zeitraum wechselt, then bleiben beide SVGs mit vollständigen Achsenlabels und der Statusbereich funktionsfähig.
- Given ein breiter Gantt-Canvas, when horizontal gescrollt wird, then findet der Scroll ausschließlich im Timeline-Bereich statt.
- Given mehr Portfoliozeilen als sichtbar, when entweder Namen oder Timeline vertikal gescrollt werden, then folgt die andere Seite ohne Endlosschleife und beide Köpfe bleiben sticky.
- Given die Projekt-Detailseite, when ihr Gantt gerendert wird, then bleiben deren Projektvariante und eigene Geometrie unverändert.

## Design Notes

Die Kompaktierung aus `b562c1c` bleibt Grundlage: Trend-SVGs haben bereits ein `440 × 160`-viewBox und das Portfolio-Gantt bereits interne Scrollcontainer sowie 3.5-rem-Zeilen. Der Fix schließt nur verbliebene Grenzen: `min-width: 0` entlang der echten Flex-/Custom-Element-Kette, einen konservativeren Karten-Breakpoint und eine portfolio-spezifische Gantt-Höhe. Der 48-rem-Canvas bleibt erlaubt, da sein Elterncontainer `overflow: auto` trägt.

## Verification

**Commands:**
- `cd frontend && npm test -- --watch=false` -- erwartet: alle Component- und Integrationsspezifikationen sind grün.
- `cd frontend && npm run build` -- erwartet: der Produktionsbuild ist erfolgreich.

**Manual checks:**
- Portfolio bei 1600 × 900, 1536 × 864, 1280 × 800, 1024 × 768, 390 × 844 sowie bei 100 % und 200 % Zoom öffnen: kein Seiten-Overflow; Kartenumbrüche und Achsen sind lesbar; Gantt-Scrollbars liegen im Rahmen.
- Projekt-Detailseite öffnen: die Projekt-Zeitleiste, ihre Marker und das Projekt-AI-Panel funktionieren unverändert.

## Suggested Review Order

**Schrumpfbare Layoutkette**

- Die Shell akzeptiert breite Grid- und Timeline-Kinder ohne die Sidenav-Spalte zu verbreitern.
  [`app-shell.component.scss:13`](../../frontend/src/app/core/layout/app-shell.component.scss#L13)

- Portfolio-Trend- und Gantt-Hosts bilden jeweils eine schrumpfbare Komponentengrenze.
  [`portfolio-trends-section.component.scss:1`](../../frontend/src/app/features/portfolio/portfolio-trends-section.component.scss#L1)
  [`portfolio-gantt-section.component.scss:1`](../../frontend/src/app/features/portfolio/portfolio-gantt-section.component.scss#L1)

**Responsive Trendkarten**

- Container Queries richten den Kartenumbruch an der tatsächlichen Inhaltsbreite aus.
  [`trend-chart.component.scss:1`](../../frontend/src/app/shared/components/trend-chart.component.scss#L1)
  [`trend-chart.component.scss:260`](../../frontend/src/app/shared/components/trend-chart.component.scss#L260)

- Statuszellen können bei kleinen Breiten schrumpfen und beschneiden Labels statt die Seite.
  [`trend-chart.component.scss:187`](../../frontend/src/app/shared/components/trend-chart.component.scss#L187)

**Portfolio-Gantt**

- Nur die Portfolio-Variante erhält ein 360–420-px-Fenster bei unverändert internem Scrollen.
  [`gantt-timeline.component.scss:40`](../../frontend/src/app/shared/components/gantt-timeline.component.scss#L40)

- Mobile Namensspalte gibt der horizontal scrollbareren Timeline nutzbare Restbreite.
  [`gantt-timeline.component.scss:460`](../../frontend/src/app/shared/components/gantt-timeline.component.scss#L460)

**Regressionstests**

- DOM-Scroll-Events prüfen die Synchronisierung beider Portfolio-Panes.
  [`gantt-timeline.component.spec.ts:98`](../../frontend/src/app/shared/components/gantt-timeline.component.spec.ts#L98)

- Trend-Panels, kompakte SVG-Geometrie und Horizontwechsel bleiben geschützt.
  [`trend-chart.component.spec.ts:46`](../../frontend/src/app/shared/components/trend-chart.component.spec.ts#L46)
