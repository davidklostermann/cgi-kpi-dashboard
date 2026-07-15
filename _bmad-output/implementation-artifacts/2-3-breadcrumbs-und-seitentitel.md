---
story_key: 2-3-breadcrumbs-und-seitentitel
epic: 2
baseline_commit: NO_VCS
---

# Story 2.3: Breadcrumbs und Seitentitel

Status: done

## Acceptance Criteria

1. Projekt-Detail zeigt Breadcrumb „Portfolio > [Projektname]".
2. Klick auf „Portfolio" navigiert zu `/portfolio`.

## Senior Developer Review (AI)

**Ergebnis:** Approved — alle ACs erfüllt

| AC | Evidenz |
|---|---|
| AC1 Breadcrumb-Trail | `BreadcrumbsComponent` + Detail-Page Test |
| AC2 Navigation zurück | Router-Link-Test in `breadcrumbs.component.spec.ts` |

### File List

- frontend/src/app/core/navigation/breadcrumb.model.ts
- frontend/src/app/core/navigation/breadcrumbs.component.ts
- frontend/src/app/core/navigation/breadcrumbs.component.html
- frontend/src/app/core/navigation/breadcrumbs.component.scss
- frontend/src/app/core/navigation/breadcrumbs.component.spec.ts
- frontend/src/app/features/project/project-detail-page.component.ts
