# Validation Report — cgi-kpi-dashboard Architecture Spine

- **Spine:** `_bmad-output/planning-artifacts/architecture/architecture-cgi-kpi-dashboard-2026-07-13/ARCHITECTURE-SPINE.md`
- **Run at:** 2026-07-14T10:15:00+02:00
- **Lint:** 4 findings (all low, false-positive `{id}` in REST paths)

## Overall verdict

## Overall verdict

Der Architecture Spine ist **für den MVP-Pilot build-ready** nach **Amendment 2026-07-14** (Angular 20, RxJS/Signals, CGI EDS). Paradigma, KPI/AI-Trennung, REST-Schnittstelle und FR-Abdeckung sind solide. Validate-Fixes (AD-2 Reader-only, AD-7 unabhängiges Laden, UUID) sind im Spine. **Hinweis:** Version-Review bezog sich auf React/Vite/Recharts — durch Stack-Pivot obsolet; siehe `consistency-check-2026-07-14.md`.

## Reviewer verdicts

| Reviewer | Verdikt |
|---|---|
| lint_spine.py | 4× low (placeholder false positives) |
| Rubric Walker | adequate → strong (Pilot) |
| Version & Reality Check | adequate |
| Adversarial Seam Hunt | 1× high, 2× medium, 1× low |

## Findings by severity

### Critical (0)

*(keine)*

### High (1)

**Adversarial — AI bypasses KPI reader layer** (AD-2, ARCHITECTURE-SPINE.md)
`ai.*` darf DTOs von `kpi.*` lesen, verbietet aber nicht direkten JPA/Domain-Zugriff. Zwei Teams könnten unterschiedliche „freigegebene Daten“ an Gemini senden (FR-14).
**Fix:** AD-2 schärfen: `ai.*` importiert kein `domain.*`, kein Repository — nur `kpi`-Reader-Interfaces.

### Medium (4)

**Rubric — KPI calculation location not enforced** (AD-3)
Aggregation in `application.*` vs. `kpi.*` beide AD-konform wörtlich.
**Fix:** Regel „alle KPI-Zahlen entstehen in `kpi.*`“.

**Adversarial — TanStack Query key collision** (AD-7)
Shared parent query key kann FR-15 unabhängiges Laden untergraben.
**Fix:** AD-7: separate queryKey-Namespaces Fakten vs. AI.

**Versions — Vite 6.x vs current Vite 8.0** (Stack)
Greenfield könnte Vite 8 nutzen; Spine pinnt 6.x.
**Fix:** Vite 8.x pin **or** explizit in Deferred wie Spring Boot 4.x.

**Versions — Spring Boot 3.5 OSS EOL** (Stack / Deferred)
Bekannt und deferred — zur Kenntnis für Stakeholder, kein Pilot-Blocker.

### Low (5)

- ID-Typ UUID vs Long unentschieden (Conventions)
- AD-5 Wording `/api/ai/*` vs `/portfolio/ai/` (Präzisierung)
- Filter-Persistenz FR-7 nicht in Spine (Story-Ebene ok)
- Gemini client/SDK unpinned (Story-Ebene)
- Lint `{id}` path placeholders (ignore)

## Reviewer files

- `reviews/review-rubric.md`
- `reviews/review-versions.md`
- `reviews/review-adversarial.md`

## Recommended next step

**`bmad architecture update`** — high + medium Fixes in Spine einarbeiten (ca. 2 AD-Ergänzungen), dann **`bmad-create-epics-and-stories`**.
