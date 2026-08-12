# Architecture Overview

## Paradigma

Layered Modular Monolith:

| Schicht | Verantwortung |
|---------|---------------|
| API | REST-Controller, DTOs, Validierung |
| Application / Domain | Use Cases, Domänenmodell |
| Infrastructure | JPA, Flyway, externe Adapter |
| `kpi.*` | Deterministische KPI-Berechnung und Reader |
| `ai.*` | KI-Use-Cases auf Basis freigegebener KPI-/Projektdaten |

## Deployables (Demo)

```text
Browser → frontend (nginx :4200)
            ├─ static Angular SPA
            └─ /api, /actuator → backend :8080
                                  └─ PostgreSQL (db)
```

- `docker compose up --build` startet PostgreSQL, Backend und Frontend
- Flyway legt Schema an und lädt Seed-/Demo-Daten
- Frontend und Backend teilen sich denselben Origin über nginx (Session + CSRF)

## Wichtige Invarianten

1. **Ein Backend-Deployable** — kein separater AI-Microservice
2. **KPI/AI-Grenze** — `ai.*` liest nur über `kpi.*`-Reader, nie direkt aus Persistence
3. **KPIs sind Backend-Fakten** — Frontend rendert, rechnet nicht nach
4. **Gemini ist Interpretation** — keine DB-Writes und keine erfundenen KPI-Werte
5. **Secrets serverseitig** — keine API-Keys im Frontend

## Auth / Security (Kurz)

- Session-Cookie-Authentifizierung
- CSRF-Token (Cookie + Header)
- Bootstrap-Admin über Environment Variables beim ersten Start
- AI-Keys verschlüsselbar über `APP_AI_MASTER_KEY`

## Daten

- PostgreSQL als System of Record
- Flyway-Migrationen unter `backend/src/main/resources/db/migration/`
- Demo-Daten über Seed-Migrationen (keine DB-Dumps)
