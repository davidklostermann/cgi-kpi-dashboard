---
title: 'Portfolio-Trenddiagramme kompakt und responsiv darstellen'
type: 'bugfix'
created: '2026-07-22'
status: 'done'
review_loop_iteration: 0
baseline_commit: '672bfecc4926d301e136151fa1ea1f14becca7ff'
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Die beiden SVG-Liniendiagramme in „Trends & Statusverteilung“ skalieren mit ihrer Panelbreite unbegrenzt proportional zum `440 × 220`-viewBox. Das erzeugt auf breiten Panels unnötig hohe Karten und verschiebt Gantt sowie Projekttabelle vertikal nach unten.

**Approach:** Die Darstellungsgeometrie der vorhandenen Trenddiagramm-Komponente wird kompakter begrenzt und die responsive Skalierung so angepasst, dass beide Charts auf Desktop, Tablet und Mobilgerät gut lesbar bleiben, aber keinen unverhältnismäßigen vertikalen Raum einnehmen.

## Boundaries & Constraints

**Always:** Ausschließlich die Trendsektion und deren zwei Liniencharts anpassen; SVG-Seitenverhältnis und Achsenbeschriftungen lesbar erhalten; alle bestehenden Horizonte, Daten, Statusbalken, Zugänglichkeit und Interaktionen beibehalten; vorhandene uncommittete Portfolio-Drawer-Änderungen unangetastet lassen.

**Ask First:** Änderungen an Datenmodellen, API-Aufrufen, Chart-Inhalten, dem Gantt, der Projekttabelle oder an nicht zum Portfolio gehörenden Komponenten.

**Never:** Backend, Filterlogik, AI-Drawer, Statusverteilung, fachliche Kennzahlen oder eine neue Chart-Bibliothek ändern; den Liniencharts ein festes, nicht responsives Pixelmaß geben.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Breites Desktop-Panel | Trendcharts in der dreispaltigen Desktopansicht | Die SVGs besitzen eine begrenzte, kompaktere Renderhöhe und behalten ihr Seitenverhältnis. | N/A |
| Tablet- oder Mobil-Viewport | Panels wechseln bei den bestehenden 1100px-/720px-Breakpoints auf zwei bzw. eine Spalte | Die SVGs nutzen die verfügbare Breite responsiv, ohne horizontalen Überlauf oder übergroße Höhe. | N/A |
| 3M-, 6M- oder 12M-Horizont | Unterschiedliche Zahl von Datenpunkten und Achsenlabels | Linien, Punkte und alle lesbaren Achsenlabels bleiben innerhalb derselben kompakten SVG-Geometrie funktionsfähig. | N/A |

</frozen-after-approval>

## Code Map

- `frontend/src/app/shared/components/trend-chart.component.html` -- Rendert die beiden SVG-Liniencharts mit dynamischem `viewBox`.
- `frontend/src/app/shared/components/trend-chart.component.scss` -- Steuert Panel-Grid, SVG-Größe und Breakpoints bei 1100px und 720px.
- `frontend/src/app/shared/components/trend-chart.component.ts` -- Berechnet das kompakte `440 × 160`-viewBox sowie Plot- und Padding-Koordinaten.
- `frontend/src/app/shared/components/trend-chart.component.spec.ts` -- Sichert Rendering, Horizonte und Datenzustände der Komponente.
- `frontend/src/app/features/portfolio/portfolio-trends-section.component.scss` -- Besitzt nur Zustandsstyles; die Chart-Komponente wird ausschließlich in dieser Portfolio-Sektion verwendet.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/app/shared/components/trend-chart.component.ts` -- SVG-Geometrie und gegebenenfalls vertikale Paddings auf eine kompaktere, weiterhin lesbare Höhe abstimmen -- die Berechnung und das `viewBox` definieren die tatsächlich skalierte Chartfläche.
- [x] `frontend/src/app/shared/components/trend-chart.component.scss` -- SVG-Darstellung mit einer responsiven Obergrenze und passenden Breakpoint-Regeln begrenzen -- auf breiten Panels darf die Höhe nicht mehr proportional unbegrenzt wachsen, auf kleinen Viewports muss die Darstellung überlauffrei bleiben.
- [x] `frontend/src/app/shared/components/trend-chart.component.spec.ts` -- Die erwartete kompakte `viewBox`-Geometrie der beiden Liniendiagramme prüfen -- ein späterer Rückfall auf die übergroße Standardhöhe wird erkannt.

**Acceptance Criteria:**
- Given ein breites Portfolio-Dashboard, when die Trendsektion gerendert wird, then sind „Portfolio-Fortschritt“ und „Ist-Kosten des Portfolios“ sichtbar kompakter als mit dem bisherigen `440 × 220`-viewBox und behalten ihr Seitenverhältnis.
- Given ein Viewport bei oder unter 1100px beziehungsweise 720px, when das Panel-Grid auf zwei oder eine Spalte wechselt, then bleiben beide Diagramme lesbar, vollständig sichtbar und ohne horizontalen Scrollbereich.
- Given eine Änderung des Zeitraums zwischen 3M, 6M und 12M, when die Liniencharts neu berechnet werden, then bleiben Linien, Punkte, Achsenlabels und die Statusverteilung unverändert funktionsfähig.
- Given die vorherige Portfolio-Drawer-Änderung im Arbeitsbaum, when dieser Fix umgesetzt wird, then werden keine Dateien außerhalb der Trenddiagramm-Komponente und ihrer Tests fachlich verändert.

## Design Notes

Die bisherige `height: auto`-Regel ist nicht isoliert fehlerhaft: Sie überträgt das hohe Seitenverhältnis des `440 × 220`-viewBox proportional auf jedes breite Grid-Panel. Ein kompakteres viewBox mit zugehörigen Koordinaten plus eine begrenzte responsive Inline-Größe erhält die SVG-Proportionen, statt eine feste CSS-Höhe zu erzwingen und das Diagramm zu stauchen.

## Verification

**Commands:**
- `cd frontend && npm test -- --watch=false` -- erwartet: alle bestehenden Tests sowie die Geometrie-Regressionstests sind grün.
- `cd frontend && npm run build` -- erwartet: Produktionsbuild ist erfolgreich.

**Manual checks (if no CLI):**
- Portfolio bei 1536×864, 1024×768 und etwa 390×844 öffnen; die zwei Liniendiagramme müssen kompakt, lesbar und ohne horizontalen Überlauf bleiben.

## Suggested Review Order

**Kompakte SVG-Geometrie**

- Reduziert die Zeichenhöhe und verdichtet die vertikalen Innenabstände ohne Datenlogik zu ändern.
  [`trend-chart.component.ts:194`](../../frontend/src/app/shared/components/trend-chart.component.ts#L194)

**Responsive Darstellung**

- Begrenzt breite Desktop-Charts, erweitert sie auf Tablet und nutzt mobil die verfügbare Breite.
  [`trend-chart.component.scss:129`](../../frontend/src/app/shared/components/trend-chart.component.scss#L129)

- Erhält das bestehende Zwei- und Einspalten-Layout an den etablierten Breakpoints.
  [`trend-chart.component.scss:247`](../../frontend/src/app/shared/components/trend-chart.component.scss#L247)

**Regressionstest**

- Sichert das kompakte viewBox für beide Liniencharts gegen versehentliche Rückfälle ab.
  [`trend-chart.component.spec.ts:48`](../../frontend/src/app/shared/components/trend-chart.component.spec.ts#L48)
