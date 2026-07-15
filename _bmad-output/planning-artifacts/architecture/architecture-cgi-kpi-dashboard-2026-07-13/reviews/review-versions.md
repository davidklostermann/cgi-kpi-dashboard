# Architecture Spine Review — Version & Reality Check

> **Amendment 2026-07-14:** Obsolet für Frontend-Stack — Pivot auf Angular 20 + CGI EDS. Siehe `ARCHITECTURE-SPINE.md` und `consistency-check-2026-07-14.md`. Backend-Versionen (Spring Boot 3.5.16) weiterhin gültig.

## Overall verdict

Kern-Stack aus PRD-Addendum ist **plausibel und weitgehend verifiziert**. **Spring Boot 3.5.16** ist korrekt als letzter 3.5.x-Patch, aber **OSS-EOL (Jun 2026)** — im Spine unter Deferred, akzeptabel für kurzen Pilot. **Vite 6.x** ist stabil aber nicht mehr „current“ (Vite **8.0** seit Mär 2026). React **19.x** passt. Keine critical findings.

## Verified

| Technology | Spine | Reality (Jul 2026) | Status |
|---|---|---|---|
| Java 21 | 21 | LTS, Spring Boot 3.5/4.x compatible | ✓ |
| Spring Boot | 3.5.16 | Final 3.5.x OSS release (25 Jun 2026); OSS support ended 30 Jun 2026 | ✓ pinned, ⚠ EOL |
| PostgreSQL | 16.x | Supported, current line | ✓ |
| React | 19.x | 19.2.x stable (Jun 2026) | ✓ |
| TanStack Query | 5.x | v5 current major | ✓ |
| Recharts | 2.x | v2 current | ✓ |
| Maven | 3.9.x | Current | ✓ |

## Findings

- **medium** **Vite 6.x** im Spine — Vite **8.0** stable since Mar 2026 (Rolldown bundler). Vite 6 still works but is one major behind. *Fix:* Pin `Vite 8.x` for greenfield scaffold **or** document „Vite 6.x [ADOPTED]“ with upgrade in Deferred (like Spring Boot 4.x).
- **medium** **Spring Boot 3.5 OSS EOL** — fine for internal pilot ending 2026; production needs 4.x plan (already in Deferred). *Fix:* none for pilot; ensure Deferred visible to stakeholders.
- **low** **Spring Data JPA / Flyway / Actuator** „via Boot BOM“ — not independently pinned; acceptable at pilot altitude.
- **low** **Gemini API** — „SDK or HTTP client“ unpinned; Google SDK versions change frequently. *Fix:* Defer to story (e.g. `google-genai` Java client version at implementation).
- **low** **TypeScript 5.x** — TS 6.x emerging (Jun 2026 articles); 5.x still safe for pilot.
