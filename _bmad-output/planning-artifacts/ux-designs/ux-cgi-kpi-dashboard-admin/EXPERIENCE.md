---
name: ux-cgi-kpi-dashboard-admin
type: ux-experience
status: draft
created: 2026-07-21
extends: ../ux-cgi-kpi-dashboard-2026-07-13/EXPERIENCE.md
---

# EXPERIENCE — Auth & Admin (Post-MVP)

## Principles

- Frontend blendet aus und führt — **Backend autorisiert**.
- Formelles „Sie“ (NFR-9 MVP) beibehalten.
- KI-Kennzeichnung bleibt; zusätzlich Rollenhinweise.
- CGI Shell / EDS-Stil wo verfügbar; sonst Material konsistent zum MVP.
- **v1 DECIDED:** lokaler Login; alle aktiven Workspace-Mitglieder sehen dieselben Portfolio-/Projektdaten; KI nur ADMIN; kein persistenter KI-Chat; Force-Change nach Bootstrap-Admin.

## Surfaces

| Oberfläche | Primärer Nutzer | Zweck |
|---|---|---|
| Login | alle | Anmelden |
| Zugriff verweigert (403) | USER auf Admin/KI | Klärung + Navigation zurück |
| Shell User-Menü | alle | Name, Rolle, Logout |
| Rollenabhängige Nav | alle | Admin-Einträge nur ADMIN |
| Admin-Dashboard | ADMIN | Einstieg User-Mgmt, KI-Config, Audit |
| Benutzerübersicht | ADMIN | Liste, Status, Rolle |
| Benutzer anlegen / Rolle / Aktivierung | ADMIN | FR-24 |
| Passwort zurücksetzen (Admin) | ADMIN | ohne E-Mail in v1 |
| KI-Konfiguration | ADMIN | Provider, Modell, Key (maskiert), Enable |
| Connection-Test | ADMIN | Erfolg/Fehler ohne Key-Echo |
| Audit-Übersicht | ADMIN | sicherheitsrelevante Events |
| KI-Panel Zustände | USER/ADMIN | „nur Admin“ / „nicht konfiguriert“ |

## Journeys

### UJ-S1 Login

Entry: unauth → Login. Success → letzte Route oder Portfolio. Fail → Fehlermeldung ohne User-Enumeration-Details. Lockout → verständlicher Hinweis.  
Bootstrap-Admin mit `must_change_password`: nach Login zwingend Passwort-ändern, bevor normale Nutzung.

### UJ-S2 USER versucht KI

KI-Bereich zeigt Hinweis „KI nur für Administratoren verfügbar“; API 403; kein Retry als Scheinlösung außer Re-Login als Admin.

### UJ-S3 ADMIN Key hinterlegen

Maskierte Anzeige; Paste neuer Key ersetzt; nie vollständiger Key sichtbar nach Speichern; Connection-Test optional vor Enable.

### UJ-S4 Letzter Admin

UI verhindert Disable/Role-Downgrade; Backend lehnt ab (Doppelabsicherung).

## State Patterns

| State | Verhalten |
|---|---|
| Unauth API | Redirect Login (SPA) / 401 |
| 403 | Access-denied Page oder Inline |
| KI disabled/unconfigured | Fakten nutzbar; KI-Panel Hinweis (FR-15 bleibt) |
| Session expired | Re-Login; Formularzustand nicht still fortschreiben |

## Nicht Security

Ausblenden von Admin-Nav ist **kein** Schutz — nur UX.
