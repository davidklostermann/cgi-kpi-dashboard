import { Component, DestroyRef, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Subject, catchError, map, of, switchMap } from 'rxjs';

import { AiApiService } from '../../core/api/ai-api.service';
import { resolveAiPanelError } from '../../shared/utils/ai-error.util';
import {
  PortfolioInsight,
  PortfolioInsightEvidence,
  PortfolioTrendAnalysis,
} from '../../shared/models/portfolio-ai.model';
import { PortfolioFilterService } from './portfolio-filter.service';

type LoadStatus = 'loading' | 'success' | 'error' | 'disabled';

interface AffectedProjectLink {
  id: string;
  name: string;
}

const ACTIVE_INSIGHT_TYPES = new Set(['DETERIORATING_TREND', 'REPORTING_PATTERN']);

const EMPTY_MESSAGE =
  'Für den gewählten Berichtsstand wurden keine belastbaren projektübergreifenden Muster erkannt.';
const FILTERED_EMPTY_MESSAGE =
  'Es liegen Muster vor, die für die Anzeige nicht ausreichend belegt sind.';
const ERROR_MESSAGE =
  'Die Portfolio-Musteranalyse ist derzeit nicht verfügbar. KPIs und Projektdaten bleiben uneingeschränkt nutzbar.';

const CONFIDENCE_LABELS: Record<string, string> = {
  HIGH: 'Hoch',
  MEDIUM: 'Mittel',
  LOW: 'Niedrig',
};

const DATA_QUALITY_LABELS: Record<string, string> = {
  COMPLETE: 'Vollständig',
  PARTIAL: 'Teilweise',
  INSUFFICIENT: 'Unzureichend',
};

@Component({
  selector: 'app-portfolio-ai-panel',
  imports: [RouterLink],
  templateUrl: './portfolio-ai-panel.component.html',
  styleUrl: './portfolio-ai-panel.component.scss',
})
export class PortfolioAiPanelComponent {
  private readonly aiApi = inject(AiApiService);
  private readonly filterService = inject(PortfolioFilterService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload$ = new Subject<void>();

  readonly status = signal<LoadStatus>('loading');
  readonly analysis = signal<PortfolioTrendAnalysis | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly emptyMessage = EMPTY_MESSAGE;
  readonly filteredEmptyMessage = FILTERED_EMPTY_MESSAGE;

  constructor() {
    this.reload$
      .pipe(
        switchMap(() => {
          this.status.set('loading');
          this.errorMessage.set(null);
          return this.aiApi.getPortfolioTrend(this.filterService.toQueryParams()).pipe(
            map((payload) => ({ ok: true as const, payload })),
            catchError((error: unknown) => of({ ok: false as const, error })),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (!result.ok) {
          this.analysis.set(null);
          const resolved = resolveAiPanelError(result.error, ERROR_MESSAGE);
          this.status.set(resolved.status);
          this.errorMessage.set(
            resolved.status === 'disabled' ? resolved.message : ERROR_MESSAGE,
          );
          return;
        }

        if (!result.payload || !Array.isArray(result.payload.insights)) {
          this.analysis.set(null);
          this.status.set('error');
          this.errorMessage.set(ERROR_MESSAGE);
          return;
        }

        this.analysis.set(result.payload);
        this.status.set('success');
      });

    effect(() => {
      this.filterService.filters();
      this.reload$.next();
    });
  }

  load(): void {
    this.reload$.next();
  }

  displayableInsights(insights: PortfolioInsight[] | null | undefined): PortfolioInsight[] {
    if (!Array.isArray(insights)) {
      return [];
    }
    return insights
      .filter(
        (insight): insight is PortfolioInsight =>
          insight != null &&
          ACTIVE_INSIGHT_TYPES.has(insight.type) &&
          (insight.evidence?.length ?? 0) >= 2 &&
          (insight.affectedProjectIds?.length ?? 0) >= 2 &&
          Array.isArray(insight.affectedProjectNames) &&
          insight.affectedProjectNames.length >= 2,
      )
      .slice(0, 5);
  }

  emptyStateMessage(insights: PortfolioInsight[] | null | undefined): string {
    const rawCount = Array.isArray(insights) ? insights.filter((item) => item != null).length : 0;
    return rawCount > 0 ? this.filteredEmptyMessage : this.emptyMessage;
  }

  affectedProjects(insight: PortfolioInsight): AffectedProjectLink[] {
    const ids = insight.affectedProjectIds ?? [];
    const names = insight.affectedProjectNames ?? [];
    const count = Math.min(ids.length, names.length);
    const links: AffectedProjectLink[] = [];
    for (let index = 0; index < count; index++) {
      const id = ids[index]?.trim();
      const name = names[index]?.trim();
      if (id && name) {
        links.push({ id, name });
      }
    }
    return links;
  }

  readableEvidence(insight: PortfolioInsight): PortfolioInsightEvidence[] {
    return (insight.evidence ?? []).filter(
      (item): item is PortfolioInsightEvidence =>
        item != null && !!item.label?.trim() && !!item.value?.trim(),
    );
  }

  providerSublabel(aiGenerated: boolean): string {
    return aiGenerated ? 'Gemini' : 'Regelbasiert';
  }

  typeLabel(type: string): string {
    switch (type) {
      case 'DETERIORATING_TREND':
        return 'Verschlechternder Trend';
      case 'REPORTING_PATTERN':
        return 'Berichtsmuster';
      default:
        return type;
    }
  }

  confidenceLabel(value: string): string {
    return CONFIDENCE_LABELS[value] ?? value;
  }

  dataQualityLabel(value: string): string {
    return DATA_QUALITY_LABELS[value] ?? value;
  }

  formatReportDate(value: string | null | undefined): string {
    if (!value?.trim()) {
      return '';
    }
    const trimmed = value.trim();
    const dateOnly = /^(\d{4})-(\d{2})-(\d{2})$/.exec(trimmed);
    if (dateOnly) {
      const [, year, month, day] = dateOnly;
      return new Intl.DateTimeFormat('de-DE').format(
        new Date(Number(year), Number(month) - 1, Number(day)),
      );
    }
    const parsed = new Date(trimmed);
    if (Number.isNaN(parsed.getTime())) {
      return trimmed;
    }
    return new Intl.DateTimeFormat('de-DE').format(parsed);
  }
}
