import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';

/** Nested AI endpoints — separate streams per panel (AD-7). Stub for Story 1.2. */
@Injectable({ providedIn: 'root' })
export class AiApiService extends ApiClient {
  getPortfolioTrend() {
    return this.get<{ text: string }>('/portfolio/ai/trend-analysis');
  }
}
