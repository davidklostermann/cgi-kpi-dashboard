# User Stories - Kapazitaetsdaten anbinden (Epic 16)

> Ergaenzt `stories-mvp.md` und `stories-iso-management.md`. **Keine sensiblen Personaldetails.**
> Stack-Ist: Angular 22, Spring Boot 3.5.16, Java 21, PostgreSQL.
> Planungsstand: 2026-07-30

---

## Epic 16 - Kapazitaetsdaten anbinden

**Ziel:** Die bestehende Projekt-Detail-Section **Team & Kapazitaet** mit echten rollenbasierten Kapazitaetsdaten befuellen, statt fuer die relevanten ISO-orientierten Projektbeispiele pauschal den Empty-State zu zeigen.

**Position auf der Detailseite:**

```text
KPIs -> Issues & Massnahmen -> Berichtsstandsvergleich -> Erweiterte Projektsteuerung -> Team & Kapazitaet -> Phasen/Meilensteine
```

**Abhaengigkeiten:** Epic 7.5 (`GET /api/projects/{id}/capacity`, `ProjectCapacityDto`, `app-project-team-capacity-section`), Epic 15 (visuelle Management-Section), Epic 12 (Workspace-Isolation).

**Schnittstellenentscheidung:** Es wird der bestehende Endpoint `GET /api/projects/{id}/capacity` genutzt. Kein neuer Endpoint, weil das vorhandene Modell bereits genau die geforderte Granularitaet liefert: Rollenname, benoetigte FTE, verfuegbare FTE, Besetzungsgrad und Summary ohne Personenbezug.

**Datenschutz-Regel:** Keine Namen, keine individuellen Auslastungen, keine Gehalts- oder Personaldaten. Anzeige und API bleiben auf aggregierter Rollen-/Skill-Ebene.

---

### Story 16.1 - Kapazitaets-Seeds fuer Projektbeispiele

**Als** Projektleiter  
**moechte ich** fuer repraesentative Projektbeispiele gepflegte Rollen-/Skill-Kapazitaetsdaten sehen  
**damit** die Kapazitaets-Section nicht nur fuer ein einzelnes Projekt echte Daten zeigt.

#### Scope

- Neue Flyway-Migration `V14__mock_seed_capacity_epic16.sql`.
- Seed-Erweiterung fuer ISO-relevante Beispielprojekte mit aggregierten Rollen-/Skill-Capacity-Zeilen.
- Summary je befuelltem Projekt: fehlende FTE, naechste Verfuegbarkeit, ueberlastete Rollen, externe Optionen, Impact Headline/Detail.
- Keine Aenderung an `V6`/`V7`; bestehende Daten fuer Projekt `...0001` bleiben unveraendert.

#### Acceptance Criteria

1. **Given** frische Mock-DB, **when** Flyway bis V14 laeuft, **then** mehrere Projekt-Detailseiten liefern unter `/capacity` Rollen und Summary.
2. **Given** Seed-Daten, **then** jede Rolle ist aggregiert und enthaelt keine Personennamen.
3. **Given** ein Projekt ohne Capacity-Seed, **then** `/capacity` liefert weiterhin `roles: []` und `summary: null`.

---

### Story 16.2 - Capacity-Section visuell an Management-Karten angleichen

**Als** Nutzer der Projekt-Detailseite  
**moechte ich** Team & Kapazitaet im gleichen ruhigen Kartenstil wie die erweiterte Projektsteuerung sehen  
**damit** die Detailseite optisch aus einem Guss wirkt.

#### Scope

- Nur `project-team-capacity-section.component.html/.scss`.
- Bestehender Service-Aufruf `ProjectApiService.getProjectCapacity(id)` bleibt unveraendert.
- Loading, Error und Empty bleiben erhalten.
- Rollenliste und Summary nutzen bestehende Tokens aus Epic 15 (`--cgi-*`, Border/Radius/Shadow/Progressbar-Pattern).

#### Acceptance Criteria

1. **Given** Capacity-Daten vorhanden, **then** Rollen, Besetzungsgrad, FTE-Abdeckung und Summary werden sichtbar.
2. **Given** keine Rollen vorhanden, **then** der Empty-State bleibt sichtbar.
3. **Given** API-Fehler, **then** Error-State mit Retry bleibt nutzbar.
4. **Given** gerenderte UI, **then** keine personenbezogenen Details werden angezeigt.

---

### Story 16.3 - Tests und Schnittstellennachweis

**Als** Product Owner  
**moechte ich** die Capacity-Anbindung ueber Backend- und Frontend-Tests abgesichert sehen  
**damit** Empty-State und echte Daten nicht wieder auseinanderlaufen.

#### Scope

- Backend-Integrationstest fuer zusaetzliches Seed-Projekt.
- Component-Test fuer gerenderte Kapazitaetsdaten, Empty-State und Datenschutz-Negativbeispiele.
- Production Build als Smoke.

#### Acceptance Criteria

1. **Given** Projekt mit Epic-16-Seed, **when** `/api/projects/{id}/capacity` geladen wird, **then** Response enthaelt Rollen und Summary.
2. **Given** Component-Testdaten mit Rollen, **then** UI zeigt keine Namen oder sensiblen Felder.
3. **Given** Projekt ohne Rollen, **then** Empty-State bleibt korrekt.

---

## Epic-16 Definition of Done

- [x] Mehrere Projekte haben rollenbasierte Capacity-Seeds.
- [x] UI nutzt vorhandene Capacity-API und zeigt echte Daten bei vorhandenen Rollen.
- [x] Empty-/Error-/Loading-States bleiben intakt.
- [x] Keine sensiblen Personaldetails in API, Seed oder UI.
- [x] Frontend-Tests und Build laufen ohne Fehler.
