import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AiProviderConfig {
  id?: string;
  provider: string;
  model: string;
  apiKeyMasked: string;
  enabled: boolean;
}

export interface SaveAiConfigRequest {
  provider: string;
  model: string;
  apiKey?: string;
  enabled: boolean;
}

export interface ConnectionTestResponse {
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiConfigService {
  private http = inject(HttpClient);
  private apiUrl = '/api/admin/ai';

  getConfig(provider: string = 'gemini'): Observable<AiProviderConfig> {
    return this.http.get<AiProviderConfig>(`${this.apiUrl}/config?provider=${provider}`);
  }

  saveConfig(request: SaveAiConfigRequest): Observable<AiProviderConfig> {
    return this.http.put<AiProviderConfig>(`${this.apiUrl}/config`, request);
  }

  testConnection(provider: string = 'gemini'): Observable<ConnectionTestResponse> {
    return this.http.post<ConnectionTestResponse>(`${this.apiUrl}/test-connection?provider=${provider}`, {});
  }
}

export const AI_CONFIG_MESSAGES = {
  LOAD_CONFIG_ERROR: 'Fehler beim Laden der Konfiguration',
  SAVE_CONFIG_SUCCESS: 'Konfiguration erfolgreich gespeichert',
  SAVE_CONFIG_ERROR: 'Fehler beim Speichern',
  CONNECTION_TEST_FAILED: 'Verbindungstest fehlgeschlagen',
  CONNECTION_TEST_UNSUPPORTED_PROVIDER: (provider: string) => `Provider ${provider} not supported for testing yet.`,
  CONNECTION_TEST_SUCCESS: 'Verbindung erfolgreich: Gemini antwortet.',
  CONNECTION_TEST_UNEXPECTED_RESPONSE: (response: string) => `Unerwartete Antwort von Gemini: ${response}`,
  CONNECTION_TEST_ERROR: (message: string) => `Verbindungsfehler: ${message}`,
  CONNECTION_TEST_UNEXPECTED_ERROR: (message: string) => `Unerwarteter Fehler beim Verbindungstest: ${message}`
};