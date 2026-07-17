import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { PortfolioTrendAnalysis } from '../../shared/models/portfolio-ai.model';

/** Portfolio AI API — never calls Gemini directly (FR-14 / AD-4). */
@Injectable({ providedIn: 'root' })
export class AiApiService extends ApiClient {
  getPortfolioTrend(params?: Record<string, string | string[] | undefined>) {
    return this.get<PortfolioTrendAnalysis>('/portfolio/ai/trend-analysis', params);
  }
}
