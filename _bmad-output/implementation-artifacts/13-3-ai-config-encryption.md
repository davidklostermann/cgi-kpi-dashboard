# Story 13.3: KI-Config-Verschlüsselung (Encryption Service & Config-Modell)

Status: review

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

Als Administrator,
möchte ich, dass KI-Provider-API-Keys verschlüsselt in der Datenbank gespeichert werden,
damit ein Diebstahl der Datenbankinhalte nicht zum Leak meiner Gemini-Zugangsdaten führt (FR-28, AD-15).

## Acceptance Criteria

1. **Gegeben** ein aktiver `EncryptionService`, **wenn** ein API-Key gespeichert wird, **dann** erfolgt die Verschlüsselung mittels **AES-GCM** (Authenticated Encryption) und der Ciphertext wird in der Datenbank abgelegt.
2. **Gegeben** die Umgebungsvariable `AI_MASTER_KEY`, **wenn** der Service startet, **dann** wird dieser Key als Master-Key für alle Ver- und Entschlüsselungsvorgänge verwendet.
3. **Gegeben** ein fehlender oder leerer `AI_MASTER_KEY`, **wenn** eine Entschlüsselung angefordert wird, **dann** schlägt diese kontrolliert fehl (Exception) und die KI-Funktionen bleiben deaktiviert (kein Fallback auf Klartext).
4. **Gegeben** eine API-Antwort mit Konfigurationsdaten (DTO), **wenn** der API-Key enthalten ist, **dann** wird dieser **maskiert** zurückgegeben (z. B. nur die letzten 4 Zeichen sichtbar, Rest `*`), um Leaks im Browser/Log zu verhindern.
5. **Tests:** Ein Integrationstest bestätigt, dass nach dem Speichern nur der verschlüsselte Wert in der Datenbank steht und der Master-Key an keiner Stelle in der Datenbank oder im Repository persistiert wird.

## Tasks / Subtasks

- [x] **Kryptographie-Basis (Backend)**
  - [x] `EncryptionService` implementieren:
    - [x] Algorithmus: `AES/GCM/NoPadding` (GCM für Integritätsschutz).
    - [x] `AI_MASTER_KEY` aus `System.getenv()` oder `@Value` laden (kein Default im Code!).
    - [x] Key-Ableitung (z. B. PBKDF2 oder direkt 256-bit Key aus Env).
    - [x] `encrypt(String plaintext)` -> `String` (Base64 inkl. IV).
    - [x] `decrypt(String ciphertext)` -> `String`.
  - [x] Fehlerbehandlung: Eigene `EncryptionException` bei fehlendem Master-Key oder Entschlüsselungsfehlern.
- [x] **Datenmodell & Persistenz**
  - [x] `AiProviderConfig` Entity (oder entsprechende Tabelle/Klasse) anpassen:
    - [x] `apiKey` Feld speichert verschlüsselten Wert.
    - [x] Flyway-Migration (falls noch nicht vorhanden), um die Spalte für Ciphertext (VARCHAR/TEXT) vorzubereiten.
  - [x] `@Convert` oder Service-Integration in den Repository/Service-Layer, um Transparenz beim Speichern/Laden zu gewährleisten (oder explizite Aufrufe im `AiConfigService`).
- [x] **API & DTOs**
  - [x] `AiProviderConfigDto` erstellen/anpassen.
  - [x] Maskierungslogik implementieren: Ein Getter oder Mapper stellt sicher, dass der Key für die UI maskiert wird (z.B. `****...****abcd`).
- [x] **Nachweise & Qualität**
  - [x] `EncryptionServiceTest`: Unit-Tests für Roundtrip (Encrypt/Decrypt).
  - [x] `AiConfigEncryptionIntegrationTest`: 
    - [x] Speichern via Service -> Abfrage via JDBC direkt auf DB -> Assert: Wert ist verschlüsselt.
    - [x] Laden via Service -> Assert: Wert ist korrekt entschlüsselt.
    - [x] Test mit falschem/fehlendem Master-Key -> Assert: Fehlerverhalten korrekt.

## Dev Notes

### Architektur-Compliance (MUST)
- **AD-15:** Verschlüsselte Speicherung mit AES-GCM.
- **AD-16:** Master-Key nur via Env (`AI_MASTER_KEY`), keine Secrets in DB/Git.
- **NFR-15:** Keine Secrets in Logs. Der `EncryptionService` darf niemals den Klartext-Key loggen.
- **AD-10:** API-Struktur beibehalten (`core/api`).

### Technische Details
- **AES-GCM:** Benötigt einen 96-bit (12-byte) zufälligen Nonce (IV) pro Verschlüsselung. Der IV muss zusammen mit dem Ciphertext gespeichert werden (üblich: `IV + Ciphertext` als ein Base64-String).
- **Master-Key:** Der Env-Key sollte 32 Zeichen (256 Bit) lang sein. Falls kürzer/länger, ist eine Key-Ableitung (z.B. SHA-256) ratsam, um eine fixe Key-Länge für AES-256 zu erhalten.

### Dateien (Voraussichtliche Änderungen)
- `backend/src/main/java/com/cgi/kpi/dashboard/security/crypto/EncryptionService.java` (Neu)
- `backend/src/main/java/com/cgi/kpi/dashboard/security/crypto/EncryptionException.java` (Neu)
- `backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiProviderConfig.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java`
- `backend/src/main/resources/db/migration/V11_X__add_ai_config_table.sql` (Falls nötig)

## Abgrenzung
- **Story 13.4:** Der Connection-Test (Validierung des Keys gegen Gemini) und die Cache-Invalidierung erfolgen erst in der nächsten Story. 13.3 fokussiert sich rein auf die sichere Speicherung.

## Dev Agent Record

### Implementation Plan
1. Implementiert `EncryptionService` mit AES-GCM (96-bit IV, 128-bit Tag).
2. Erstellt `AiProviderConfig` Entität und `AiProviderConfigRepository`.
3. Erstellt `AiConfigService` zur Kapselung der Verschlüsselungslogik beim Speichern/Laden.
4. Erstellt `AiProviderConfigDto` mit Maskierungslogik für den API-Key.
5. Registriert neue Pakete in `JpaConfig` für Entity Scanning.
6. Validierung durch Unit- und Integrationstests.

### Completion Notes
- `EncryptionService`: Nutzt `AI_MASTER_KEY` aus der Env zur Key-Ableitung via SHA-256.
- `AiProviderConfig`: Neue DB-Tabelle via Flyway V10.
- `AiConfigService`: Handelt Transparenz der Ver-/Entschlüsselung.
- `AiProviderConfigDto`: Maskiert Keys sicher (nur letzte 4 Zeichen).
- Tests: `EncryptionServiceTest` (Unit) und `AiConfigEncryptionIntegrationTest` (Integration) sind grün.

### File List
- `backend/src/main/java/com/cgi/kpi/dashboard/security/crypto/EncryptionException.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/crypto/EncryptionService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiProviderConfig.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/dto/AiProviderConfigDto.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/persistence/AiProviderConfigRepository.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/config/JpaConfig.java`
- `backend/src/main/resources/db/migration/V10__create_ai_config_table.sql`
- `backend/src/test/java/com/cgi/kpi/dashboard/security/crypto/EncryptionServiceTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/admin/ai/AiConfigEncryptionIntegrationTest.java`

### Change Log
- 2026-07-23: Initial implementation of AI config encryption (Story 13.3).

