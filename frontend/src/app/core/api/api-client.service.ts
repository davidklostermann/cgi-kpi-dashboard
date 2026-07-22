import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

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

  protected get<T>(path: string, query?: Record<string, string | string[] | null | undefined>) {
    return this.http.get<T>(`${this.baseUrl}${path}`, { params: this.toHttpParams(query) });
  }

  protected post<T>(path: string, body: unknown) {
    return this.http.post<T>(`${this.baseUrl}${path}`, body);
  }

  protected put<T>(path: string, body: unknown) {
    return this.http.put<T>(`${this.baseUrl}${path}`, body);
  }

  protected delete<T>(path: string) {
    return this.http.delete<T>(`${this.baseUrl}${path}`);
  }

  private toHttpParams(query?: Record<string, string | string[] | null | undefined>): HttpParams | undefined {
    if (!query) {
      return undefined;
    }

    let params = new HttpParams();
    let hasValues = false;

    for (const [key, value] of Object.entries(query)) {
      if (value == null || value === '') {
        continue;
      }
      if (Array.isArray(value)) {
        for (const entry of value) {
          if (entry) {
            params = params.append(key, entry);
            hasValues = true;
          }
        }
      } else {
        params = params.set(key, value);
        hasValues = true;
      }
    }

    return hasValues ? params : undefined;
  }

  /** Paths outside `/api` (e.g. Spring Actuator) — proxied separately in dev. */
  protected getAtRoot<T>(path: string) {
    return this.http.get<T>(path);
  }
}
