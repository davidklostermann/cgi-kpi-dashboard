# Architecture Spine Review — Adversarial Seam Hunt

> **Amendment 2026-07-14:** Validate-Fixes (Reader-only, AD-3 KPI in kpi.*) im Spine umgesetzt. TanStack-Finding obsolet — AD-7 nutzt RxJS + Signals.

## Overall verdict

Zwei unabhängige Implementierungen könnten trotz Einhaltung aller ADs **wörtlich** an **3 Stellen** inkompatibel werden. Keine davon bricht FR-10 offensichtlich, aber KPI/AI-Trennung (AD-2/AD-3) ist **nicht vollständig dicht**. Empfehlung: 2 AD-Ergänzungen vor Story-Dev.

## Constructed divergence pairs

### Pair 1 — KPI-Berechnung außerhalb `kpi.*`

- **Unit A:** Portfolio-Aggregation in `application.PortfolioService` (Summen, Durchschnitte inline)
- **Unit B:** Alles in `kpi.PortfolioKpiCalculator`
- **Beide erfüllen:** AD-3 („KPI-Endpunkte liefern backend-Werte“), AD-1
- **Divergenz:** Unterschiedliche Aggregationslogik, Tests an verschiedenen Orten, Filter-Konsistenz bricht (FR-8)
- **Severity:** **medium**
- **Fix:** AD-3 oder neues AD: „Jede KPI-Zahl entsteht in `kpi.*`; Application orchestriert nur.“

### Pair 2 — AI liest Domain statt KPI-DTOs

- **Unit A:** `ai.GeminiSummaryService` injiziert `ProjectRepository`, baut Prompt aus Entities
- **Unit B:** `ai.*` nutzt nur `kpi.ProjectKpiReader.toApprovedDto(projectId)`
- **Beide erfüllen:** AD-4 (read-only), AD-2 (ai importiert kpi — optional)
- **Divergenz:** Unterschiedliche „freigegebene Daten“ für Gemini (FR-14), Leaks nicht-freigegebener Felder
- **Severity:** **high** (Daten-Governance-Pilot)
- **Fix:** AD-2 Rule: „`ai.*` hat keinen Zugriff auf `domain.*`/Repositories; nur `kpi`-Reader-Interfaces.“

### Pair 3 — Frontend shared query key für Portfolio

- **Unit A:** Eine TanStack Query `['portfolio']` für KPIs + Trendanalyse
- **Unit B:** Getrennte Keys `['portfolio','facts']` und `['portfolio','ai','trend']`
- **Beide erfüllen:** AD-7 wenn Fehler-UI getrennt — **Unit A verletzt Geist** von AD-7 bei refetch/invalidate
- **Divergenz:** KI-Retry invalidiert KPI-Cache oder umgekehrt; FR-15 UX bricht
- **Severity:** **medium**
- **Fix:** AD-7 Rule: „Separate queryKey-Namespaces für Fakten vs. AI; kein shared parent key mit gemeinsamem error boundary.“

### Pair 4 — ID-Typ mismatch

- **Unit A:** UUID in API + DB
- **Unit B:** Long in DB, UUID in API (Mapping)
- **Spine:** Conventions erlauben beides
- **Divergenz:** URL `/projects/{id}` inkonsistent in Integrationstests
- **Severity:** **low**

## Summary

| Severity | Count |
|---|---|
| critical | 0 |
| high | 1 |
| medium | 2 |
| low | 1 |
