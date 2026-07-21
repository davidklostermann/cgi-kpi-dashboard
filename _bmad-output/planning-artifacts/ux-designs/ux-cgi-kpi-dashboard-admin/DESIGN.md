---
name: ux-cgi-kpi-dashboard-admin-design
type: ux-design
status: draft
created: 2026-07-21
extends: ../ux-cgi-kpi-dashboard-2026-07-13/DESIGN.md
---

# DESIGN — Auth & Admin

## Layout

- **Login:** eigenständige Seite außerhalb der App-Shell (kein Side-Nav). Marke/Produktname sichtbar; ein Formular (Benutzer, Passwort, Anmelden); ein Fehlerbereich.
- **Access Denied:** in Shell oder minimal; CTA „Zur Portfolio-Übersicht“.
- **Admin:** unter Shell; Side-Nav-Eintrag „Administration“ nur für ADMIN; Unterseiten: Dashboard, Benutzer, KI-Konfiguration, Audit.

## Components (Reuse)

| Bedarf | Ansatz |
|---|---|
| Forms | Angular Material / EDS Inputs |
| Tabellen Benutzer | bestehendes Tabellen-Muster Portfolio |
| Status Badges | `status-badge` (aktiv/deaktiviert) |
| Key-Feld | Password-Input für Eingabe; nach Save nur `••••` + Suffix |
| Alerts | bestehende Error-/Info-Patterns |

## Copy (DE)

- „KI nur für Administratoren verfügbar.“
- „KI ist nicht konfiguriert oder deaktiviert. Bitte wenden Sie sich an eine Administratorin oder einen Administrator.“
- „Der API-Schlüssel wird verschlüsselt gespeichert und kann nicht erneut vollständig angezeigt werden.“
- „Der letzte aktive Administrator kann nicht deaktiviert werden.“
- „Bitte ändern Sie Ihr Initialpasswort, bevor Sie fortfahren.“
- Filter/Ansichten: persönlich (nur Sie) — Portfolio-Daten: gemeinsam im Workspace.

## WCAG

NFR-6 (WCAG 2.1 AA) gilt weiter: Fokusreihenfolge Login, Labels, `aria-live` für Fehler, Kontrast Captions.

## Visual

Bestehende CGI-Farben/Caption aus DESIGN.md MVP — keine neue Theme-Richtung.
