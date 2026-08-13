# Architecture Overview

## Paradigma

Layered Modular Monolith:

| Schicht | Verantwortung |
|---------|---------------|
| API | REST-Controller, DTOs, Validierung |
| Application / Domain | Use Cases, Domänenmodell |
| Infrastructure | JPA, Flyway, externe Adapter |
| `kpi.*` | Deterministische KPI-Berechnung und Reader |
| `ai.*` | Interpretation auf Basis bereits berechneter KPI-/Projektdaten |

## Frontend

Angular 22 (TypeScript, Angular Material, RxJS), gebaut mit Node.js 22 und npm.
Im Demo-Betrieb liefert nginx die SPA und proxied `/api` sowie `/actuator`
an das Backend. Dadurch teilen sich Frontend und Backend denselben Origin
(Session-Cookie + CSRF).

## Deployables (Demo)

```text
Browser → frontend (nginx :4200)
            ├─ static Angular SPA
            └─ /api, /actuator → backend :8080
                                  └─ PostgreSQL (db)
```

- `docker compose up --build` startet PostgreSQL, Backend und Frontend
- Flyway legt Schema an und lädt Seed-/Demo-Daten
- Backend: Spring Boot 3.5 / Java 21, Maven

## KPI und KI

Kennzahlen entstehen nur im Backend (`kpi.*`). Das Frontend zeigt sie an,
rechnet sie nicht nach. Die KI-Schicht (`ai.*`) liest ausschließlich über
diese Reader — nicht direkt aus der Persistenz.

Ohne API-Key bleibt die KI deaktiviert. Portfolio, Projektliste und KPIs
funktionieren davon unabhängig. Die KI schreibt keine Kennzahlen in die
Datenbank und ersetzt keine fachlichen Werte.

## Auth / Security

- Session-Cookie-Authentifizierung, CSRF-Token (Cookie + Header)
- Rollen: Admin (Benutzerverwaltung, API-Key) und Workspace-Nutzer
- Workspace-Isolation: Zugriff nur auf Projekte des eigenen Workspace
- Bootstrap-Admin über Environment Variables beim ersten Start
- API-Keys und Secrets nur serverseitig; optional verschlüsselt über `APP_AI_MASTER_KEY`

## Daten

- PostgreSQL als System of Record
- Flyway-Migrationen unter `backend/src/main/resources/db/migration/`
- Demo-Daten über Seed-Migrationen (keine DB-Dumps)
