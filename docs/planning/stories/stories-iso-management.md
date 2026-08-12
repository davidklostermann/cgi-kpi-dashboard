# User Stories — ISO-orientierte Projektsteuerung (Epic 15)

> Ergänzt `stories-mvp.md` und `stories-security-multi-user.md`. **Keine Implementierung hier.**  
> Stack-Ist: Angular **22** · Spring Boot **3.5.16** · Java 21 · PostgreSQL · CGI EDS **19.0.0**  
> Planungsstand: **2026-07-29**

---

## Epic 15 — ISO-orientierte Projektsteuerung

**Ziel:** Die bestehende Projekt-Detailansicht um eine kompakte Sektion **„ISO 21502 – ergänzende Steuerungsfelder“** mit fünf gleichartigen Management-Cards erweitern — ohne KPI-Overload, ohne neue Charts/Gauges/Donuts.

**Position auf der Detailseite (Fakten-Spalte):**

```
KPIs → Issues & Maßnahmen → Berichtsstandsvergleich → ISO 21502 → Team & Kapazität → Phasen/Meilensteine
```

**FR:** FR-33  
**Abhängigkeiten:** Epics 1–14 (fachlich als abgeschlossen vorausgesetzt); insbesondere Epic 6 (Detailseite), Epic 7 (Team & Kapazität), Epic 9 (ApprovedProjectDataReader), Epic 12 (Workspace-Isolation).  
**Reihenfolge:** 15.1 → 15.2 → 15.3 (strikt sequenziell)

**ISO-Hinweis (verbindlich):** UI-Titel **„ISO 21502 – ergänzende Steuerungsfelder“** ist erlaubt. **Nicht** verwenden: „ISO compliant“, „ISO-konform“, „ISO-zertifiziert“. Es werden ausgewählte Management Practices umgesetzt — keine vollständige Normkonformität.

**Out of Scope (Epic):**

- Bearbeitung/Pflege der ISO-Felder in der UI (Read-only Anzeige)
- Portfolio-Aggregation oder Filter nach ISO-Feldern
- Vollständige ISO-21502-Compliance-Checkliste oder Audit-Workflow
- Neue Haupt-KPI-Karten, Donut-/Gauge-Charts, Trend-Visualisierungen für ISO-Bereiche
- Änderung bestehender Detail-Sections (KPIs, Issues, Berichtsvergleich, Kapazität, Phasen, KI)
- CR/Change-Management-Workflow (Genehmigung, Historie, Detail-Listen)

---

### Story 15.1 — ISO Management Domain & API

**Als** Führungskraft oder Projektleiter  
**möchte ich** ISO-orientierte Steuerungsfelder pro Projekt über eine API abrufen  
**damit** ich Nutzen, Scope, Changes, Qualität und Stakeholder kompakt einsehen kann.

#### Ziel

Persistentes Domain-Modell, Flyway-Migration, Mock-Seed, Repository/Service und REST-Endpunkt für alle fünf Management-Bereiche — workspace-isoliert und an bestehende `kpi.*`-Patterns angelehnt.

#### Scope

- JPA-Entität `ProjectIsoManagement` (1:1 zu `Project`, `project_id` PK/FK)
- Flyway **V12** — Schema (keine Änderung an V1–V11)
- Flyway **V13** — Mock-Seed für repräsentative Projekte (~6–8 von ~20 mit Daten; Rest ohne Zeile → Empty-State)
- DTO `ProjectIsoManagementDto` mit fünf verschachtelten Records:
  - `BenefitsCardDto`: `expectedBenefit`, `benefitUnit`, `realizedPercent`, `status` (`GREEN`|`AMBER`|`RED`)
  - `ScopeCardDto`: `scopeStatus`, `deviations` (max. 2 Strings), `trend` (`IMPROVING`|`STABLE`|`DETERIORATING`)
  - `ChangeRequestsCardDto`: `total`, `open`, `inReview`, `approved`, `impactSchedule`, `impactCost`, `impactScope` (jeweils `NONE`|`LOW`|`MEDIUM`|`HIGH`)
  - `QualityCardDto`: `qualityStatus`, `openDefects`, `criticalDefects`, `testAcceptanceStatus`, `progressPercent`
  - `StakeholdersCardDto`: `sponsorCustomer`, `stakeholderStatus`, `escalationStatus`, `lastSteeringDate` (optional, `null` wenn nicht gepflegt)
- Enum-Typen backend-seitig als Java-Enums; API liefert String-Werte + deutschsprachige `*Label`-Felder wo nötig (Pattern: `status-badge`-Labels)
- `ProjectIsoManagementAssembler` in `kpi.*`
- Endpunkt: `GET /api/projects/{id}/iso-management` → `200` + DTO oder `404` wenn Projekt nicht im Workspace / nicht existent
- Workspace-Isolation über bestehenden Project-Scope (wie `/capacity`, `/issues-actions`)
- `factsAsOf`-Timestamp im DTO-Root (analog `ProjectCapacityDto`)

#### Acceptance Criteria

1. **Schema:** Gegeben frische DB, wenn Flyway läuft, dann existiert Tabelle `project_iso_management` mit allen Feldern für die fünf Cards; FK `project_id` → `projects(id)` ON DELETE CASCADE; kein Eintrag in V1–V11 geändert.
2. **Seed:** Gegeben Mock-Portfolio, dann haben mindestens 6 Projekte (inkl. Szenario-Vertreter: im Plan, Terminverzug, Budget, Risiken, widersprüchliche Signale) plausible ISO-Daten; mindestens 1 Projekt ohne ISO-Zeile für Empty-State-Tests.
3. **API — Happy Path:** Gegeben authentifizierter Nutzer im Workspace und existierendes Projekt mit ISO-Daten, wenn `GET /api/projects/{id}/iso-management`, dann Response enthält alle fünf Card-Objekte mit den in Scope definierten Feldern; `Content-Type: application/json`.
4. **API — Empty:** Gegeben Projekt ohne ISO-Zeile, dann `200` mit `dataAvailable: false` und strukturiertem Empty-Payload **oder** `200` mit `null`-Cards und `dataAvailable: false` — einheitlich dokumentiert; UI-tauglicher Hinweistext-Feld `emptyReason`.
5. **API — Isolation:** Gegeben Projekt-ID außerhalb des Workspace, dann `404` mit `{ code, message }` (bestehendes Fehlerformat).
6. **Scope-Abweichungen:** Gegeben Seed/API, dann liefert `deviations` höchstens 2 Einträge; längere Listen werden backend-seitig gekürzt.
7. **Stakeholder optional:** Gegeben Projekt ohne `last_steering_date`, dann Feld `lastSteeringDate: null`; UI kann Feld ausblenden.
8. **Keine Regression:** Bestehende Endpunkte `/kpis`, `/trends`, `/capacity` unverändert in Semantik.

#### Technische Abhängigkeiten

- Story 3.1–3.7 (Domain, Flyway, Seed-Patterns)
- Story 12.1–12.4 (Workspace-Scope auf Projekten)
- Nächste freie Migration: **V12** (Schema), **V13** (Seed)

#### Relevante bestehende Module

| Bereich | Referenz |
|---|---|
| Controller-Pattern | `ProjectController` — `/capacity`, `/issues-actions` |
| Assembler | `ProjectIssuesCapacityAssembler`, `ProjectTrendsAssembler` |
| DTO-Stil | `ProjectCapacityDto`, `ProjectIssuesActionsDto` |
| Migration | `V6__project_issues_capacity.sql`, `V7__mock_seed_issues_capacity.sql` |
| Domain | `Project`, `ProjectCapacitySummary` |
| Service | `ProjectKpiService`, `DefaultProjectQueryService` |

#### Testanforderungen

- `ProjectControllerIntegrationTest`: Happy path, 404 fremdes Projekt, Empty-State
- `ProjectIsoManagementAssemblerTest`: Mapping, max. 2 Abweichungen, Label-Generierung
- ArchUnit (optional): DTO/Entity in erlaubten Packages

#### Out of Scope

- Frontend-Komponenten
- ApprovedProjectDataReader-Erweiterung
- Schreib-Endpoints (POST/PUT/PATCH)
- Validierung/Business-Rules über Seed hinaus (keine CR-Genehmigungslogik)

---

### Story 15.2 — ISO Management UI

**Als** Führungskraft oder Projektleiter  
**möchte ich** die ISO-Steuerungsfelder als kompakte Cards auf der Projekt-Detailseite sehen  
**damit** ich Management-Practices auf einen Blick erfasse, ohne die Seite zu überladen.

#### Ziel

Neue Section-Komponente auf der Projekt-Detailseite zwischen Berichtsstandsvergleich und Team & Kapazität — fünf gleichartige Cards, CGI-EDS-konform, responsive, mit Loading/Empty/Error-States.

#### Scope

- Komponente `app-project-iso-management-section` (standalone Angular)
- Einbindung in `project-detail-page.component.html` **zwischen** `#fact-report-comparison` und `#fact-team-capacity`
- Section-Titel: **„ISO 21502 – ergänzende Steuerungsfelder“** (kein Compliance-Claim)
- Untertitel optional: kurzer Hinweis „Ausgewählte Management Practices — keine Normzertifizierung“
- API-Anbindung: `ProjectApiService.getProjectIsoManagement(id)` — **kein Frontend-Mock als finale Datenquelle**
- Fünf Cards in CSS-Grid:
  1. **Nutzen & Benefits** — erwarteter Nutzen, Einheit, realisierter Nutzen %, Status-Badge
  2. **Scope & Abweichungen** — Scope-Status, max. 2 Abweichungen (Liste), Trend-Label
  3. **Change Requests** — Zähler (gesamt/offen/in Prüfung/genehmigt), Impact-Zeilen Termin/Kosten/Scope
  4. **Qualität** — Quality Status, Defects, Test-/Abnahmestatus, Fortschritt %
  5. **Stakeholder & Kunde** — Sponsor/Kunde, Stakeholder-Status, Eskalationsstatus, optionales Steering-Datum
- Desktop (≥1200px): 5 Cards nebeneinander (gleiche Mindestbreite, gleiche Card-Höhe soweit möglich)
- Tablet: 2–3 Spalten; Mobil: 1 Spalte
- Status-Farben über bestehende `app-status-badge` / semantische Tokens (`status-on`, `status-watch`, `status-crit`) — **keine** Donuts/Gauges
- Unabhängiges Laden (eigener HTTP-Call, eigener Loading/Error-State — Pattern: `project-team-capacity-section`, `project-report-comparison`)
- `id="fact-iso-management"` als Detail-Anker für AI Facts
- WCAG: Section-Heading, Card-Labels, sr-only wo nötig, Kontrast gemäß DESIGN.md

#### Acceptance Criteria

1. **Position:** Gegeben Projekt-Detailseite, dann erscheint ISO-Section **nach** Berichtsstandsvergleich und **vor** Team & Kapazität im DOM und visuell.
2. **Titel:** Gegeben gerenderte Section, dann sichtbarer Titel exakt „ISO 21502 – ergänzende Steuerungsfelder“; nirgends „ISO-konform“ o. ä.
3. **Desktop-Layout:** Gegeben Viewport ≥1200px und geladene Daten, dann 5 Cards in einer Reihe (CSS Grid/Flex mit Wrap-Fallback nur bei Overflow).
4. **Responsive:** Gegeben Viewport <768px, dann Cards untereinander ohne horizontalen Scroll.
5. **Loading:** Gegeben langsamer API-Call, dann `aria-live="polite"`-Hinweis „ISO-Steuerungsfelder werden geladen …“ ohne Layout-Sprung der Nachbar-Sections.
6. **Error:** Gegeben API-Fehler, dann Alert mit `{ message }` aus API-Fehlerformat und Button „Erneut laden“.
7. **Empty:** Gegeben `dataAvailable: false`, dann definierter Empty-Text (z. B. „Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.“) — keine leeren Card-Hüllen mit „undefined“.
8. **Cards — Inhalt:** Gegeben vollständige API-Daten, dann zeigt jede Card alle fachlichen Felder aus Story 15.1 kompakt (Label/Wert-Paare, keine Chart-Widgets).
9. **Benefits-Status:** Gegeben `GREEN`/`AMBER`/`RED`, dann `app-status-badge` mit ausgeschriebenem Label (z. B. „Auf Kurs“ / „Beobachten“ / „Kritisch“).
10. **Scope-Trend:** Gegeben `IMPROVING`/`STABLE`/`DETERIORATING`, dann deutschsprachiges Trend-Label (z. B. „Verbessernd“ / „Stabil“ / „Verschlechternd“) — textuell, kein Sparkline-Chart.
11. **Stakeholder optional:** Gegeben `lastSteeringDate: null`, dann kein leeres Datumsfeld; bei gesetztem Datum de-DE-Format.
12. **Keine Regression:** Bestehende Sections (KPI, Issues, Berichtsvergleich, Kapazität, Phasen) unverändert in Reihenfolge und Verhalten.
13. **Kein KPI-Overload:** Gegeben ISO-Section, dann keine zusätzlichen großen KPI-Hero-Zahlen oberhalb der Cards und keine neuen Chart-Komponenten.

#### Technische Abhängigkeiten

- Story 15.1 (API + DTOs)
- Story 2.4 (Layout-Raster), 7.3 (`status-badge`), 10.1–10.3 (unabhängiges Laden, A11y)

#### Relevante bestehende Komponenten

| Komponente | Nutzung |
|---|---|
| `project-report-comparison` | Card-Grid-Pattern, Loading/Error |
| `project-team-capacity-section` | Section-Header, Grid, Facts-Badge |
| `project-kpi-section` | API-Service-Pattern |
| `status-badge` | Benefits/Quality/Stakeholder-Status |
| `ProjectApiService` | Neuer GET-Wrapper |

#### Testanforderungen

- `project-iso-management-section.component.spec.ts`: Loading, Success (5 Cards), Empty, Error, Retry
- `project-api.service.spec.ts`: neuer Endpoint-Mapping-Test
- Visueller Smoke: Detailseite mit Seed-Projekt rendert ohne Konsole-Fehler

#### Out of Scope

- Inline-Bearbeitung der Felder
- Drill-down zu CR-Listen oder Defect-Backlogs
- KI-Panel-Integration (bleibt Epic 9)
- Portfolio-Übersicht

---

### Story 15.3 — Integration, Tests & AI Facts

**Als** Product Owner  
**möchte ich** ISO-Steuerungsfelder in Tests, Security und der KI-Facts-Schicht abgesichert wissen  
**damit** Regressionen vermieden werden und der KI-Assistent die neuen Fakten korrekt nutzen kann.

#### Ziel

Querschnittliche Absicherung: Backend-/Frontend-Tests, Workspace-Isolation, Integration in `ApprovedProjectDataReader` / Project Facts, Regression-Suite und Production Build.

#### Scope

- **ApprovedProjectDataReader:** ISO-Fakten in `JpaApprovedProjectDataReader.readApprovedContext()` aufnehmen
  - Entfernen/Ersetzen des Platzhalters `missing: QUALITY — Defect-, Testabdeckungs- und Abnahmedaten fehlen`
  - Neue Facts mit `detailAnchor: "fact-iso-management"` und Kategorien z. B. `ISO_BENEFITS`, `ISO_SCOPE`, `ISO_CHANGE`, `ISO_QUALITY`, `ISO_STAKEHOLDER`
  - Beispiel-`factId`s: `iso.benefits.realizedPercent`, `iso.scope.trend`, `iso.changes.openCount`, `iso.quality.criticalDefects`, `iso.stakeholders.escalationStatus`
  - Bei fehlenden ISO-Daten: `MissingDataItemDto` mit präzisem Hinweis (nicht generisch „QUALITY fehlt“)
- **AI Evidence:** Facts erscheinen in Project-AI-Kontext; Evidence-Validator akzeptiert neue `factId`-Präfixe `iso.*`
- **Security:** Integrationstest — Nutzer A sieht ISO-Daten nur für Workspace-Projekte; Fremd-UUID → 404
- **Regression:** Bestehende Tests grün; `ProjectAiControllerIntegrationTest` erweitert (ISO-Facts in Kontext wenn Seed vorhanden)
- **Production Build:** `mvn -q verify` (Backend) und `npm run build` (Frontend) erfolgreich
- **Dokumentation:** Story-Status in `sprint-status.yaml`; Epic-15-Retrospective optional

#### Acceptance Criteria

1. **AI Facts — vorhanden:** Gegeben Projekt mit ISO-Seed, wenn `readApprovedContext`, dann mindestens 8 Facts mit Prefix `iso.` und `detailAnchor` `fact-iso-management`.
2. **AI Facts — fehlend:** Gegeben Projekt ohne ISO-Daten, dann `MissingDataItemDto` Kategorie `ISO_MANAGEMENT` mit verständlichem Text; kein erfundener Fact.
3. **AI Q&A:** Gegeben Seed-Projekt, wenn Project-AI-Analyse/Q&A, dann können Antworten ISO-Facts referenzieren (`evidenceFactIds` enthält `iso.*` wenn relevant) — bestehende Evidence-Validation bleibt grün.
4. **Isolation:** Gegeben zwei Workspaces, wenn Nutzer A Projekt-ID von Workspace B abfragt (`/iso-management`), dann `404` — Test in `ProjectControllerIntegrationTest` oder dediziertem Isolationstest.
5. **Frontend Regression:** Alle bestehenden `project-*-section` Specs grün.
6. **Backend Regression:** `ProjectControllerIntegrationTest`, `JpaApprovedProjectDataReader`-Tests (neu oder erweitert) grün.
7. **Build:** CI-lokal: Backend `mvn verify`, Frontend `ng build --configuration=production` ohne Fehler.
8. **Keine Breaking Changes:** Bestehende AI-Facts (`kpi.*`, `report.*`, `milestone.*`) unverändert in Struktur und Anzahl für Projekte ohne ISO-Daten.

#### Technische Abhängigkeiten

- Story 15.1, 15.2
- Story 9.1 (`ApprovedProjectDataReader`), 12.4 (Isolationstests)

#### Relevante bestehende Module

| Modul | Datei |
|---|---|
| AI Facts Reader | `JpaApprovedProjectDataReader.java` |
| Evidence Validator | `AiEvidenceValidator.java` |
| AI Integration Tests | `ProjectAiControllerIntegrationTest.java` |
| Isolation Tests | `ProjectControllerIntegrationTest`, Epic-12-Isolation-Suite |
| Frontend Build | `angular.json`, `project-detail-page.component.spec.ts` |

#### Testanforderungen

| Test | Mindestabdeckung |
|---|---|
| `JpaApprovedProjectDataReaderTest` (neu) | ISO-Facts + Missing-Path |
| `ProjectAiControllerIntegrationTest` | Kontext enthält ISO-Facts |
| `ProjectControllerIntegrationTest` | Workspace-404 für `/iso-management` |
| `project-iso-management-section.component.spec.ts` | aus 15.2 |
| Gesamt-Suite | keine neuen Failures |

#### Out of Scope

- Portfolio-AI-Erweiterung
- Neue Management-Insight-Regeln in `kpi.insights`
- Performance-Optimierung / Caching (ISO-Daten werden mit Detailseite geladen)
- E2E-Browser-Tests (optional, nicht verpflichtend)

---

## Epic-15 Definition of Done

- [x] Stories 15.1–15.3 `done`
- [x] FR-33 in PRD-Addendum und FR-Coverage-Map (`epics.md`) verknüpft
- [x] UX-Komponente `iso-management-section` in DESIGN.md referenziert
- [x] Flyway V12/V13 angewendet; keine Änderungen an V1–V11
- [x] Detailseiten-Reihenfolge dokumentiert und umgesetzt
- [x] ISO-Compliance-Claims in UI und Docs ausgeschlossen
- [x] Production Build grün
