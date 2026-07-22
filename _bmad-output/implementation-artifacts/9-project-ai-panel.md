---
story_key: 9-project-ai-panel
epic: 9
---

# Epic 9 — Projekt-Assistent

Status: done

## Scope

- `ApprovedProjectDataReader` mit stabilen `factId`s
- `GET /api/projects/{id}/ai/analysis?refresh=`
- `POST /api/projects/{id}/ai/questions`
- Mock- und Gemini-`AiModelClient`
- Evidenz-Validator (unbekannte Fact-IDs werden verworfen)
- `app-project-ai-panel` auf der Detailseite aktiv (Tabs Überblick / Maßnahmen / Fragen)

## Produktstand (2026-07-16)

- Detailseite rendert `app-project-ai-panel` (Placeholder ersetzt)
- Maßnahmen nur als Vorschläge, kein Persistieren
- Provider: `APP_AI_PROVIDER=mock|gemini`, Key nur `GEMINI_API_KEY`

## Change Log

- 2026-07-16: Projekt-Assistent implementiert
- 2026-07-16: Panel auf Detailseite aktiviert; Gemini-Anbindung ergänzt
- 2026-07-17: Managementbewertung mit Klartext-Belegen (≥2), Prioritäten mit managementImplication/requiredDecision; Insights ohne Belege ausgeblendet.
- 2026-07-17: Detailseiten-Nacharbeit — Entscheidungsblock bei `escalationNeeded`, KI ohne generische Maßnahmenlisten, Klartext-Belege, UI-Bereinigung ([OFFEN]/UUID/Disclaimer/Ist-Kosten).
- 2026-07-17: UI Berichtsstandsvergleich (Vergleichskarten) und Projektzeitplan & Meilensteine (Gantt-Projektmodus, Meilenstein-Karten).
- 2026-07-20: Code-Review Chunk Frontend Projekt-Detail abgeschlossen; Patches applied. Status → **done**.
- 2026-07-21: Abschluss-Review — keine offenen Patch-Findings; Backend- und Frontend-Tests grün. Epic 9 freigegeben.

### Review Findings

- [x] [Review][Patch] Chat behandelt `AI_DISABLED` nicht wie Analyse-Ladung — `sendQuestion()` setzt immer `chatStatus=error` statt `disabled` [`project-ai-panel.component.ts:174-178`]
- [x] [Review][Patch] Race bei `projectId`-Wechsel — laufende Analyse kann Daten des vorherigen Projekts anzeigen [`project-ai-panel.component.ts:96-113`]
- [x] [Review][Patch] Keine Fragen-Längenbegrenzung — große Payloads gehen ungebremst an Gemini [`ProjectAiAnalysisService.java:66-71`]
- [x] [Review][Patch] `parseJson` Markdown-Extraktion fehleranfällig — `lastIndexOf('}')` bei verschachteltem JSON [`GeminiAiModelClient.java:204-209`]
- [x] [Review][Patch] SSRF-Risiko über `GEMINI_API_BASE_URL` — keine Allowlist für Gemini-Host [`HttpGeminiApiTransport.java:106-107`, `application.yml:43`]
- [x] [Review][Defer] Q&A-Antworttext nicht vollständig validiert — Evidence-IDs werden gefiltert, Freitext bleibt Modell-output [`AiEvidenceValidator.java:83-90`] — deferred, MVP guard via evidenceFactIds
- [x] [Review][Defer] Prompt-Injection-Härtung (Delimiter/Sanitisierung) — Längenlimit als Minimum, vollständige Härtung später [`GeminiAiModelClient.java:112-121`] — deferred, security hardening

#### Review Findings (2026-07-20, Chunk Backend AI)

- [x] [Review][Patch] Evidence ohne `sourceField` oder mit abweichendem `value` wird akzeptiert — Halluzinationen möglich [`AiEvidenceValidator.java`] — applied
- [x] [Review][Patch] Priorität ohne Titel (`title` blank) wird nicht verworfen [`AiEvidenceValidator.java`] — applied
- [x] [Review][Patch] Priorität mit nur erfundenen Evidence-Items und leeren `evidenceFactIds` kann bestehen bleiben [`AiEvidenceValidator.java`] — applied

#### Review Findings (2026-07-20, Chunk Frontend Projekt-Detail)

- [x] [Review][Patch] KI-Disclaimer wieder rendern — Footer mit `data.disclaimer` [`project-ai-panel.component.html`] — Entscheidung 2026-07-20: Option 1; applied
- [x] [Review][Patch] Q&A unabhängig von Analyse-Fehler — Fragen-Tab auch bei Analyse-error/disabled nutzbar [`project-ai-panel`] — Entscheidung 2026-07-20: Option 1; applied
- [x] [Review][Patch] Projekt-ID im Header wieder anzeigen [`project-detail-page.component.html`] — Entscheidung 2026-07-20: Option 1; applied
- [x] [Review][Patch] Chat trotz `AI_DISABLED`/`error` weiter sendbar — Inputs/Chips ohne Guard [`project-ai-panel.component.ts` / `.html`] — applied
- [x] [Review][Patch] Chat-Race bei Projektwechsel A→B→A — keine `chatGeneration`, verwaiste Antworten möglich [`project-ai-panel.component.ts`] — applied
- [x] [Review][Patch] `insufficientEvidence` ungenutzt — Story 9.3 Hinweis fehlt [`project-ai-panel.component.ts`] — applied
- [x] [Review][Patch] `jumpToFact` auf totes `fact-insights`; Issues-Fakten ohne Anker trotz `#fact-issues-actions` [`project-ai-panel.component.ts`] — applied
- [x] [Review][Patch] `evidenceFactIds` null crasht Details; leere Priority-Felder ohne `formatOptionalField` [`project-ai-panel.component.html`] — applied
- [x] [Review][Patch] `@for` track über `priority.rank` / `action.title`; angezeigte Ränge nach Filter lückenhaft [`project-ai-panel.component.html`] — applied
- [x] [Review][Patch] `isConcreteAction` — Whitespace-`evidenceFactIds` und ASCII „Massnahme“ nicht abgedeckt [`project-ai-panel.component.ts`] — applied
- [x] [Review][Patch] Stale-Response-Race in Issues/Report/Phasen — kein Generations-Guard bei `projectId`-Wechsel — applied
- [x] [Review][Patch] `formatDate` zeitzonenanfällig für ISO-Datums-only [`project-issues-actions-section.component.ts`] — applied
- [x] [Review][Patch] Issue-`metrics` und leere `cause`/`priority` ungeschützt; Überschrift „Benötigte Entscheidung“ Singular [`project-issues-actions-section`] — applied
- [x] [Review][Patch] Berichtsvergleich: leere Kartenliste, Roh-Datumsanzeige, fehlende NaN-Guards [`project-report-comparison`] — applied
- [x] [Review][Patch] Kapazität Empty-State entfernt — Section verschwindet bei leeren Rollen (Story 7.5) [`project-team-capacity-section`] — applied
- [x] [Review][Patch] Meilenstein-Karten ohne Ist/Prognose; `formatDeviation(1)` „Tage“; track über Name; overdue+done [`project-phases-section`] — applied
- [x] [Review][Patch] Gantt `screenReaderSummary`: bei `delayDays<=0` „Planende“ = Forecast statt Plan [`gantt-timeline.component.ts`] — applied
- [x] [Review][Patch] Chat-Fehler ohne expliziten Retry-Button (Story 10.2) [`project-ai-panel.component.html`] — applied
- [x] [Review][Defer] Kein eigener Forecast-Bereich — Produktstand deckt Managementbewertung+Q&A ab; Story 9.2/9.4 Intent offen — deferred, productstand
- [x] [Review][Defer] Maßnahmen nur `evidenceFactIds`, kein Klartext-Evidence-Feld am Action-DTO — deferred, needs API
- [x] [Review][Defer] Gantt-Phasensegmente nur Plantermine — Ist/Prognose auf Segmentebene — deferred, follow-up Story 6.4
- [x] [Review][Defer] Meilenstein-Blockaden `[OFFEN]` in Spec — deferred, Spec-OFFEN
