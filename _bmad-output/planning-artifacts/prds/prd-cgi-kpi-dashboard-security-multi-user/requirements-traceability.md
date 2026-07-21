---
name: requirements-traceability
parent: prd-cgi-kpi-dashboard-security-multi-user
created: 2026-07-21
updated: 2026-07-21
---

# Requirements Traceability — Security Multi-User

## FR → Epic / AD / Stories

| FR | Epic | AD | Stories |
|---|---|---|---|
| FR-22 | 11 | AD-12 | 11.2–11.6 |
| FR-23 | 11, 12 | AD-13 | 11.1, 12.2 |
| FR-24 | 13 | AD-13, AD-17 | 13.1–13.2 |
| FR-25 | 12 | AD-13, AD-14 | 12.1, 12.4 |
| FR-26 | 12, 13 | AD-13 | 12.2, 13.4 |
| FR-27 | 13 | AD-15 | 13.3–13.4 |
| FR-28 | 13 | AD-15, AD-16 | 13.3 |
| FR-29 | 14 | AD-17 | 14.2 |
| FR-30 | 11, 14 | AD-12, AD-18 | 11.6, 14.3 |
| FR-31 | 11, 13 | AD-12 | 11.4, 11.6, 13.1 |
| FR-32 | 13 | AD-15, AD-18 | 13.4 |

## P0 Decision → Artefakt

| Entscheidung | Artefakt |
|---|---|
| Default-Workspace, alle sehen gemeinsame Daten, kein project_membership v1 | PRD §3.1, AD-14, Stories 11.1/12.1 |
| Private Daten workspace_id+user_id aus Context | PRD §3.2, AD-13/14, 12.1 |
| Session-Auth, kein JWT | PRD §3.3, AD-12, 11.3–11.5 |
| Env-Bootstrap-Admin | PRD §3.4, AD-12, 11.2 |
| Flyway Backfill plan | PRD §3.5, AD-14, 11.1 |
| KI nur ADMIN, 3 Endpoints | PRD §3.6, 12.2 |
| Kein KI-Persist; Cache-Key/disable | PRD §3.7, AD-18, 12.3 |
| Master-Key Env; Prod KMS Blocker | PRD §3.8, AD-16, 13.3 |
| Single-instance Sessions | PRD §3.9, AD-12 |
| Rollen + Last-Admin + Session reject | PRD §3.10, 13.1, 11.6 |

## Acceptance Tests → Stories

| Test | Primär |
|---|---|
| Unauth → 401 | 11.3, 11.6 |
| USER Admin/KI → 403 | 12.2, 12.4 |
| ADMIN KI → ok | 12.2, 13.4 |
| Außerhalb Workspace | 12.1, 12.4 |
| Manipulierte IDs | 12.1, 12.4 |
| Private Settings A≠B | 12.1, 12.4 |
| Bootstrap ≤1 / kein Default-PW / Force-Change | 11.2, 11.4, 11.6 |
| Deaktiviert Login/Session | 11.4, 11.6, 13.1 |
| Letzter Admin | 13.1 |
| API-Key Logs/Responses; MK∉PG | 13.3 |
| Cache Isolation | 12.3, 12.4 |
| Parallele Sessions | 11.6, 14.3 |
| CSRF | 11.3, 11.6, 14.1 |
