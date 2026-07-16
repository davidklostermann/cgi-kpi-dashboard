# Epic 5 — Review & Freigabe (2026-07-16)

Status: **done** nach Pflicht-Fixes

## Pflichtprüfung — Ergebnis

| Prüfpunkt | Vorher | Fix | Schwere |
|---|---|---|---|
| Gantt-Monatsachse unlesbar | Monatslabels überlappt bei langem Horizont | Dynamische Canvas-Breite (`monthSpan × 3.25rem`), Quartalslabels ab >18 Monaten | **Blocker** → behoben |
| Meilensteine Story 5.2 | API ja, UI fehlte | Diamant-Marker + Legende im Gantt | **Blocker** → behoben |
| 3M/6M/12M nur Jun/Jul | Seed nur 2 Monate | V5-Migration: 12 Monate (2025-08 … 2026-07) | **Blocker** → behoben |
| Statusbalken bei 0 sichtbar | `min-width: 0.25rem` | Balken nur bei `count > 0`, min-width entfernt | **Major** → behoben |
| Planende/Prognose/Terminabw. | DB-Feld direkt, inkonsistent möglich | `ScheduleDeviationResolver` aus Plan-/Prognosedatum | **Major** → behoben |
| Trefferzahlen KPI/Gantt/Tabelle/Trends | Kein Cross-Test | Integrationstest: 19 über alle Endpunkte | **Major** → behoben |
| Ampel-KPI doppelt | KPI-Karte + Trend-Balken | Ampel-KPI-Karte entfernt, API `statusDistribution` bleibt | **Major** → behoben |
| Layout volle Breite | Trends/Gantt/Tabelle in 75%-Spalte | KPI+KI oben, Visualisierungen darunter full-width | **Major** → behoben |
| Deutsche Zahlenformate | DecimalPipe ohne Locale | `LOCALE_ID: de-DE` global | **Minor** → behoben |
| Kritische Risiken/Probleme | KPI „Kritische Risiken“ vs. Tabelle „Kritisch“ | KPI „Kritische Risiken (offen)“, Tabelle „Krit. Risiken & Probleme“ | **Minor** → behoben |
| Navigation Tabelle → Detail | Bereits in 5.5 | Enter + Filter-Persistenz unverändert grün | — |

## Teststand nach Fixes

- Backend: `mvn test`
- Frontend: `npm test -- --watch=false`

## Geänderte Kern-Dateien

- `V5__mock_seed_trend_history.sql` — 12-Monats-Snapshots
- `ScheduleDeviationResolver.java` — konsistente Terminabweichung
- `gantt-timeline.component.*` — Achse, Breite, Meilensteine
- `trend-chart.component.*` — Status-Balken, Horizon-Tests
- `portfolio-page.component.*` — Layout full-width
- `portfolio-kpi-section.component.html` — Ampel-KPI entfernt
- `app.config.ts` — de-DE Locale
