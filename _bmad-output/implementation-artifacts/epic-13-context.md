# Epic 13 — Administration und API-Key (Kontext)

Dieses Dokument destilliert die Anforderungen, architektonischen Invarianten und User Stories für Epic 13. Es dient als zentrale Referenz für die Implementierung der Benutzerverwaltung und der verschlüsselten KI-Konfiguration.

**Status:** Developer-Ready  
**Bezieht sich auf:** Stories 13.1–13.4 · FR-24, FR-27, FR-28, FR-32 · NFR-15, NFR-17

---

## 1. Zielsetzung & Nutzen

*   **Ziel:** Aufbau einer administrativen Ebene zur Verwaltung von Benutzern und zur sicheren Konfiguration des KI-Providers (Gemini).
*   **Nutzen:** Operative Verwaltung von Zugängen und API-Keys ohne Quellcode-Änderungen oder Klartext-Leaks in der Datenbank.
*   **Kern-Invariante:** Sensible Daten (API-Keys) werden verschlüsselt in der Datenbank gespeichert und niemals im Klartext an Clients oder Logs übertragen.

---

## 2. Funktionale Anforderungen (FR)

*   **FR-24 (User-Mgmt):** ADMINs können Benutzer anlegen, (de-)aktivieren und Rollen (USER/ADMIN) vergeben.
*   **FR-27 (KI-Provider-Config):** Zentrale Verwaltung der Gemini-Konfiguration (API-Key, Modell, Status) durch ADMINs.
*   **FR-28 (Verschlüsselung):** API-Keys werden mittels AES-GCM verschlüsselt. Der Master-Key liegt nur in der Umgebungsvariable (Env).
*   **FR-32 (Wartbarkeit):** Connection-Tests für den KI-Provider, Key-Rotation und automatische Cache-Invalidierung bei Konfigurationsänderung.

---

## 3. Architektur & Sicherheit (AD-15/16)

### 3.1 Kryptographie-Konzept
*   **Verschlüsselung:** AES-GCM (Authenticated Encryption).
*   **Master-Key (MK):** Muss via Env `AI_MASTER_KEY` bereitgestellt werden.
*   **Speicherung:** In der DB (`ai_provider_config`) landet nur der Ciphertext. Der MK darf niemals in die Datenbank oder das Git-Repository gelangen.
*   **Fallback:** Ohne gültigen MK ist keine Entschlüsselung möglich -> KI-Funktionen bleiben deaktiviert.

### 3.2 Zugriffsschutz
*   **Rollen:** Alle Endpunkte in Epic 13 erfordern die Rolle `ADMIN`.
*   **Last-Admin-Guard:** Der letzte aktive Administrator kann sich nicht selbst deaktivieren oder seine Rolle entziehen.
*   **API-Maskierung:** API-Keys werden in Responses nur maskiert (z.B. Suffix-Anzeige) zurückgegeben.

---

## 4. User Stories & Akzeptanzkriterien (AC)

### 13.1 & 13.2 — Benutzerverwaltung (API & UI)
*   **AC:** CRUD für `AppUser`; Status-Änderungen (Aktiv/Inaktiv) werfen bestehende Sessions des betroffenen Nutzers beim nächsten Request ab.
*   **AC:** Admin-Passwort-Reset (initial ohne E-Mail, direktes Setzen durch ADMIN).
*   **AC:** Audit-Events für alle administrativen Änderungen.

### 13.3 — Crypto Service & Config-Modell
*   **AC:** `EncryptionService` implementiert AES-GCM; Integrationstest stellt sicher, dass MK nicht in PG landet.
*   **AC:** Maskierung von Keys in allen DTOs.

### 13.4 — Admin KI-Konfiguration & Connection-Test
*   **AC:** `POST /api/admin/ai/test-connection` prüft den Key gegen den Provider ohne den Key im Echo/Log zu zeigen.
*   **AC:** Bei Änderung der Konfiguration (`config_version++`) wird der `ProjectAiAnalysisCache` für alle Projekte invalidiert.

---

## 5. Abhängigkeiten & Reihenfolge

1.  **Voraussetzung:** Epic 11 (Auth-Basis) muss vollständig sein.
2.  **Voraussetzung:** Epic 12.2 (Policies) sollte implementiert sein, damit KI-Endpoints bereits unter ADMIN-Schutz stehen.
3.  **Implementierungsfolge:** 
    *   13.1 (User-API) -> 13.2 (User-UI)
    *   13.3 (Crypto) -> 13.4 (Admin AI Config)

---

## 6. Bekannte Risiken

*   **Master-Key Verlust:** Ohne `AI_MASTER_KEY` in der Prod-Umgebung sind alle gespeicherten Keys wertlos.
*   **Key-Logging:** Gefahr des unbeabsichtigten Loggens von Keys während des Connection-Tests (strenges Masking erforderlich).
*   **Prod-KMS:** Der Übergang zu einem echten KMS (Key Management Service) ist für v1 noch offen (Blocker für echten Prod-Betrieb).
