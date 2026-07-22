---
story_key: 8-portfolio-ki
epic: 8
---

# Epic 8 — KI-Portfolioanalyse

Status: done

## Scope

- Freigegebene Portfolio-Fakten über `PortfolioKpiReader` + `PortfolioTableReader` (AD-2)
- `GET /api/portfolio/ai/trend-analysis` mit typisiertem Insight-Modell (Breaking Change vs. `{ text, topProjects }`)
- `app-portfolio-ai-panel` auf der Portfolio-Seite (Loading/Error/Empty/Retry) — Titel „Portfolio-Muster und systemische Risiken“
- Mock-Default; Gemini optional über `APP_AI_PROVIDER=gemini`
- Deterministischer Pattern-Detector vor LLM; nur belegte projektübergreifende Muster

## Endpoints

- `GET /api/portfolio/ai/trend-analysis`

## Course Correction (2026-07-17)

**Shift Story 8.2 / 8.4:** Weg vom narrativen Trendtext + Top-3-Handlungsbedarf hin zu typisierten `PortfolioInsight`-Karten (`type`, `finding`, `managementImplication`, `evidence[]`, `confidence`, `dataQuality`, betroffene Projekte).

**Aktiver Mustertyp-Scope:** `DETERIORATING_TREND`, `REPORTING_PATTERN` — nur Typen mit belastbarer Seed-/Snapshot-Basis. **Deaktiviert** (keine künstlichen Seed-Belege): `CAPACITY_CONFLICT`, `SHARED_DEPENDENCY`, `MEASURE_INEFFECTIVENESS`. Seed kann später unabhängig vom Detector nachgerüstet werden.

**Implementierungsreihenfolge Schritt 4:** Erst DTO + alle Consumer synchron (Mock, Gemini, Tests, Panel), danach Pattern-Detector mit den zwei aktiven Typen befüllen — kein Zwischenzustand mit abweichendem Response-Shape.

## Change Log

- 2026-07-16: Epic 8 implementiert (Mock + Gemini-fähig)
- 2026-07-17: Course Correction — Insight-Modell, reduzierter Mustertyp-Scope (siehe oben)
- 2026-07-17: Breaking Change Response → `insights[]`; Pattern-Detector (`DETERIORATING_TREND`, `REPORTING_PATTERN`); Panel-Titel „Portfolio-Muster und systemische Risiken“. Status bleibt **review**.
- 2026-07-20: Code-Review Chunks Backend AI + Frontend Portfolio-KI abgeschlossen; Patches applied. Status → **done**.
- 2026-07-21: Abschluss-Review — keine offenen Patch-Findings; Backend- und Frontend-Tests grün. Epic 8 freigegeben.

### Review Findings

- [x] [Review][Patch] Portfolio-KI ignoriert aktive Filter — `portfolio-ai-panel` ruft API ohne Filter-Query-Params auf, obwohl Backend Filter unterstützt [`frontend/src/app/features/portfolio/portfolio-ai-panel.component.ts:31-32`]
- [x] [Review][Patch] Portfolio-KI lädt nicht neu bei Filterwechsel — kein Abonnement auf `PortfolioFilterService` [`frontend/src/app/features/portfolio/portfolio-ai-panel.component.ts:24-26`]
- [x] [Review][Patch] Kandidaten-Fakten unvollständig — `progressPercent`/`criticalIssueCount` im Prompt, aber nicht in `evidenceFactIds`/Facts [`ApprovedPortfolioContextAssembler.java:70-96`]
- [x] [Review][Patch] Kanonischer Projektname — Top-3 nutzt LLM-`projectName` statt Backend-Name [`PortfolioAiAnalysisService.java:86-90`]
- [x] [Review][Patch] Race bei erneutem `load()` — parallele Requests können veraltete Antwort anzeigen [`portfolio-ai-panel.component.ts:28-48`]
- [x] [Review][Defer] Portfolio-Fließtext ohne Evidence-Validierung — narrative KI-Zusammenfassung bewusst ohne Fact-Bindung im MVP [`PortfolioAiAnalysisService.java:97-106`] — deferred, design choice MVP
- [x] [Review][Defer] Kein Rate-Limiting/Cache für Portfolio-Trendanalyse — Kostenrisiko, nicht AC-blockierend [`PortfolioAiAnalysisService.java:45-54`] — deferred, follow-up hardening

#### Review Findings (2026-07-20, Chunk Backend AI)

- [x] [Review][Patch] LLM nur Formulierung der Detector-Treffer — bei gleichem Typ LLM-Text (title/finding/implication/action) übernehmen, Evidence/`affectedProjectIds` vom Detector behalten; keine LLM-only-Typen [`PortfolioAiAnalysisService.java`] — Entscheidung 2026-07-20: Option A; applied
- [x] [Review][Patch] Portfolio-Evidence ohne Fact-/sourceField-Allowlist — `validateInsight` akzeptiert freie label/value [`PortfolioAiAnalysisService.java`] — applied
- [x] [Review][Patch] Evidence ohne Bezug zu `affectedProjectIds` bleibt gültig [`PortfolioAiAnalysisService.java`] — applied
- [x] [Review][Patch] `aiGenerated=true` bei reinem Detector-Fallback nach Provider-Fehler [`PortfolioAiAnalysisService.java`] — applied
- [x] [Review][Patch] `confidence`/`dataQuality` nicht auf erlaubte Enum-Werte normalisiert [`PortfolioAiAnalysisService.java`] — applied
- [x] [Review][Patch] Provider-Fallback fängt nur `GeminiTransportException`/`IllegalStateException` — andere Runtime-Fehler verlieren Detector-Insights [`PortfolioAiAnalysisService.java`] — applied
- [x] [Review][Patch] REPORTING_PATTERN hängt Ist-Budget als Beleg an, obwohl nur Risikoanstieg detektiert wird [`PortfolioPatternDetector.java`] — applied
- [x] [Review][Patch] Null-Terminabweichung als 0 → Fehlwerte erzeugen falsche DETERIORATING_TREND-Signale [`PortfolioPatternDetector.java`] — applied
- [x] [Review][Patch] Unbekannter Status-Rank 0 verfälscht Status-Vergleich [`PortfolioPatternDetector.java`] — applied
- [x] [Review][Patch] DETERIORATING-Finding nutzt nur Datumsintervall des ersten betroffenen Projekts [`PortfolioPatternDetector.java`] — applied
- [x] [Review][Patch] Snapshot-Datums-Ties ohne sekundären Sortierschlüssel [`JpaPortfolioReportTrendReader.java`] — applied
- [x] [Review][Patch] HTTPS-Check greift nicht bei scheme-loser Gemini-Base-URL [`HttpGeminiApiTransport.java`] — applied
- [x] [Review][Defer] `extractJsonPayload` Brace-Counting ohne String-Kontext — JSON mit `{`/`}` in Werten kann fehlschneiden [`GeminiAiModelClient.java:270-289`] — deferred, hardening
- [x] [Review][Defer] Fehlende Unit-Tests für `JpaPortfolioReportTrendReader` — deferred, follow-up coverage

#### Review Findings (2026-07-20, Chunk Frontend Portfolio-KI)

- [x] [Review][Patch] Sublabel anhand `aiGenerated` — z. B. „Gemini“ vs. „Regelbasiert“; Badge „Musteranalyse“ bleibt [`portfolio-ai-panel.component.html`] — Entscheidung 2026-07-20: Option 1; applied
- [x] [Review][Patch] Projekt-Links per Index ohne Längen-/Null-Guard — `affectedProjectNames[i]` ↔ `affectedProjectIds[i]`; bei Drift/`undefined`/leerer ID falsche Navigation [`portfolio-ai-panel.component.html`] — applied
- [x] [Review][Patch] `@for` track über Projektnamen statt IDs — Duplikatnamen brechen DOM-Zuordnung [`portfolio-ai-panel.component.html`] — applied
- [x] [Review][Patch] Irreführender Empty-State nach Client-Filter — API liefert Insights, `displayableInsights` filtert alle weg, Text behauptet „keine Muster erkannt“ [`portfolio-ai-panel.component.ts` / `.html`] — applied
- [x] [Review][Patch] `displayableInsights` defensiv unvollständig — kein `Array.isArray`, keine Null-Elemente, kein Check auf `affectedProjectNames`, keine Beschränkung auf aktive Typen `DETERIORATING_TREND`/`REPORTING_PATTERN` [`portfolio-ai-panel.component.ts`] — applied
- [x] [Review][Patch] In-flight Requests bei Filterwechsel nicht abgebrochen — nur `loadGeneration` verwirft UI; Endpoint-Last bleibt [`portfolio-ai-panel.component.ts`] — applied (`switchMap`)
- [x] [Review][Patch] Retry bei `AI_DISABLED` weiterhin sichtbar — sinnlose Aktion [`portfolio-ai-panel.component.html`] — applied
- [x] [Review][Patch] HTTP-200 mit `null`/leerem Body wird als Success ohne Inhalt gesetzt [`portfolio-ai-panel.component.ts`] — applied
- [x] [Review][Patch] Konfidenz/Datenqualität als EN-Enums in DE-UI — Mapping fehlt [`portfolio-ai-panel.component.html`] — applied
- [x] [Review][Patch] `reportDate` unformatiert (ISO roh) [`portfolio-ai-panel.component.html`] — applied
- [x] [Review][Patch] Evidence-`track` über `label+value` kollidiert bei gleichen Belegzeilen; Null-Elemente in `evidence` ungeschützt [`portfolio-ai-panel.component.html`] — applied
- [x] [Review][Patch] Verwaiste Styles nach Top-3-Umbau (`.portfolio-ai__top`, `__subtitle`, teils `__text`) [`portfolio-ai-panel.component.scss`] — applied
- [x] [Review][Patch] Spec-Lücken — kein `routerLink`-Test (Story 8.4), `AI_PROVIDER_ERROR`-Fall entfernt, keine Array-Drift-/Null-Body-Tests [`portfolio-ai-panel.component.spec.ts`] — applied
- [x] [Review][Patch] Kein `takeUntilDestroyed` — Signal-Writes möglich nach Destroy bei offenem Request [`portfolio-ai-panel.component.ts`] — applied
