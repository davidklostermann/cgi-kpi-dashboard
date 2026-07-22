## Acceptance-Audit — Frontend Portfolio-KI (Chunk 2)

**Gesamturteil:** Weitgehend konform mit Story **8.3 / 8.4**, Course Correction (Insight-Modell, Titel, ≥2 Projekte/Belege, max. 5 Karten, Belege eingeklappt, feste DE-Fehler + Retry, Filter/Reload). Keine harten Blocker-Verletzungen der Frontend-ACs im Happy Path. Residuale Abweichungen/Lücken:

---

- **Gemini-Sublabel fehlt im Panel**
  - **Verletzt:** Story 8.3 UX `ki-panel` / DESIGN.md (`Kopf-Badge …, Gemini-Sublabel, Footer-Disclaimer`)
  - **Evidenz:** Diff setzt Badge auf `Musteranalyse` und Titel auf „Portfolio-Muster…“, aber weder Template noch Spec-Test zeigen ein Gemini-Sublabel; Course Correction ersetzt nur „KI-Einschätzung“/Top-3, nicht das Sublabel.

- **Frontend-Vertrag erlaubt deaktivierte Mustertypen**
  - **Verletzt:** Course Correction / Story 8.2 — aktiver Scope nur `DETERIORATING_TREND`, `REPORTING_PATTERN`; deaktiviert u. a. `CAPACITY_CONFLICT`, `SHARED_DEPENDENCY`, `MEASURE_INEFFECTIVENESS`
  - **Evidenz:** `portfolio-ai.model.ts` typisiert alle genannten (plus `PHASE_VARIANCE`, `SYSTEMIC_RISK`) als `PortfolioInsightType`; `displayableInsights()` filtert nur nach Evidence/Projekte, nicht nach aktivem Typ; `typeLabel()` fällt für Nicht-Scope auf den Roh-Enum zurück.

- **Projektnavigation koppelt Name und Id per Array-Index**
  - **Verletzt / Risiko:** Story 8.4 AC — Klick auf Projektname navigiert zu Detail
  - **Evidenz:** Template `@for (name of insight.affectedProjectNames …)` mit `[routerLink]="['/projects', insight.affectedProjectIds[idx]]"`; bei Längen-/Reihenfolge-Mismatch (Backend-DTO erlaubt getrennte Listen) entstehen falsche oder `undefined`-Links; kein Guard, kein Spec-Test für RouterLink-Ziele.

- **Kein Test für Navigation zu Projektdetail**
  - **Verletzt:** Story 8.4 AC (Verhalten spezifiziert; Tests nennen nur Filter Einzelprojekt)
  - **Evidenz:** Spec prüft TextText/„Nexus Analytics Pilot“ und Filter von Einzelprojekt-Insights, aber keine Assertion auf `routerLink`/`/projects/{id}`.

- **Provider-Fehlerpfad nur indirekt abgesichert**
  - **Verletzt:** Story 8.3 AC — bei Gemini 503 / AI-Fehler feste DE-Meldung (nie Provider-Rohtext / „Failed to fetch“)
  - **Evidenz:** Component überschreibt korrekt mit `ERROR_MESSAGE` (außer `disabled`); Diff entfernt den alten `AI_PROVIDER_ERROR`-Test und prüft nur generischen HTTP 500 — Regression auf Diagnose-Code/`body.message` im UI bleibt ungetestet.

- **Konfidenz/Datenqualität nur als EN-Enums**
  - **Abweichung (vorsichtig):** Course Correction verlangt Anzeige der Felder; UX/DE-Konsistenz der Panel-Microcopy nicht spezifiziert, aber Panel zeigt sonst durchgängig DE
  - **Evidenz:** `Konfidenz {{ insight.confidence }} · Datenqualität {{ insight.dataQuality }}` rendert z. B. `HIGH` / `COMPLETE` ohne Labels; Specs verlangen keine Lokalisierung — Residuallücke, kein harter AC-Bruch.