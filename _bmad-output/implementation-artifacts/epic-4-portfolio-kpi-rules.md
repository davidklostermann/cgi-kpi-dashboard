# Epic 4 — Portfolio-KPI-Regeln (kanonisch)

**Gültigkeit:** Stories 4.1–4.4 · API `GET /api/portfolio/kpis` · Berechnung in `kpi.service.PortfolioKpiCalculator` (AD-3)

**Referenz-Implementierung:** `backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiCalculator.java`

---

## 1. Datenfluss

1. `JpaPortfolioKpiReader` lädt Projekte, Budgets und Risiken aus der Datenbank.
2. `PortfolioProjectFilter` wendet FR-8-Filter an (vor der Aggregation).
3. `PortfolioKpiCalculator` berechnet alle Kennzahlen deterministisch.
4. `PortfolioController` liefert `PortfolioKpiSummaryDto` als JSON.

**Invariante:** Alle KPI-Zahlen in der API-Response stammen ausschließlich aus `PortfolioKpiCalculator` — keine Frontend-Berechnung, keine hart codierten Produktionswerte.

---

## 2. Filter (Story 4.4)

| Query-Parameter | Wirkung |
|---|---|
| `customer` | Teilstring-Match (case-insensitive) auf Kundenname |
| `projectLead` | Teilstring-Match (case-insensitive) auf Projektleitung |
| `status` | Ampelstatus (`ON_TRACK`, `AT_RISK`, `CRITICAL`, `COMPLETED`); mehrfach kombinierbar |
| `phase` | Exakter Match auf aktuelle Phase |
| `lifecycle` | `active` (Default), `completed`, `all` |
| `reportMonth` | Format `YYYY-MM`, Match auf `dataReportMonth` |
| `riskSeverity` | Projekte mit mindestens einem **offenen** Risiko der Severity |

### Lifecycle-Default

Ohne `lifecycle`-Parameter gilt **`active`**: Projekte mit `status = COMPLETED` werden ausgeschlossen.

| `lifecycle` | Enthaltene Projekte |
|---|---|
| `active` | `status != COMPLETED` |
| `completed` | `status = COMPLETED` |
| `all` | alle Status |

Filter sind **UND-verknüpft** (kombinierbar).

---

## 3. Kennzahlen und Formeln

Alle Formeln beziehen sich auf die **nach Filter verbleibende Projektliste** (`filteredProjects`).

### 3.1 Aktive Projekte (`activeProjectCount`)

```
activeProjectCount = Anzahl Projekte mit status ∈ {ON_TRACK, AT_RISK, CRITICAL}
```

Nicht in `activeProjectCount`: `COMPLETED`.

**UI-Label (Frontend):** abhängig vom Lifecycle-Filter:
- Default / `active` → „Aktive Projekte“ → `activeProjectCount`
- `completed` → „Abgeschlossene Projekte“ → `statusDistribution.completed`
- `all` → „Projekte“ → `activeProjectCount + statusDistribution.completed`

### 3.2 Ø-Fortschritt (`averageProgressPercent`)

```
averageProgressPercent = Σ progressPercent / |filteredProjects|
```

Eine Dezimalstelle, HALF_UP.

### 3.3 Budgetabweichung (`budgetDeviationPercent`)

Nur Projekte mit `plannedBudget > 0` und vorhandenem `actualBudget`:

```
projektAbweichung = (actualBudget − plannedBudget) / plannedBudget × 100
budgetDeviationPercent = Ø projektAbweichung
```

Ohne gültige Budgetdaten: `0.0`.

### 3.4 Termintreue (`scheduleCompliancePercent`)

```
imPlan = scheduleDeviationDays IS NULL OR scheduleDeviationDays ≤ 0
scheduleCompliancePercent = |imPlan| / |filteredProjects| × 100
```

### 3.5 Kritische Risiken (`criticalRiskCount`)

**MVP-Annahme** (PRD FR-1 Aggregationslogik `[OFFEN]`):

```
criticalRiskCount = Anzahl offener Risiken (status = OPEN)
                    mit severity ∈ {HIGH, CRITICAL}
                    für Projekte in filteredProjects
```

Geschlossene Risiken (`CLOSED`) zählen nicht. Im Mock-Seed existieren nur `HIGH`-Severity-Risiken, keine `CRITICAL`.

### 3.6 Statusverteilung (`statusDistribution`)

| Feld | Zählt Projekte mit Status |
|---|---|
| `onTrack` | `ON_TRACK` |
| `atRisk` | `AT_RISK` |
| `critical` | `CRITICAL` |
| `completed` | `COMPLETED` |

**Invariante (immer gültig):**

```
onTrack + atRisk + critical + completed = |filteredProjects|
activeProjectCount = onTrack + atRisk + critical
```

### 3.7 Leerzustand (`empty: true`)

Wenn `filteredProjects` leer ist:

```json
{
  "activeProjectCount": 0,
  "averageProgressPercent": 0.0,
  "budgetDeviationPercent": 0.0,
  "scheduleCompliancePercent": 0.0,
  "criticalRiskCount": 0,
  "statusDistribution": { "onTrack": 0, "atRisk": 0, "critical": 0, "completed": 0 },
  "empty": true
}
```

---

## 4. Mock-Seed Referenzwerte (Default-Filter)

`lifecycle=active` (implizit), ungefiltert:

| Kennzahl | Wert |
|---|---|
| `activeProjectCount` | 19 |
| `statusDistribution` | Grün 9 · Gelb 6 · Rot 4 · Abgeschlossen 0 |
| `criticalRiskCount` | 4 |

`lifecycle=all`:

| Kennzahl | Wert |
|---|---|
| `activeProjectCount` | 19 |
| `statusDistribution` | Grün 9 · Gelb 6 · Rot 4 · Abgeschlossen 1 |
| Gesamtprojekte | 20 |

---

## 5. Frontend-Darstellung (Story 4.3)

- **Ampelverteilung:** `Grün: N · Gelb: N · Rot: N` (+ `· Abgeschlossen: N` wenn > 0)
- **Ladezustand:** nur KPI-Bereich
- **Fehlerzustand:** nur KPI-Bereich mit Retry (AD-7)
- **Gefiltert leer:** Hinweis + „Filter zurücksetzen“

---

## 6. Bewusst außerhalb Epic 4

| Thema | Epic |
|---|---|
| Portfolio-Tabelle, Projektklick | 5 |
| Gantt, Charts | 5 |
| Problem-KPI Portfolio-Ebene | 5/6 (Change Request) |
| Projektsuche nach Name | Follow-up |
| Filter-Sync mit Tabelle/Gantt/Charts | 5 |

---

## 7. Änderungshistorie

| Datum | Änderung |
|---|---|
| 2026-07-15 | Kanonisches Regelwerk erstellt (Epic-4-Review Follow-up) |
| 2026-07-15 | `statusDistribution.completed` ergänzt; `activeProjectCount`-Semantik präzisiert |
