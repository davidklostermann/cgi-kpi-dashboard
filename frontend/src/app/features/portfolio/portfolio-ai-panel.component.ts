import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';

import { AiApiService } from '../../core/api/ai-api.service';
import { resolveAiPanelError } from '../../shared/utils/ai-error.util';
import { PortfolioTrendAnalysis } from '../../shared/models/portfolio-ai.model';

type LoadStatus = 'loading' | 'success' | 'error' | 'disabled';

@Component({
  selector: 'app-portfolio-ai-panel',
  imports: [RouterLink],
  templateUrl: './portfolio-ai-panel.component.html',
  styleUrl: './portfolio-ai-panel.component.scss',
})
export class PortfolioAiPanelComponent {
  private readonly aiApi = inject(AiApiService);

  readonly status = signal<LoadStatus>('loading');
  readonly analysis = signal<PortfolioTrendAnalysis | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.status.set('loading');
    this.errorMessage.set(null);
    this.aiApi
      .getPortfolioTrend()
      .pipe(take(1))
      .subscribe({
        next: (payload) => {
          this.analysis.set(payload);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.analysis.set(null);
          const resolved = resolveAiPanelError(
            error,
            'Die KI-Trendanalyse konnte nicht geladen werden.',
          );
          this.status.set(resolved.status);
          this.errorMessage.set(resolved.message);
        },
      });
  }
}
