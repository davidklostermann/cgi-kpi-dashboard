---
story_key: 2-4-layout-raster-haupt-vs-ki-spalte
epic: 2
baseline_commit: NO_VCS
---

# Story 2.4: Layout-Raster Haupt vs. KI-Spalte

Status: done

## Acceptance Criteria

1. Desktop ≥1200px: Hauptbereich 9/12, KI-Spalte 3/12 rechts.
2. Viewport <1024px: KI-Bereich unter Hauptinhalt mit KI-Badge.
3. KI-Spalte mit `ki-surface` und Disclaimer.

## Senior Developer Review (AI)

**Ergebnis:** Approved — alle ACs erfüllt

| AC | Evidenz |
|---|---|
| AC1 Desktop-Raster | CSS Grid `9fr 3fr` ab 1200px |
| AC2 Mobile Stack | Single column + `order` unter 1024px |
| AC3 KI-Styling | `--cgi-ki-surface`, Disclaimer-Text, KI-Badge |

### File List

- frontend/src/app/core/layout/facts-ai-layout.component.ts
- frontend/src/app/core/layout/facts-ai-layout.component.html
- frontend/src/app/core/layout/facts-ai-layout.component.scss
- frontend/src/app/core/layout/facts-ai-layout.component.spec.ts
- frontend/src/app/features/ai/ai-panel-placeholder.component.ts
- frontend/src/app/features/portfolio/portfolio-page.component.html
- frontend/src/app/features/project/project-list-page.component.html
- frontend/src/app/features/project/project-detail-page.component.html
