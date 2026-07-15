# Konsistenzcheck — cgi-kpi-dashboard Planungsartefakte

**Datum:** 2026-07-14  
**Anlass:** Stack-Pivot React → Angular 20 + CGI EDS 19.0.0; Epics/Stories erstellt

## Geprüfte Dokumente

| Dokument | Status | Konsistent |
|---|---|---|
| brief.md | aktualisiert | ✓ Angular-Frontend |
| prd.md | unverändert (FR-1..19 stack-neutral) | ✓ |
| prd addendum.md | aktualisiert | ✓ |
| ARCHITECTURE-SPINE.md | aktualisiert | ✓ |
| DESIGN.md | CGI EDS Pivot | ✓ |
| EXPERIENCE.md | Angular + CGI Shell | ✓ |
| epics.md | neu/aktualisiert | ✓ |
| stories/stories-mvp.md | neu | ✓ |

## Abgleich Product Brief ↔ PRD ↔ UX ↔ Architektur ↔ Epics

| Thema | Brief | PRD | UX | Arch | Epics |
|---|---|---|---|---|---|
| Portfolio + Projekt-Detail | ✓ | FR-1..7 | IA | Capability Map | Epic 4–6 |
| Backend-KPIs | ✓ | FR-9 | Fakten links | AD-3 | Epic 3–4 |
| KI getrennt | ✓ | FR-10 | ki-panel | AD-5,7 | Epic 8–9 |
| Mock ~20 Projekte | ✓ | FR-19 | — | Flyway | Story 3.3 |
| Kein Portfolio-Q&A | ✓ | FR-18 | — | AD-5 | Story 9.3 |
| Gemini serverseitig | ✓ | FR-9,13 | — | AD-4,8 | Epic 8–9 |
| CGI Design | neu Brief-Instruktion | — | DESIGN.md | AD-11 | Epic 2 |
| Angular Frontend | neu | addendum | EXPERIENCE | Stack | Epic 1–2 |
| Kein Auth MVP | ✓ | FR-19 | — | AD-6 | Deferred |
| WCAG | — | — | 2.1 AA | — | Epic 10 |

## Entfernte / ersetzte Technologien

| Entfernt | Ersetzt durch |
|---|---|
| React 19 | Angular 20 |
| Vite 8 | Angular CLI (`ng serve`, `:4200`) |
| Recharts | Beschriftete Charts; Gantt HTML/CSS/SVG; Chart-Lib Deferred |
| TanStack Query 5 | RxJS + HttpClient + Angular Signals |
| „Clear Executive" Tokens | CGI Color System (#200A58, #5236AB, …) |
| Donut-Charts | Zahlenzeile / Balken mit Labels |
| `VITE_API_BASE_URL` | `environment.apiBaseUrl` |

## Verbleibende bewusste Inkonsistenzen

| Item | Begründung |
|---|---|
| HTML-Mockups (portfolio-und-projekt-detail.html) | Historische Referenz; Spines maßgeblich |
| architecture validation-report (2026-07-13) | Bezieht sich auf React-Stack; Amendment in Spine |
| ux validation-report High-Finding muted | In DESIGN.md behoben (#333333 caption) |
| PRD FR-3 „Diagramme" | Umgesetzt als trend-chart ohne Donut — fachlich erfüllt |

## Ergebnis

**Planungsartefakte sind konsistent** für Implementierungsphase mit Angular 20 + CGI EDS + Spring Boot. Keine Blocker. Nächster Schritt: **`bmad-dev-story`** ab Story 1.1.
