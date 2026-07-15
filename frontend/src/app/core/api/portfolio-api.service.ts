import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';

/** Portfolio facts API — stub for Story 1.2 structure. */
@Injectable({ providedIn: 'root' })
export class PortfolioApiService extends ApiClient {
  /** Dev smoke probe — Actuator is not under `/api`; proxied at `/actuator`. */
  getHealthProbe() {
    return this.getAtRoot<{ status: string }>('/actuator/health');
  }
}
