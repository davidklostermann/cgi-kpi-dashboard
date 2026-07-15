# Reconciliation — Referenzbild „goplan" (imports/reference-goplan-dashboard.png)

Der Nutzer lieferte das goplan-Dashboard als Ziel-Anmutung. Abgleich, was in die Spines übernommen bzw. bewusst nicht übernommen wurde.

## Übernommen (in DESIGN.md / EXPERIENCE.md destilliert)
- Ruhige Enterprise-Analytics-Anmutung: weiße Karten auf hellem Grund, weiche Schatten, großzügiger Weißraum → DESIGN.md Brand & Style, Elevation & Depth.
- Linke Sidebar-Navigation → EXPERIENCE.md IA / Foundation, DESIGN.md Layout & Spacing.
- KPI-Karten-Reihe mit getönten Icon-Kacheln + großer Zahl + Delta → `kpi-card`.
- Flächen-/Linien-Zeitverlauf mit Achsen und Legende → `chart-card`.
- Donut mit Legende (im Original „Asset Allocation") → adaptiert als Ampelverteilung `donut`.
- Reduzierte, gut lesbare Tabelle → `project-table-row`.

## Nicht übernommen (bewusst, außerhalb MVP-Scope)
- Globale Suchleiste im Header — kein Suchbedarf im MVP-Umfang.
- Benachrichtigungs-Glocke / „Customize"-Button — keine entsprechenden Features (PRD Scope §6).
- Warnbanner-Muster (gelber Hinweisstreifen oben) — Warnungen laufen stattdessen über die KI-Trendanalyse (Top-3-Handlungsbedarf) und Status-Badges.
- Reiche, mehrstufige Sidebar (viele Menügruppen) — IA hat nur zwei Kernoberflächen (Portfolio, Projekte).

## Ergänzt über die Referenz hinaus (produktspezifisch)
- Rechte KI-Spalte (violett) als Signatur — nicht im goplan-Original; erfüllt PRD FR-10 (KPI/KI-Trennung).
- Gantt-Zeitachse für Projektphasen inkl. Verzugs-Markierung — auf Nutzerwunsch, im Original nicht vorhanden.

Bei Konflikt gewinnen die Spines gegenüber dem Referenzbild.
