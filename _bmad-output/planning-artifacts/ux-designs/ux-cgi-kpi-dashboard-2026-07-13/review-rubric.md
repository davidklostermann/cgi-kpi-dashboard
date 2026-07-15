# Spine Pair Review — cgi-kpi-dashboard

## Overall verdict

Das Spine-Paar ist als Downstream-Vertrag **grundsätzlich einsatzbereit**: UJ-1/UJ-2 sind als Key Flows vollständig modelliert, das Token-System ist vollständig hex-definiert, die KPI/KI-Trennung ist in beiden Spines konsistent verankert, und die visuellen Referenzen sind an den relevanten Stellen verlinkt. Es gibt **keine kritischen Blocker**, aber mehrere **medium**-Findings — vor allem fehlende Verhaltensregeln für Neben-Komponenten, ein offener Leerzustand, ein nicht modellierter Fakten-API-Fehlerzustand und eine leichte Inkonsistenz bei der redundanten Status-Kodierung (Wort ja, Form nein). Empfehlung: Findings als Update-Runde oder direkt in Story-Acceptance-Criteria aufnehmen; Finalisierung bleibt gültig.

## 1. Flow coverage — strong

Geprüft: PRD §2.3 UJ-1, UJ-2 gegen EXPERIENCE.md → Key Flows.

- UJ-1 → Flow 1 (Sabine): benannter Protagonist, 6 nummerierte Schritte, Climax (30-Sekunden-Entscheidung), Failure path (Gemini-Ausfall, FR-15) ✓
- UJ-2 → Flow 2 (Markus): benannter Protagonist, 6 nummerierte Schritte, Climax (KI-Antwort als Gesprächsgrundlage), Failure path (fehlende Datengrundlage, FR-14) ✓

### Findings

*(keine)*

## 2. Token completeness — adequate

Geprüft: gesamtes `colors`-, `typography`-, `rounded`-, `spacing`-, `components`-Frontmatter; alle `{path.to.token}`-Referenzen in DESIGN.md-Prosa und EXPERIENCE.md.

- Alle referenzierten Pfade lösen auf definierte Frontmatter-Tokens auf ✓
- Kontrastziele in DESIGN.md §Colors explizit genannt (≥ 7:1 Body, ≥ 4.5:1 Status-`*-ink`) ✓
- `#E7DFFB` (chat-bubble-user) nur inline in Components-Tabelle, nicht als `{colors.*}`-Token im Frontmatter

### Findings

- **medium** `{colors.muted}` (#8A97A8) auf `{colors.surface}` erreicht ~2.97:1 — unter WCAG AA für Normaltext (DESIGN.md §Colors, §Typography caption). *Fix:* `muted` auf #6B7785–#707F93 anheben oder Caption/Achsentext auf `{colors.foreground-muted}` umstellen; Verhältnis in DESIGN.md verifiziert dokumentieren.
- **low** `chat-bubble-user`-Hintergrund `#E7DFFB` fehlt als benanntes Color-Token (DESIGN.md Components + Frontmatter). *Fix:* Token `chat-user-surface: '#E7DFFB'` ergänzen und in `chat-bubble-user` referenzieren.

## 3. Component coverage — adequate

Geprüft: alle in DESIGN.md Components, EXPERIENCE.md Component Patterns und Mockup vorkommenden Komponenten.

| Komponente | DESIGN.md | EXPERIENCE.md |
|---|---|---|
| kpi-card | ✓ | ✓ |
| status-badge | ✓ | ✓ |
| ki-panel | ✓ | ✓ |
| chart-card | ✓ | ✓ |
| donut | ✓ | ✓ |
| gantt | ✓ | ✓ |
| project-table-row | ✓ | ✓ |
| filter-chip | ✓ | ✓ |
| quick-reply-chip | ✓ | ✓ |
| chat | ✓ (Prosa) | ✓ |
| chat-bubble-user/ki | ✓ (Frontmatter) | (via `chat`) |
| button-primary | ✓ | ✗ |
| Sidebar-Navigation | (implizit Layout) | ✗ |

### Findings

- **medium** `button-primary` hat visuelle Spec (DESIGN.md), aber keine Verhaltensregel in EXPERIENCE.md — betrifft u. a. „Senden" im Q&A und potenzielle primäre Aktionen. *Fix:* Zeile in Component Patterns: disabled/loading während Q&A-Wartezeit; Enter löst Senden aus.
- **low** Sidebar-Navigation ohne explizite Component-Pattern-Zeile (aktiver Zustand, Fokus). *Fix:* Kurzzeile in Component Patterns oder Interaction Primitives.

## 4. State coverage — adequate

Geprüft: Portfolio-Übersicht, Projekt-Detailansicht — cold-load, empty, error, focus, filtered-empty, KI-degraded.

| Zustand | Abgedeckt |
|---|---|
| Kaltstart/Laden | ✓ |
| Leeres Portfolio | `[OFFEN]` — Text fehlt |
| Gefilterte Leermenge | ✓ |
| Gemini-Ausfall (KI) | ✓ |
| Unzureichende KI-Daten | ✓ |
| Q&A wartet | ✓ |
| Tabellen-Fokus/Hover | ✓ |
| Backend/Fakten-API-Fehler | ✗ |
| KI-Panel Retry-Erfolg | ✗ (implizit) |

### Findings

- **medium** Kein State Pattern für **Fakten-API-Fehler** (KPIs/Diagramme/Tabelle laden nicht), obwohl FR-15 nur Gemini-Ausfall adressiert. *Fix:* Zeile in State Patterns: partielle/totale Fakten-Fehler mit Retry; KI-Spalte unabhängig.
- **low** Leerzustand Portfolio `[OFFEN: UX-Text]` (PRD FR-1) — bewusst offen, aber vor Story-Dev zu schließen. *Fix:* Microcopy in Voice and Tone oder State Patterns festlegen.

## 5. Visual reference coverage — adequate

Geprüft: mockups/, imports/, wireframes/; Inline-Links in Spines; spines-win-on-conflict.

| Artefakt | Verlinkt | Illustriert |
|---|---|---|
| mockups/portfolio-und-projekt-detail.html | ✓ (beide Spines) | Gesamtkomposition beider Screens |
| mockups/farbthemen.html | ✓ (DESIGN.md) | Farbherleitung |
| imports/reference-goplan-dashboard.png | ✓ (DESIGN.md, EXPERIENCE.md Inspiration) | Ziel-Anmutung |
| reconcile-goplan.md | ✗ | Abgleich Referenz ↔ Spine |
| .working/* | ✗ (absichtlich nicht promoted) | — |

„Spines gewinnen bei Konflikt" in DESIGN.md und EXPERIENCE.md ✓

### Findings

- **low** `reconcile-goplan.md` ist Orphan — nicht aus Spines referenziert. *Fix:* Einzeiler in EXPERIENCE.md → Inspiration & Anti-patterns verlinken oder in Sources aufnehmen.

## 6. Bloat & overspecification — strong

Keine überflüssigen PRD-Restatements, keine Pixel-Specs wo Tokens reichen. EXPERIENCE.md bleibt verhaltensfokussiert. DESIGN.md-Prosa trägt begründete Entscheidungen (FR-10, SM-C2).

### Findings

*(keine)*

## 7. Inheritance discipline — adequate

- Sources frontmatter löst auf existierende PRD-Pfade auf ✓
- UJ-Namen verbatim (UJ-1, UJ-2) ✓
- FR-Referenzen in Flows und Patterns konsistent ✓
- EXPERIENCE `{colors.ki}` → DESIGN.md ✓

### Findings

- **medium** EXPERIENCE.md status-badge: „Farbe + **Form**/Wort" — finales Mockup und DESIGN.md nutzen nur **Punkt + Wort**, keine unterschiedlichen Formen (Kreis/Quadrat/Raute). Redundanz gegenüber Rot-Grün-Schwäche ist schwächer als behauptet. *Fix:* Either Form-Kodierung in DESIGN.md + Mockup wieder einführen **oder** EXPERIENCE.md + Accessibility Floor auf „Punkt + Wort + Legende mit Zahlen" präzisieren.
- **low** Sortierbare Spalten `[OFFEN]` in Component Patterns — korrekt getaggt, aber vor Dev klären.

## 8. Shape fit — strong

DESIGN.md: Brand & Style → Colors → Typography → Layout & Spacing → Elevation & Depth → Shapes → Components → Do's and Don'ts ✓

EXPERIENCE.md: Foundation, IA, Voice and Tone, Component Patterns, State Patterns, Interaction Primitives, Accessibility Floor, Responsive & Platform, Inspiration & Anti-patterns, Key Flows ✓

Inspiration & Responsive getriggert und vorhanden ✓

### Findings

*(keine)*

## Mechanical notes

- `chat` in DESIGN.md Components-Tabelle, Frontmatter hat `chat-bubble-user`/`chat-bubble-ki` — konsolidieren oder Cross-Ref.
- `#F8FAFD` (project-table-row hover) und `#E7DFFB` (chat-user) als Hardcoded-Hex — optional tokenisieren.
- `.working/`-Artefakte (Wireframe v1, Mockup-Entwürfe) bewusst nicht promoted — kein Spine-Gap.
