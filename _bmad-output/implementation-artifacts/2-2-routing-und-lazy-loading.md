---
story_key: 2-2-routing-und-lazy-loading
epic: 2
baseline_commit: NO_VCS
---

# Story 2.2: Routing und Lazy Loading

Status: done

## Acceptance Criteria

1. Side Nav „Portfolio" → `/portfolio` (lazy).
2. Side Nav „Projekte" → `/projects` (lazy).
3. Route `/projects/:id` lädt Projekt-Detail lazy.

## Senior Developer Review (AI)

**Ergebnis:** Approved — alle ACs erfüllt

| AC | Evidenz |
|---|---|
| AC1 Portfolio lazy | Shell-Child-Route + `loadChildren` |
| AC2 Projekte lazy | Side-Nav href + lazy chunk |
| AC3 Detail lazy | `project-detail-page.component` Route `:id` |

### File List

- frontend/src/app/app.routes.ts
- frontend/src/app/app.routes.spec.ts
- frontend/src/app/app.config.ts
- frontend/src/app/features/project/project.routes.ts
- frontend/src/app/features/project/project-detail-page.component.ts
- frontend/src/app/features/project/project-detail-page.component.html
- frontend/src/app/features/project/project-detail-page.component.scss
- frontend/src/app/features/project/project-detail-page.component.spec.ts
- frontend/src/app/features/project/project-list-page.component.html
