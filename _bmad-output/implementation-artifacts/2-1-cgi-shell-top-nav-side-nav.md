---
story_key: 2-1-cgi-shell-top-nav-side-nav
epic: 2
baseline_commit: NO_VCS
---

# Story 2.1: CGI Shell (Top Nav, Side Nav)

Status: done

## Acceptance Criteria

1. Top Navigation und Side Navigation sichtbar beim App-Start.
2. Side Navigation enthält „Portfolio" und „Projekte".
3. Angular Material Stub mit CGI-Farben (DESIGN.md).

## Senior Developer Review (AI)

**Ergebnis:** Approved — alle ACs erfüllt

| AC | Evidenz |
|---|---|
| AC1 Shell sichtbar | `AppShellComponent` mit `mat-sidenav` + Tests |
| AC2 Nav-Einträge | `SideNavComponent` — Portfolio, Projekte |
| AC3 Material + CGI | Material Toolbar/Sidenav; `--cgi-primary-darkest` Top Nav |

### File List

- frontend/src/styles.scss
- frontend/src/app/core/layout/app-shell.component.ts
- frontend/src/app/core/layout/app-shell.component.html
- frontend/src/app/core/layout/app-shell.component.scss
- frontend/src/app/core/layout/app-shell.component.spec.ts
- frontend/src/app/core/navigation/top-nav.component.ts
- frontend/src/app/core/navigation/top-nav.component.html
- frontend/src/app/core/navigation/top-nav.component.scss
- frontend/src/app/core/navigation/side-nav.component.ts
- frontend/src/app/core/navigation/side-nav.component.html
- frontend/src/app/core/navigation/side-nav.component.scss
- frontend/src/app/core/navigation/side-nav.component.spec.ts
- frontend/package.json
