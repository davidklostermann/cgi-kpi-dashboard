import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { API_BASE_URL } from './api.config';

/**
 * Base HTTP access for typed feature API services (AD-10).
 * Presentation components must not inject HttpClient directly.
 */
@Injectable({ providedIn: 'root' })
export class ApiClient {
  private readonly http = inject(HttpClient);

  protected get baseUrl(): string {
    return API_BASE_URL;
  }

  protected get<T>(path: string) {
    return this.http.get<T>(`${this.baseUrl}${path}`);
  }

  /** Paths outside `/api` (e.g. Spring Actuator) — proxied separately in dev. */
  protected getAtRoot<T>(path: string) {
    return this.http.get<T>(path);
  }
}
