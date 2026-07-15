# Architecture Spine Review — Rubric Walker

**Spine:** `ARCHITECTURE-SPINE.md` · **Projekt:** cgi-kpi-dashboard · **Altitud:** feature

## Overall verdict

Der Spine ist **downstream-tauglich** für den MVP-Pilot: Paradigma, KPI/AI-Grenze, REST-Oberfläche und Capability-Map decken FR-1..FR-19 ab. Keine kritischen Lücken. Schwächen: einige ADs sind **durchsetzbar aber nicht wasserdicht** (KPI-Berechnung nur in `kpi.*`, AI ohne Domain-Bypass), ID-Konvention mehrdeutig, und operational details (Health/Timeouts) nur teilweise in Conventions.

**Verdikt:** adequate → strong (Pilot-Stakes)

## Checklist

| Kriterium | Verdikt | Anmerkung |
|---|---|---|
| Divergenzpunkte für Epic/Story-Ebene abgedeckt | **strong** | KPI/AI, REST, Frontend-Loading, Secrets, Deploy |
| AD Rules enforceable | **adequate** | AD-2/AD-3 Lücken (s.u.) |
| Deferred lässt keine stille Divergenz | **adequate** | KPI-Formeln deferred — Risiko kpi vs ai DTO-Shape |
| Tech verified-current | **adequate** | Spring Boot 3.5.16 EOL dokumentiert; Vite 6 vs 8 (siehe Version-Review) |
| Spec-Coverage | **strong** | Alle FRs in Capability Map |
| Operational envelope | **adequate** | Local runtime (AD-9), Actuator im Stack; keine Timeout-AD |
| Shape / Template | **strong** | Alle Pflichtsektionen, valide Mermaid |

## Findings

- **medium** AD-2 erzwingt DTO-Übergabe, verbietet aber nicht direkten `ai.*` → `domain.*`/JPA-Zugriff. Zwei Teams könnten Gemini mit Roh-Entities statt KPI-DTOs füttern. *Fix:* Rule ergänzen: „`ai.*` liest ausschließlich über `kpi`-Reader-Interfaces; kein Import von `domain`-Entities in `ai.*`.“
- **medium** AD-3 sagt „KPI-Endpunkte backend-berechnet“, definiert aber nicht, dass **alle** Berechnung in `kpi.*` leben muss (Controller/Application könnten aggregieren). *Fix:* AD-3 Rule schärfen oder AD-10: „Keine KPI-Aggregation außerhalb `kpi.*`.“
- **low** ID-Konvention: „UUID oder Long — einheitlich, nicht mischen“ ohne Festlegung. *Fix:* Ein Typ wählen (z. B. UUID) im Seed oder Deferred mit Entscheidungsdatum.
- **low** AD-5 Formulierung „Kein `/api/ai/*`“ widerspricht `/api/portfolio/ai/…` semantisch — gemeint ist Portfolio-Q&A, nicht nested AI paths. *Fix:* Präzisieren: „Kein portfolio-weites Q&A; `/portfolio/ai/trend-analysis` erlaubt.“
- **low** Filter-Persistenz (PRD FR-7 Assumption) nicht in Spine — Frontend-only, ok für Stories, optional in Conventions.
- **low** Lint: `{id}` in Pfad-Templates als Placeholder gemeldet — false positive.

## Mechanical notes

- 9 ADs, alle mit Binds/Prevents/Rule ✓
- Keine duplizierten AD-IDs ✓
- Stack-Versionen größtenteils gepinnt; „via Boot BOM“ akzeptabel für Pilot
