# Story 10.1: Unabhängiges Laden Fakten/KI

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

As a Nutzer,
I want KPIs und Faktenbereiche auch bei KI-/Gemini-Ausfall weiterhin laden und nutzen,
so that ich Portfolio- und Projektentscheidungen ohne Abhängigkeit von der KI-Spalte treffen kann (FR-15, AD-7).

## Acceptance Criteria

1. **Gegeben** simulierter Gemini-/KI-Ausfall (HTTP 503 oder `AI_PROVIDER_ERROR` auf KI-Endpoints), **wenn** Portfolio-Seite offen ist, **dann** laden Filter, Portfolio-KPIs, Trends, Gantt und Projekttabelle weiterhin erfolgreich und bleiben bedienbar — unabhängig vom KI-Panel-Zustand.
2. **Gegeben** simulierter Gemini-/KI-Ausfall auf Projekt-KI-Endpoints (`/ai/analysis`, optional `/ai/questions`), **wenn** Projekt-Detailseite offen ist, **dann** laden Stammdaten und alle Fakten-Sektionen (KPIs, Issues/Maßnahmen, Berichtsvergleich, Kapazität, Phasen) weiterhin erfolgreich.
3. **Gegeben** getrennte Datenströme, **dann** besitzt jedes Fakten-Panel und jedes KI-Panel eigene Signals für `loading` / `success` / `error` (bzw. `disabled` nur für KI) — **keine** gemeinsame Parent-Observable/-Signal, die Error oder Invalidation zwischen Fakten und KI koppelt (AD-7).
4. **Gegeben** KI-Fehler oder -Retry, **dann** werden Fakten-Streams weder abgebrochen noch in einen Error-State versetzt (und umgekehrt: Fakten-Retry invalidiert nicht KI-Zustand außer über bewusste, getrennte Reload-Aufrufe).
5. **Tests:** Mindestens ein Page-Level-Frontend-Test (Portfolio und Projekt-Detail), der KI-Endpoints mit 503 stubbt und zugleich erfolgreiche Fakten-Antworten assertiert; Backend-Nachweis: KPI-/Fakten-Endpoints bleiben erreichbar, wenn Gemini-Transport 503 liefert (oder vorhandener Gemini-Stub-Test wird um diesen Unabhängigkeits-Nachweis ergänzt).

## Tasks / Subtasks

- [ ] Audit Ist-Zustand gegen AD-7 (AC: #3, #4)
  - [ ] Portfolio: `portfolio-page` (Shell), `portfolio-kpi|trends|gantt|table-section`, `portfolio-filter-bar`, `portfolio-ai-panel` — prüfen, dass kein `forkJoin`/`combineLatest` Fakten+KI koppelt und kein gemeinsamer Page-Error-State existiert
  - [ ] Projekt: `project-detail-page`, Fakten-Sektionen, `project-ai-panel` — gleiche Prüfung; Stammdaten-Error darf KI/Fakten nicht global blockieren (Layout bereits getrennt)
  - [ ] Shared Services: `PortfolioFilterService` darf nur Filter-Params teilen, **nicht** Error/Loading zwischen KPI und KI
- [ ] Gaps schließen (AC: #1–#4) — nur wo Audit Abweichungen findet
  - [ ] Gekoppelte Error-/Loading-States entkoppeln
  - [ ] Sicherstellen: KI-Retry (`load()` / `loadAnalysis()`) triggert keine Fakten-Reloads und setzt keine Fakten-Signals
  - [ ] Keine neue globale State-Lib (kein NgRx); kein Rewrite auf `rxResource`/`httpResource` in dieser Story
- [ ] Frontend-Nachweise (AC: #1, #2, #5)
  - [ ] `portfolio-page.component.spec.ts`: Test „KI 503 → Fakten trotzdem success“ (KPIs/Tabelle/Gantt/Trends sichtbar; AI-Panel error/disabled)
  - [ ] `project-detail-page.component.spec.ts`: analog für Master-Data + Fakten-Sektionen bei AI-Analysis 503
  - [ ] Bestehende Panel-Specs (`portfolio-ai-panel`, `project-ai-panel`) beibehalten; nicht ersetzen, sondern Page-Level ergänzen
- [ ] Backend-Nachweis Gemini Stub 503 (AC: #5)
  - [ ] Vorhandene Gemini-Integrationstests (`ProjectAiControllerGeminiIntegrationTest` o. ä.) nutzen/erweitern: bei Transport-503 bleiben `/api/portfolio/kpis` (bzw. Projekt-KPI) `200`
  - [ ] **Nicht** Portfolio-Detector-Fallback als FR-15-Verstoß „fixen“ — Fallback ist Epic-8-Design; FR-15 fordert KPI-Verfügbarkeit, nicht zwingend leeres KI-Panel
- [ ] Abgrenzung Story 10.2 beachten
  - [ ] Keine A11y-Politur (`aria-live`, Fokus-Ring) und keine Fakten-Fehler-UX-Überarbeitung — das ist 10.2
  - [ ] Vorhandene DE-Fehlermeldungen / Retry in KI-Panels belassen, solange Unabhängigkeit gilt

## Dev Notes

### Epic-Kontext

Epic 10 schließt Querschnittsthemen nach MVP-Features (Epics 1–9): Fehlerbehandlung, Barrierefreiheit, Qualität. Story 10.1 ist die **architekturelle Absicherung** von FR-15/AD-7; 10.2 baut die UX-Feinschliff-Schicht darauf.

Abhängigkeiten laut Spec: **8.3** (Portfolio-KI-Panel), **9.4** (Projekt-KI-Panel) — beide done.

### Ist-Zustand (kritisch — nicht neu erfinden)

Die Architektur ist **weitgehend bereits AD-7-konform**. Diese Story ist primär **Audit + Lückenschluss + Page-Level-Nachweis**, kein Greenfield-Umbau.

| Bereich | Aktueller Stand | Was Story 10.1 verlangt |
|---|---|---|
| `PortfolioPageComponent` | Reine Shell, kein HttpClient (AD-10) | Beibehalten |
| Fakten-Sektionen Portfolio | Eigene `status`/`errorMessage` Signals + eigener API-Call je Section | Beibehalten; Unabhängigkeit von KI beweisen |
| `PortfolioAiPanelComponent` | Eigene Signals; `switchMap`+`takeUntilDestroyed`; Error über `resolveAiPanelError` | Beibehalten; Retry darf Fakten nicht anfassen |
| `ProjectDetailPageComponent` | Nur Stammdaten-Load; Fakten/KI als Child-Komponenten | Beibehalten |
| Projekt-Fakten-Sektionen | Je eigene LoadStatus-Signals | Beibehalten |
| `ProjectAiPanelComponent` | Eigene Analysis-/Chat-Signals; Q&A unabhängig von Analyse-Fehler (Epic-9-Review) | Beibehalten |
| Page-Specs | Smoke lädt Fakten+KI parallel; **kein** expliziter „KI 503 → Fakten OK“-Test | **Hier Lücke schließen** (SM-5) |
| Portfolio-Fakten Race-Guards | KPI/Trends/Gantt/Table: oft nur `take(1)`, kein `switchMap`/Generation | **Nicht** Scope von 10.1 (Filter-Stale ≠ FR-15); optional nur wenn Unabhängigkeits-AC bricht |
| `project-kpi-section` | Kein Generation-Guard (andere Projekt-Sections haben ihn) | Ebenfalls außerhalb 10.1, außer Stale-Antworten koppeln sichtbaren Error mit KI |

**Code-Audit-Fazit:** Weder `forkJoin` noch `combineLatest` im Frontend; KI blockiert Fakten weder technisch noch im Template. Eager Auto-Load aller Panels ist bewusst und OK für 10.1.

### Architektur-Compliance (MUST)

- **AD-7:** Getrennte RxJS-Streams pro Fakten-Bereich und pro KI-Panel; Angular Signals für lokalen UI-Zustand; **keine** gemeinsame Parent-Observable/-Signal für Error/Invalidation Fakten↔KI; kein NgRx.
- **AD-10:** Kein `HttpClient` in Page-/Präsentations-Shells — nur `core/api/*`-Services (`PortfolioApiService`, `AiApiService`, `ProjectApiService`, `ProjectAiApiService`).
- **AD-4 / FR-14:** Frontend ruft Gemini **nie** direkt auf — nur Backend-AI-Endpoints.
- **FR-15 / NFR-5 / SM-5:** Bei Gemini-Ausfall bleiben KPI-Funktionen nutzbar (Ziel: 100 % KPI-Verfügbarkeit bei simuliertem Ausfall); KI zeigt Fehlermeldung (oder Detector-Fallback auf Portfolio — beides OK solange Fakten laufen).
- **FR-8 / Filter:** `PortfolioFilterService` aktualisiert Fakten **und** KI über **getrennte** Streams (Params teilen = OK; Error/Loading teilen = Verstoß).
- **UX EXPERIENCE.md:** Getrennte Subscriptions; Failure-Path „Gemini down → KI-Fehlerpanel; Fakten nutzbar“; Skeleton Fakten vs. eigener KI-Ladezustand.

Quellen:
- [Source: `_bmad-output/planning-artifacts/architecture/.../ARCHITECTURE-SPINE.md` — AD-7]
- [Source: `_bmad-output/planning-artifacts/stories/stories-mvp.md` — Story 10.1]
- [Source: `_bmad-output/planning-artifacts/prds/.../prd.md` — FR-15]
- [Source: `_bmad-output/planning-artifacts/ux-designs/.../EXPERIENCE.md` — State Patterns / Flow 1 Failure]

### Anti-Patterns / Out-of-Scope (NICHT tun)

- **Nicht** Fakten und KI in einem `forkJoin`/`combineLatest` auf Page-Ebene bündeln.
- **Nicht** einen globalen `pageStatus`/`dashboardError` einführen, der beide Spalten steuert.
- **Nicht** TanStack Query / NgRx / neues State-Management einführen (AD-7 verbietet NgRx explizit; Stack ist RxJS + Signals).
- **Nicht** auf Angular `rxResource`/`httpResource` umbauen — Out-of-Scope; bestehendes `effect`+`subscribe`/`switchMap`-Muster beibehalten.
- **Nicht** Lazy/Deferred KI („erst nach Fakten laden“) einführen — das wäre neue Produktkopplung und kein AC von 10.1.
- **Nicht** Load-Coordinator-Service bauen, der Fakten+KI orchestriert — widerspricht dem dezentralen AD-7-Muster.
- **Nicht** Filter-Sync zwischen Fakten und KI entfernen (Story-8-Fix muss bleiben).
- **Nicht** Story-10.2-Scope (aria-live, Fakten-Fehler-State-Patterns, Caption-Kontrast, Chat-Retry-Politur) mitziehen.
- **Nicht** Portfolio-Detector-Fallback bei Gemini-Fehler entfernen „damit AI immer error zeigt“ — das ist kein AC von 10.1.
- **Nicht** Backend-KPI-Reader an AI-Services koppeln.
- **Nicht** Race-Guard-Härtung aller Portfolio-Sections als Pflichtaufgabe dieser Story (technische Schuld, orthogonal zu FR-15).

### Dateien (UPDATE erwartet / lesen vor Änderung)

**Frontend (primär):**
- `frontend/src/app/features/portfolio/portfolio-page.component.ts` (+ `.html`, `.spec.ts`)
- `frontend/src/app/features/portfolio/portfolio-kpi-section.component.ts` (+ siblings trends/gantt/table)
- `frontend/src/app/features/portfolio/portfolio-ai-panel.component.ts`
- `frontend/src/app/features/portfolio/portfolio-filter.service.ts` (nur Params-Sharing prüfen)
- `frontend/src/app/features/project/project-detail-page.component.ts` (+ `.html`, `.spec.ts`)
- `frontend/src/app/features/project/project-*-section.component.ts` / `project-report-comparison.component.ts`
- `frontend/src/app/features/project/project-ai-panel.component.ts`
- `frontend/src/app/core/layout/facts-ai-layout.component.ts` (Layout only — nicht mit State koppeln)
- `frontend/src/app/core/api/{portfolio,ai,project,project-ai}-api.service.ts`
- `frontend/src/app/shared/utils/ai-error.util.ts`

**Backend (Nachweis Gemini 503):**
- `backend/src/test/java/.../ProjectAiControllerGeminiIntegrationTest.java` (erweitern oder parallelen Unabhängigkeits-Test)
- Optional: Portfolio-KPI-Controller-IT + Gemini-Transport-Stub — KPI darf nicht von AI-Bean-Failure abhängen
- Services nur lesen, falls Audit Kopplung vermutet: `PortfolioAiAnalysisService`, `ProjectAiAnalysisService` (Fakten-Reader dürfen nicht durch AI-Exceptions blockiert werden)

### Testanforderungen

- Stack: Angular 22 + Vitest + `HttpTestingController` (bestehende Specs).
- Page-Test-Muster: alle Fakten-URLs `flush(success)`, KI-URL `flush(..., { status: 503, ... })` mit Error-Body (`AI_PROVIDER_ERROR` oder `AI_DISABLED` je Szenario), dann DOM/Signals der Fakten auf Erfolg prüfen.
- Backend: vorhandenes Gemini-Stub-Pattern in `ProjectAiControllerGeminiIntegrationTest` wiederverwenden (kein neuer Test-Framework).
- Regression: bestehende AI-Panel-Specs und Portfolio-Page-Smoke-Test müssen grün bleiben.

### Previous Story Intelligence

Aus Epic 8/9 (done) — übernehmen, nicht wiederholen:

- Portfolio-KI: Filter-Params, `switchMap` gegen In-flight-Races, `takeUntilDestroyed`, Retry bei `AI_DISABLED` ausblenden.
- Projekt-KI: Analysis- und Chat-Generations-Guards; Q&A unabhängig von Analyse-Fehler; Stale-Response-Guards in Fakten-Sektionen.
- Fehlertexte: `resolveAiPanelError` — nie Roh-„Failed to fetch“; feste DE-Meldungen im Portfolio-Panel.
- Review-Defer aus 9.x: Chat-Retry-Button als Story-10.2 markiert — hier nicht nachziehen außer Unabhängigkeit bricht.

Quellen: `_bmad-output/implementation-artifacts/8-portfolio-ki.md`, `9-project-ai-panel.md`.

### Git Intelligence

Aktuelle Linie: AI-Transport/Portfolio-KI/Projekt-Kapazität gehärtet (`feat(ai): fix Gemini transport...`), Epics 8–9 freigegeben. Story 10.1 baut auf getrennten Panels auf und liefert den **Querschnitts-Nachweis**, den die Feature-Stories noch nicht page-übergreifend abgesichert haben.

### Latest Tech Notes

- Projekt nutzt **Angular 22** + RxJS 7.8 + Signals — AD-7-Pattern beibehalten.
- Angular Resource API (`rxResource`) ist verfügbar, aber **kein Migrationsziel** dieser Story (Scope/Risk).

### Project Structure Notes

- Feature-Ordner: `features/portfolio`, `features/project`; Layout: `core/layout/facts-ai-layout`.
- API nur über `core/api`.
- Story-Datei-Konvention: `{epic}-{story}-{slug}.md` unter `_bmad-output/implementation-artifacts/`.

### References

- `_bmad-output/planning-artifacts/stories/stories-mvp.md` — Epic 10 / Story 10.1–10.4
- `_bmad-output/planning-artifacts/epics.md` — FR-15 → 10.1/10.2; UX-DR12
- `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-2026-07-13/ARCHITECTURE-SPINE.md` — AD-7, AD-10
- `_bmad-output/planning-artifacts/prds/prd-cgi-kpi-dashboard-2026-07-13/prd.md` — FR-15, SM-5
- `_bmad-output/planning-artifacts/ux-designs/ux-cgi-kpi-dashboard-2026-07-13/EXPERIENCE.md` — State Patterns, Flow 1 Failure
- `_bmad-output/implementation-artifacts/8-portfolio-ki.md`, `9-project-ai-panel.md`

## Dev Agent Record

### Agent Model Used

{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List
