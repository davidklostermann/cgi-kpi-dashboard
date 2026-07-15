# Accessibility Review — cgi-kpi-dashboard

## Overall verdict

Die Spines adressieren Barrierefreiheit bewusst (WCAG 2.2 AA als Ziel, redundante Status-Kodierung, KI-Landmarks, Tastatur, aria-live). **Tragende Text-Kombinationen** (Body, Status-Badges, KI-Text) bestehen den Kontrastcheck. **Ein relevanter Kontrast-Fail** betrifft `{colors.muted}` für Caption-/Achsentexte (~2.97:1 auf Weiß). Die Rot-Grün-Unterscheidung ist durch Wort-Labels und Legenden-Zahlen abgemildert, aber die behauptete Form-Redundanz fehlt im finalen Design — Donut-Segmente bleiben farbabhängig. Keine kritischen Blocker; 1× high, 3× medium, 2× low.

## Kontrast-Tabelle

Berechnet relativ luminance (WCAG 2.2). Schwellen: AA normal ≥ 4.5:1, AA large (≥18pt bold / ≥14pt bold) ≥ 3.0:1.

| Kombination | Vordergrund | Hintergrund | Ratio | AA normal | AA large |
|---|---|---|---|---|---|
| Body/Metric | `#0F172A` | `#FFFFFF` | 17.85:1 | PASS | PASS |
| Body auf App-Grund | `#0F172A` | `#F5F7FB` | 16.65:1 | PASS | PASS |
| Sekundärtext (foreground-muted) | `#475569` | `#FFFFFF` | 7.58:1 | PASS | PASS |
| **Caption/Achse (muted)** | **`#8A97A8`** | **`#FFFFFF`** | **2.97:1** | **FAIL** | **FAIL** |
| Status „Auf Kurs" (on-ink/on-surface) | `#1F7A51` | `#E9F6F0` | 4.77:1 | PASS | PASS |
| Status „Beobachten" (watch-ink/watch-surface) | `#8A5E13` | `#FBF3E2` | 5.15:1 | PASS | PASS |
| Status „Kritisch" (crit-ink/crit-surface) | `#A23A33` | `#FBECEB` | 5.74:1 | PASS | PASS |
| KI-Fließtext (ki-ink/ki-surface) | `#4B3D7A` | `#F7F5FE` | 8.65:1 | PASS | PASS |
| KI-Chip-Text (ki/white) | `#7C3AED` | `#FFFFFF` | 5.70:1 | PASS | PASS |
| Primary UI / Link | `#2563EB` | `#FFFFFF` | 5.17:1 | PASS | PASS |
| Primary-Button | `#FFFFFF` | `#2563EB` | 5.17:1 | PASS | PASS |
| Chat-Nutzerblase (ki-ink/#E7DFFB) | `#4B3D7A` | `#E7DFFB` | 7.27:1 | PASS | PASS |
| Status-Punkte allein (on vs crit auf Weiß) | `#2E9E6A` vs `#D1544C` | `#FFFFFF` | ~3.38:1 (grün auf weiß) | FAIL als Text | PASS large |

## Rot-Grün-Unterscheidbarkeit

- `{colors.status-on}` (#2E9E6A) und `{colors.status-crit}` (#D1544C) sind für Deuteranopie/Protanopie **nicht allein über Farbe zuverlässig unterscheidbar** — typisches Ampel-Problem.
- **Abmilderung im Design:** Status-Badges tragen semantische Worte („Auf Kurs / Beobachten / Kritisch"), Donut-Legende trägt **Zahlen**, Gantt nutzt Projektname + Verzugs-Schraffur.
- **Lücke:** EXPERIENCE.md behauptet „Farbe + **Form**/Wort", DESIGN.md/Mockup v2 nutzen nur **runde Punkte** (keine Kreis/Quadrat/Raute-Differenzierung aus dem Discovery-Entwurf). Donut-Segmente selbst sind rein farblich — akzeptabel nur mit textlicher Legende.

## Findings

- **high** `{colors.muted}` (#8A97A8) auf `{colors.surface}` für Caption, Achsen- und Legendentext (~2.97:1) — unter WCAG AA Normaltext (DESIGN.md §Typography caption, §Colors; Mockup SVG-Achsen `#94a3b8`/`#8A97A8`). *Fix:* Token `muted` auf mindestens ~#6B7785 anheben (≥4.5:1) **oder** Achsen/Captions auf `{colors.foreground-muted}` umstellen; in DESIGN.md mit berechnetem Verhältnis dokumentieren.
- **medium** Form-Redundanz für Ampelstatus fehlt im finalen Design trotz Spine-Aussage (EXPERIENCE.md Component Patterns + Accessibility Floor). *Fix:* Entweder unterschiedliche Punktformen (Kreis/Quadrat/Raute) in `status-badge` + Donut-Legende **oder** Spine-Text auf „Punkt + Wort + numerische Legende" korrigieren und sicherstellen, dass Donut-Legende immer sichtbar ist (nicht nur Tooltip).
- **medium** SVG-Charts (Portfolio-Verlauf, Donut, Gantt) haben keine explizite **textuelle Alternative** für Screenreader (EXPERIENCE.md fordert „textuelle Achsen-/Legendenbeschriftung", aber kein sr-only-Zusammenfassungsmuster). *Fix:* In Component Patterns oder Accessibility Floor: `aria-label`/`role="img"` + versteckte `<table>` oder sr-only-Zusammenfassung („Ø Fortschritt stieg von 59 % auf 63 % über 6 Monate").
- **medium** KI-Fehlerzustand (FR-15) — Retry-Button braucht Fokus-Ring und Screenreader-Label („Analyse erneut laden"); nicht explizit in State Patterns/Accessibility. *Fix:* State Pattern Gemini-Ausfall um `aria-live="polite"` für Fehlermeldung und fokussierbaren Retry ergänzen.
- **low** Fokus-Ring-Spezifikation nur als Verweis `{colors.primary}` in EXPERIENCE.md — kein dediziertes Token (`focus-ring-width`, `focus-ring-offset`) in DESIGN.md. *Fix:* Optional `components.focus-ring` mit 2px `{colors.primary}` + 2px offset.
- **low** Mockup HTML nutzt teils `#9385bd` für KI-Sublabels — nicht als Token definiert; Kontrast auf `{colors.ki-surface}` nicht geprüft im Spine. *Fix:* Als `{colors.ki-muted}` tokenisieren und ≥4.5:1 sichern.
