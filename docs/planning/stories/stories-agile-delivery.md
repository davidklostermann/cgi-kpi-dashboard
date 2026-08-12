# User Stories — Agile Projektsteuerung und externe Projektdaten (Epic 17)

> Ergänzt `stories-mvp.md`, `stories-security-multi-user.md`, `stories-iso-management.md` und `stories-capacity-data-binding.md`. **Keine Implementierung hier.**  
> Stack-Ist: Angular **22** · Spring Boot **3.5.16** · Java 21 · PostgreSQL · CGI EDS **19.0.0**  
> Planungsstand: **2026-08-03** (Überarbeitung Vorgehensmodell, Sprint-Karten, Chart, KPI-Leiste)

---

## Epic 17 — Agile Projektsteuerung und externe Projektdaten

**Geschäftsziel:** Führungskräfte und Projektleiter erkennen im Portfolio das Vorgehensmodell und sehen bei AGILE/HYBRID auf der Projekt-Detailseite eine klare Sprint-Übersicht mit Fortschritt, Velocity und Blockern — zunächst auf reproduzierbaren Mock-Daten, architektonisch vorbereitet für spätere Jira-/PPM-Anbindung.

**Nutzermehrwert:** Schnelle Einordnung (Agil / Hybrid / Klassisch) im Portfolio; auf der Detailseite zentrale Sprint-Entwicklung statt isolierter Einzelkennzahlen; Carry-over und Blocker sichtbar ohne Board-Klon.

**Position auf der Detailseite (Fakten-Spalte):**

```
KPIs → Issues & Maßnahmen → Berichtsstandsvergleich → ISO 21502 → Agile Delivery (neu, nur AGILE/HYBRID) → Team & Kapazität → Phasen/Meilensteine
```

**FR:** FR-35  
**Abhängigkeiten:** Epic 3 (Domain/Flyway/Seed), Epic 5 (Portfolio-Tabelle/Filter), Epic 6 (Detailseite), Epic 12 (Workspace-Isolation), Epic 15 (Section-Pattern/Position nach ISO).  
**Reihenfolge:** 17.1 → 17.2 → 17.3 → 17.4

### Fachlicher Scope

- `deliveryMethod` am Projekt: `AGILE` | `HYBRID` | `WATERFALL`
- Portfolio-Spalte „Vorgehensmodell“ mit EDS-konformen Badges (Agil / Hybrid / Klassisch), sortier- und filterbar
- Projekt-Detail-Sektion **„Agile Delivery“** mit Untertitel **„Sprint-Übersicht und Fortschritt“** nur bei AGILE und HYBRID
- Horizontale Sprint-Karten (S1, S2, S3 …)
- Zentrale Grafik „Sprint-Entwicklung“ (geplante SP, erreichte SP, Velocity-Trend)
- KPI-Leiste: Sprint Health, Gesamte Story Points, Ø Velocity, Carry-over nächster Sprint, Blocker gesamt
- Mock-Daten aus bestehender PostgreSQL; Provider-Abstraktion für spätere externe Quellen

### Technischer Scope

- Flyway Schema/Seed (geplant V15/V16)
- Portfolio- und Projekt-APIs liefern `deliveryMethod`
- Lesender Endpunkt für Agile Delivery (Sprints + KPI-Werte)
- `AgileProjectDataProvider` mit erster JPA/DB-Implementierung
- Frontend: Portfolio-Spalte + Detail-Sektion (CGI EDS), Chart ohne neues Chart-Framework (bestehende Visualisierungsansätze / HTML+CSS+SVG bzw. vorhandene Lib)

### Nicht-Ziele (verbindlich)

- Keine zweite Datenbank
- Keine echte Jira-/PPM-Anbindung, kein OAuth/API-Token, kein Jira MCP, keine bidirektionale Sync
- Kein vollständiger Board-Klon, keine Ticket-Bearbeitung
- Kein neues Chart-Framework
- Keine Neugestaltung der Portfolio-Seite (nur Spalte + Filter/Sortierung ergänzen)
- Keine Änderung bestehender Wasserfall-Kernfunktionen (Phasen/Meilensteine bleiben)
- **Ausdrücklich nicht in diesem Epic:**
  - Lieferprognose
  - Prognostiziertes Abschlussdatum
  - Prognosegenauigkeit
  - Risikoübersicht
  - Panel „Nächste Risiken“

### Risiken

| Risiko | Mitigation |
|---|---|
| Chart-Komplexität ohne neue Lib | Combo-Chart bewusst mit bestehendem Stack (SVG/HTML oder vorhandene Visualisierung); Scope klein halten |
| Sprint-Health-Schwellen unklar | Backend liefert Status + Label; Schwellen als `[OFFEN]` mit Default-Annahme dokumentieren |
| Overengineering Provider | Nur schmales Interface; eine DB-Implementierung; keine Jira-Klassen |

### Messbare Erfolgskriterien

- Portfolio zeigt für alle Projekte die Spalte „Vorgehensmodell“ mit korrektem Badge
- Filter/Sortierung nach Vorgehensmodell funktioniert
- AGILE/HYBRID: Sektion mit Karten, Chart und KPI-Leiste sichtbar
- WATERFALL: keine Agile-Delivery-Sektion
- Bestehende Tests/Builds bleiben grün nach Implementierung (Story 17.4)

---

### Story 17.1 — Agiles Datenmodell und reproduzierbare Mock-Daten

**Als** System  
**möchte ich** Vorgehensmodell, Sprints, Story Points, Carry-over und Blocker persistieren  
**damit** Portfolio und Detailseite reproduzierbare agile Daten nutzen können.

#### Fachlicher Kontext

Grundlage für FR-35. Additive Erweiterung des bestehenden Domänenmodells (Epic 3); bestehende Projekte default `WATERFALL`.

#### Acceptance Criteria

1. **deliveryMethod:** Gegeben `projects`, wenn Schema migriert, dann existiert `delivery_method` mit Werten `AGILE` | `HYBRID` | `WATERFALL`; Default für bestehende Zeilen ist `WATERFALL`.
2. **Sprint-Modell:** Tabelle `project_sprints` mit mindestens: `id` (UUID), `project_id`, `name` (z. B. S1), `sequence` oder sortierbarer Index, `start_date`, `end_date`, `lifecycle` (`PAST` | `ACTIVE` | `FUTURE`), `story_points_planned`, `story_points_completed`, `carry_over_points` (Carry-over in den nächsten Sprint), optional persistierter oder berechenbarer `health_status` (`GOOD` | `WATCH` | `CRITICAL` | `PLANNED`).
3. **Work Items / Blocker:** Tabelle `project_work_items` (oder äquivalent) mit Bezug zu Projekt/Sprint; Felder mindestens für Story Points und `is_blocker` (oder Priority `BLOCKER`), sodass Blocker-Anzahl pro Projekt ableitbar ist.
4. **Flyway:** Neue Migrationen **V15** (Schema inkl. `delivery_method`) und **V16** (deterministischer Seed); keine Änderung an V1–V14.
5. **Seed-Abdeckung:** Mindestens ein `AGILE`-Projekt und ein `HYBRID`-Projekt mit mehreren Sprints (vergangen, aktiv, zukünftig) inkl. geplanter/erreichter SP, Carry-over und Blockern; mehrere `WATERFALL`-Projekte ohne Sprint-/Work-Item-Zeilen.
6. **Reproduzierbarkeit:** Seed-UUIDs/Werte deterministisch über Umgebungen (Pilot-Konvention).
7. **Tests:** Migration-/Repository-Tests bestätigen Schema, Defaults und Seed-Abdeckung.

#### Technische Hinweise

- FK `ON DELETE CASCADE` zu `projects`
- JPA-Enums als String
- Carry-over und Blocker so modellieren, dass Story 17.2 sie ohne Frontend-Berechnung liefern kann
- `[OFFEN]` Persistenz vs. Berechnung von `health_status` — Default-Annahme: Backend berechnet aus Plan/Ist/Carry-over; Seed darf Rohwerte liefern

#### Abhängigkeiten

- Epic 3 (Domain, Flyway, Seed-Patterns)
- Epic 12 (Workspace über `project_id`)

#### Out of Scope

- REST-APIs, Frontend, Chart-Datenaggregation, Prognosen, Risiko-Panels

#### Testanforderungen

- Flyway-Migrationstest (Anzahl/Idempotenz anpassen, falls Projektstand V14+)
- Seed-Abdeckung AGILE/HYBRID/WATERFALL
- Repository-Tests Sprints/Work Items

#### Definition of Done

- Schema + Seed lauffähig; Defaults WATERFALL; Tests für Modell/Seed grün; Story-Status in Sprint-Tracking aktualisierbar

---

### Story 17.2 — Agile Backend-Abfrage und KPI-Berechnung

**Als** Frontend und Management-UI  
**möchte ich** `deliveryMethod`, Sprint-Daten und agile KPI-Werte über REST abrufen  
**damit** Portfolio und Detailseite ohne Client-seitige KPI-Rechnung arbeiten können.

#### Fachlicher Kontext

Erweitert bestehende Portfolio-/Projekt-Fakten-Endpunkte und ergänzt einen lesenden Agile-Delivery-Endpunkt. KPI-Berechnung in `kpi.*` / Provider; keine Entity-Leaks (AD-3/AD-5).

#### Acceptance Criteria

1. **deliveryMethod in Portfolio/Projekt:** Gegeben existierende Portfolio-Tabellen- und Projekt-Stammdaten-/Listen-Endpunkte, wenn abgerufen, dann enthält jedes Projekt `deliveryMethod` (`AGILE` | `HYBRID` | `WATERFALL`).
2. **Provider:** Interface `AgileProjectDataProvider`; erste Implementierung liest aus PostgreSQL/JPA. Keine konkrete Jira-Implementierung.
3. **Endpunkt:** `GET /api/projects/{id}/agile-delivery` liefert DTO (kein Entity) mit:
   - `deliveryMethod`
   - `dataAvailable` (false bei WATERFALL oder fehlenden Sprint-Daten)
   - `sprints[]` für Karten und Chart: Name, Lifecycle, Health (`GOOD`/`WATCH`/`CRITICAL`/`PLANNED` + Label Gut/Achtung/Kritisch/Geplant), Fortschritt %, geplante SP, erreichte SP, Carry-over, Kennzeichnung aktuell vs. zukünftig
   - `chart`: Serien für geplante SP (Balken), erreichte SP (Balken), Velocity-Trend (Linie) je Sprint; Flag für zukünftige Sprints (gestrichelt/gedämpft)
   - `kpis`: Sprint Health (aktueller Sprint), Gesamte Story Points, Ø Velocity, Carry-over nächster Sprint, Blocker gesamt
4. **Berechnungen (definiert):**
   - Fortschritt % = `(erreichte SP / geplante SP) * 100` (bei planned=0 → definiert 0 oder N/A, dokumentiert)
   - Ø Velocity = Mittelwert der erreichten SP abgeschlossener Sprints (mindestens letzte N abgeschlossene; Default N=3 wenn verfügbar)
   - Carry-over nächster Sprint = Carry-over-Wert des aktuellen bzw. letzten abgeschlossenen Sprints gemäß Seed/Regel
   - Blocker gesamt = Anzahl offener Blocker-Work-Items des Projekts
   - Sprint Health: `PLANNED` für FUTURE; für ACTIVE/PAST aus Plan/Ist/Carry-over-Regel (Schwellen `[OFFEN]`, Default dokumentieren)
5. **WATERFALL:** `200` mit `dataAvailable: false` — kein 404 allein wegen WATERFALL.
6. **404:** Nur wenn Projekt nicht existiert oder nicht im Workspace (Epic 12).
7. **Explizit nicht enthalten:** Lieferprognose, prognostiziertes Abschlussdatum, Prognosegenauigkeit, Risikoübersicht, „Nächste Risiken“.

#### Technische Hinweise

- Portfolio-Filter/Sortierung nach `deliveryMethod` serverseitig unterstützen, analog bestehender Filter (Epic 4/5)
- Keine Predictability-/Prognose-Felder im DTO
- Chart-Serie ausschließlich aus Backend-Zahlen; Frontend rendert nur

#### Abhängigkeiten

- Story 17.1
- Epic 3, 5 (Portfolio-API), 6 (Projekt-API), 12

#### Out of Scope

- Frontend-UI, echte Jira-Anbindung, AI-Facts (optional in 17.4 nur wenn ohne Scope-Creep; Standard: Kennzahlen-Tests, keine Pflicht-AI-Erweiterung wenn nicht nötig für AC)

#### Testanforderungen

- API-Tests: AGILE/HYBRID Happy Path, WATERFALL `dataAvailable: false`, Workspace-404
- Unit-Tests für Velocity, Fortschritt, Carry-over, Blocker-Anzahl, Health-Mapping
- Portfolio-/Projekt-Response enthält `deliveryMethod`

#### Definition of Done

- Endpunkte und Berechnungen dokumentiert und getestet; Provider austauschbar; keine Prognose-/Risiko-Felder

---

### Story 17.3 — Vorgehensmodell und Agile Delivery im Frontend

**Als** Führungskraft oder Projektleiter  
**möchte ich** das Vorgehensmodell im Portfolio sehen und bei agilen/hybriden Projekten Sprint-Karten, Sprint-Entwicklung und KPI-Leiste auf der Detailseite nutzen  
**damit** ich Delivery-Fortschritt schnell einordnen kann.

#### Fachlicher Kontext

Erweitert bestehende Portfolio-Projekttabelle (Epic 5) und Projekt-Detailseite (Epic 6/15-Pattern). Keine separate agile Projektseite.

#### Acceptance Criteria

1. **Portfolio-Spalte:** Gegeben Portfolio-Projekttabelle, dann existiert Spalte „Vorgehensmodell“ mit EDS-konformen Badges: **Agil** (`AGILE`), **Hybrid** (`HYBRID`), **Klassisch** (`WATERFALL`).
2. **Sortierung:** Spalte ist sortierbar nach Vorgehensmodell.
3. **Filter:** Portfolio-Filter erlaubt Filterung nach Vorgehensmodell (mindestens die drei Werte).
4. **Sichtbarkeit Detail:** Sektion „Agile Delivery“ nur bei `AGILE` und `HYBRID`; bei `WATERFALL` nicht gerendert.
5. **Überschriften:** Titel exakt **„Agile Delivery“**, Untertitel **„Sprint-Übersicht und Fortschritt“**.
6. **Sprint-Karten:** Horizontale Karten für S1, S2, S3 … je mit Name, Status (Gut / Achtung / Kritisch / Geplant), Fortschritt %, erreichte und geplante Story Points, Carry-over; aktueller Sprint hervorgehoben; zukünftige Sprints gedämpft als geplant.
7. **Grafik „Sprint-Entwicklung“:** Zentrales visuelles Element; kombinierte Darstellung Balken „Geplante SP“, Balken „Erreichte SP“, Linie „Velocity-Trend“; X = Sprints, Y = Story Points; zukünftige Sprints gestrichelt oder gedämpft; Legende unterhalb der Grafik. Kein neues Chart-Framework.
8. **KPI-Leiste** unterhalb der Grafik mit genau: Sprint Health · Gesamte Story Points · Ø Velocity · Carry-over nächster Sprint · Blocker gesamt.
9. **Empty-State:** AGILE/HYBRID ohne Sprint-Daten → definierter Leerhinweis (keine leeren Chart-Hüllen mit undefined).
10. **Loading/Error:** Unabhängiges Laden; Error mit Retry; Nachbar-Sections unberührt.
11. **Nicht anzeigen:** Lieferprognose, prognostiziertes Abschlussdatum, Prognosegenauigkeit, Risikoübersicht, Panel „Nächste Risiken“.
12. **Datenquelle:** Kennzeichnung Mock/interne Quelle (Vorbereitung auf externe Quelle).

#### Technische Hinweise

- Wiederverwendung: `status-badge`/EDS-Badges, Section-Pattern wie ISO/Capacity, `ProjectApiService`
- Chart: vorhandene Visualisierungsansätze (wie Trend/Gantt-Stack); kein neues Framework
- Position: nach ISO 21502, vor Team & Kapazität
- `id="fact-agile-delivery"` als Anker (optional für spätere Facts)

#### Abhängigkeiten

- Story 17.2 (API + DTOs)
- Epic 5 (Tabelle/Filter), Epic 6 (Detailseite), Epic 15 (Layout-Position), Epic 10 (Loading/Error/A11y)

#### Out of Scope

- Board-Ansicht, Bearbeitung, Portfolio-Neudesign, Prognose-/Risiko-UI

#### Testanforderungen

- Component-Tests: Badge-Labels, Filter/Sort (soweit UI-testbar), Sichtbarkeit AGILE/HYBRID vs. WATERFALL
- Agile-Section: Loading, Empty, Error, Sprint-Karten, Chart-Datenbindung, KPI-Leiste
- Keine Regression bestehender Detail-Sections

#### Definition of Done

- Portfolio-Spalte + Detail-Sektion gemäß AC; WATERFALL ohne Agile-Sektion; Component-Tests grün

---

### Story 17.4 — Qualitätssicherung und Jira-Readiness

**Als** Product Owner  
**möchte ich** Tests und dokumentierte Provider-Grenze  
**damit** `deliveryMethod`, Sichtbarkeit, Sprint-Karten, Chart-Daten und KPIs abgesichert sind und Jira später austauschbar angebunden werden kann.

#### Fachlicher Kontext

Querschnittliche Absicherung ohne Scope-Ausweitung auf Prognose/Risiko oder echte Externe Integration.

#### Acceptance Criteria

1. **Backend-Tests:** Integrationstests für `deliveryMethod` in Portfolio/Projekt-APIs und für `/agile-delivery` (Happy Path, WATERFALL, Isolation/404, KPI-Felder).
2. **Frontend-Tests:** Component-Tests für Vorgehensmodell-Badges, Sichtbarkeitslogik, Sprint-Karten, Chart-Daten und KPI-Leiste.
3. **Regression:** Bestehende Portfolio-/Detail-/WATERFALL-Tests bleiben grün.
4. **Builds:** Backend-Verify und Frontend-Production-Build erfolgreich.
5. **Provider-Abstraktion:** Nachweis, dass eine spätere Jira-Implementierung Controller/Frontend nicht ändern muss (Interface-Grenze dokumentiert).
6. **Dokumentation in BMAD:** Kurze Abgrenzung Mock vs. zukünftige Jira-/PPM-Integration in diesem Story-Artefakt oder Epic-Notiz — keine parallele Planungsdatei.
7. **Nicht enthalten:** Lieferprognose, prognostiziertes Abschlussdatum, Prognosegenauigkeit, Risikoübersicht, „Nächste Risiken“, reale Jira-Anbindung.

#### Technische Hinweise

- AI-Facts (`agile.*`) nur optional und nur, wenn ohne neuen Scope; **nicht verpflichtend** für Epic-DoD, sofern nicht explizit nachgezogen
- Fokus: Fakten-UI und API-Stabilität

#### Abhängigkeiten

- Stories 17.1–17.3
- Epic 12 Isolationstests als Vorlage

#### Out of Scope

- Echte externe Integration, OAuth, Board, Prognose, Risiko-Panels

#### Testanforderungen

| Bereich | Mindestabdeckung |
|---|---|
| Backend | `deliveryMethod`, Sprint-/KPI-Berechnung, WATERFALL, Workspace-404 |
| Frontend | Badges, Filter/Sort soweit möglich, Section-Sichtbarkeit, Karten/Chart/KPIs, Empty/Error |
| Builds | Backend + Frontend Production |

#### Definition of Done

- Tests und Builds grün; Provider-Handoff dokumentiert; Sprint-Status Stories 17.1–17.4 auf `done` setzbar

---

## Epic-17 Definition of Done

- [x] Stories 17.1–17.4 umgesetzt und `done`
- [x] FR-35 in `epics.md` Coverage Map verknüpft
- [x] Portfolio-Spalte „Vorgehensmodell“ sortier-/filterbar mit Badges Agil/Hybrid/Klassisch
- [x] Agile Delivery nur bei AGILE/HYBRID: Karten + Sprint-Entwicklungs-Chart + KPI-Leiste
- [x] WATERFALL ohne Agile-Delivery-Sektion
- [x] Keine Lieferprognose / Abschlussprognose / Prognosegenauigkeit / Risikoübersicht / „Nächste Risiken“
- [x] Flyway V15/V16; Provider-Abstraktion dokumentiert; Production Builds grün

## Offene fachliche Entscheidungen

| Thema | Status |
|---|---|
| Schwellenwerte Sprint Health (Gut / Achtung / Kritisch) | `[OFFEN]` — Default-Annahme in 17.2 dokumentieren |
| Genaues N für Ø Velocity (letzte 3 vs. alle abgeschlossenen) | Default: letzte 3 abgeschlossene Sprints |
| Persistenz vs. Berechnung von Health/Carry-over | Default: Carry-over im Seed; Health backend-berechnet |
| Chart-Umsetzung ohne neues Framework | Bestehender Visualisierungsstack / SVG |
