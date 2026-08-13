import { Component, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';

import { AiApiService } from '../../core/api/ai-api.service';
import { ProjectAiApiService } from '../../core/api/project-ai-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { resolveAiPanelError } from '../../shared/utils/ai-error.util';
import {
  AiFactReference,
  resolveAiFactLabel,
  resolveAiFactReferences,
} from '../../shared/utils/ai-error.util';
import {
  PortfolioInsight,
  PortfolioInsightEvidence,
  PortfolioTrendAnalysis,
} from '../../shared/models/portfolio-ai.model';
import { ProjectAiQuestionResponse } from '../../shared/models/project-ai.model';
import { AI_KEY_MISSING_MESSAGE } from '../project/project-ai-panel.component';
import { PortfolioFilterService } from './portfolio-filter.service';

type PanelTab = 'overview' | 'questions';
type LoadStatus = 'idle' | 'loading' | 'success' | 'error' | 'disabled' | 'key_missing';

interface AffectedProjectLink {
  id: string;
  name: string;
}

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  evidenceFactIds?: string[];
  insufficientEvidence?: boolean;
}

const ACTIVE_INSIGHT_TYPES = new Set(['DETERIORATING_TREND', 'REPORTING_PATTERN']);

const EMPTY_MESSAGE =
  'Für den gewählten Berichtsstand wurden keine belastbaren projektübergreifenden Muster erkannt.';
const FILTERED_EMPTY_MESSAGE =
  'Es liegen Muster vor, die für die Anzeige nicht ausreichend belegt sind.';
const ERROR_MESSAGE =
  'Die Portfolio-Musteranalyse ist derzeit nicht verfügbar. KPIs und Projektdaten bleiben uneingeschränkt nutzbar.';
const CHAT_ERROR_MESSAGE = 'Die Frage konnte nicht beantwortet werden.';

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
  imports: [FormsModule, RouterLink],
  templateUrl: './portfolio-ai-panel.component.html',
  styleUrl: './portfolio-ai-panel.component.scss',
})
export class PortfolioAiPanelComponent {
  private readonly aiApi = inject(AiApiService);
  private readonly projectAiApi = inject(ProjectAiApiService);
  private readonly filterService = inject(PortfolioFilterService);
  readonly authService = inject(AuthService);

  readonly activeTab = signal<PanelTab>('overview');
  readonly status = signal<LoadStatus>('idle');
  readonly analysis = signal<PortfolioTrendAnalysis | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly emptyMessage = EMPTY_MESSAGE;
  readonly filteredEmptyMessage = FILTERED_EMPTY_MESSAGE;
  readonly keyMissingMessage = AI_KEY_MISSING_MESSAGE;

  readonly chatMessages = signal<ChatMessage[]>([]);
  readonly chatInput = signal('');
  readonly chatStatus = signal<'idle' | 'sending' | 'error' | 'disabled'>('idle');
  readonly chatError = signal<string | null>(null);

  readonly suggestedQuestions = [
    'Welche Projekte sind kritisch?',
    'Wie ist die Budgetabweichung im Portfolio?',
    'Gibt es terminliche Risiken?',
  ];

  private loadGeneration = 0;
  private chatGeneration = 0;
  private analysisStarted = false;
  private lastUserIdentity: string | null = null;
  private lastFilterKey: string | null = null;
  private lastAttemptedQuestion: string | null = null;

  constructor() {
    effect(() => {
      const user = this.authService.currentUser();
      const filters = this.filterService.filters();
      const userIdentity = user ? `${user.workspaceId}:${user.userId}` : null;
      const filterKey = JSON.stringify(filters);

      untracked(() => {
        if (userIdentity !== this.lastUserIdentity) {
          this.lastUserIdentity = userIdentity;
          this.lastFilterKey = filterKey;
          this.analysisStarted = false;
          this.resetAiState();
          return;
        }

        if (filterKey !== this.lastFilterKey) {
          this.lastFilterKey = filterKey;
          this.resetChatState();
        }

        if (!this.analysisStarted || this.status() === 'key_missing') {
          return;
        }

        this.runLoad(false);
      });
    });
  }

  selectTab(tab: PanelTab): void {
    if (this.status() === 'key_missing') {
      return;
    }
    this.activeTab.set(tab);
  }

  onTabKeydown(event: KeyboardEvent): void {
    if (this.status() === 'key_missing') {
      return;
    }
    const tabs: PanelTab[] = ['overview', 'questions'];
    const index = tabs.indexOf(this.activeTab());
    if (event.key === 'ArrowRight' || event.key === 'ArrowLeft') {
      event.preventDefault();
      const next =
        event.key === 'ArrowRight'
          ? tabs[(index + 1) % tabs.length]
          : tabs[(index - 1 + tabs.length) % tabs.length];
      this.activeTab.set(next);
    } else if (event.key === 'Home') {
      event.preventDefault();
      this.activeTab.set('overview');
    } else if (event.key === 'End') {
      event.preventDefault();
      this.activeTab.set('questions');
    }
  }

  chatInputDisabled(): boolean {
    const status = this.status();
    const chatStatus = this.chatStatus();
    return status === 'key_missing' || chatStatus === 'sending' || chatStatus === 'disabled';
  }

  load(): void {
    if (this.status() === 'key_missing') {
      return;
    }
    this.analysisStarted = true;
    this.runLoad(true);
  }

  private runLoad(clearError: boolean): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    if (clearError) {
      this.errorMessage.set(null);
    }

    this.projectAiApi
      .checkReadiness()
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          if (generation !== this.loadGeneration) {
            return;
          }
          if (response.ready !== true) {
            this.applyKeyMissingState();
            return;
          }
          this.fetchTrend(generation);
        },
        error: (error: unknown) => {
          if (generation !== this.loadGeneration) {
            return;
          }
          this.applyResolvedError(error, ERROR_MESSAGE);
        },
      });
  }

  private fetchTrend(generation: number): void {
    this.aiApi
      .getPortfolioTrend(this.filterService.toQueryParams())
      .pipe(take(1))
      .subscribe({
        next: (payload) => {
          if (generation !== this.loadGeneration) {
            return;
          }

          if (!payload || !Array.isArray(payload.insights)) {
            this.analysis.set(null);
            this.status.set('error');
            this.errorMessage.set(ERROR_MESSAGE);
            return;
          }

          this.analysis.set(payload);
          this.status.set('success');
        },
        error: (error: unknown) => {
          if (generation !== this.loadGeneration) {
            return;
          }
          this.applyResolvedError(error, ERROR_MESSAGE);
        },
      });
  }

  sendQuestion(question?: string): void {
    if (this.chatInputDisabled()) {
      return;
    }
    const text = (question ?? this.chatInput()).trim();
    if (!text) {
      return;
    }

    const generation = ++this.chatGeneration;
    const queryParams = this.filterService.toQueryParams();
    this.lastAttemptedQuestion = text;
    this.chatInput.set('');
    this.chatStatus.set('sending');
    this.chatError.set(null);

    this.projectAiApi
      .checkReadiness()
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          if (generation !== this.chatGeneration) {
            return;
          }
          if (response.ready !== true) {
            this.applyKeyMissingState();
            return;
          }
          this.aiApi
            .askPortfolioQuestion(text, queryParams)
            .pipe(take(1))
            .subscribe({
              next: (response: ProjectAiQuestionResponse) => {
                if (generation !== this.chatGeneration) {
                  return;
                }
                this.chatMessages.update((messages) => [
                  ...messages,
                  { role: 'user', text },
                  {
                    role: 'assistant',
                    text: response.answer,
                    evidenceFactIds: response.evidenceFactIds,
                    insufficientEvidence: response.insufficientEvidence,
                  },
                ]);
                this.chatStatus.set('idle');
                this.lastAttemptedQuestion = null;
              },
              error: (error: unknown) => {
                if (generation !== this.chatGeneration) {
                  return;
                }
                this.handleQuestionError(error);
              },
            });
        },
        error: (error: unknown) => {
          if (generation !== this.chatGeneration) {
            return;
          }
          this.handleQuestionError(error);
        },
      });
  }

  retryLastQuestion(): void {
    if (this.chatStatus() === 'disabled' || this.status() === 'key_missing') {
      return;
    }
    const question = this.lastAttemptedQuestion;
    if (!question) {
      return;
    }
    this.chatError.set(null);
    this.sendQuestion(question);
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
    return (insight.evidence ?? []).flatMap((item) => {
      if (item == null || !item.label?.trim() || !item.value?.trim()) {
        return [];
      }
      const label = item.label.trim();
      const technicalLabel = /^[a-z0-9]+(?:\.[a-z0-9]+)+$/i.test(label);
      const resolvedLabel = technicalLabel
        ? resolveAiFactLabel(item.sourceField?.trim() || label)
        : label;
      return resolvedLabel ? [{ ...item, label: resolvedLabel }] : [];
    });
  }

  providerSublabel(aiGenerated: boolean): string {
    return aiGenerated ? 'KI-gestützte Analyse' : 'Aus Portfoliodaten abgeleitet';
  }

  factReferences(factIds: string[] | null | undefined): AiFactReference[] {
    return resolveAiFactReferences(factIds);
  }

  analysisBadge(): string {
    return this.analysis()?.aiGenerated === false ? 'Portfolioanalyse' : 'KI-Einschätzung';
  }

  jumpToFact(factId: string): void {
    const anchor = factId.startsWith('snapshot.')
      ? 'fact-portfolio-trends'
      : factId.startsWith('portfolio.') || factId.startsWith('budget.') || factId.startsWith('kpi.')
        ? 'fact-portfolio-kpis'
        : factId.startsWith('phase.') || factId.startsWith('milestone.')
          ? 'fact-portfolio-timeline'
          : 'fact-portfolio-projects';
    const element = document.getElementById(anchor);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      element.classList.add('fact-highlight');
      window.setTimeout(() => element.classList.remove('fact-highlight'), 1600);
    }
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

  private handleQuestionError(error: unknown): void {
    const resolved = resolveAiPanelError(error, CHAT_ERROR_MESSAGE);
    if (resolved.status === 'key_missing') {
      this.applyKeyMissingState(resolved.message);
      return;
    }
    this.chatStatus.set(resolved.status === 'disabled' ? 'disabled' : 'error');
    this.chatError.set(resolved.message);
  }

  private resetAiState(): void {
    this.loadGeneration++;
    this.chatGeneration++;
    this.clearDisplayedAiContent();
    this.activeTab.set('overview');
    this.status.set('idle');
    this.errorMessage.set(null);
    this.chatStatus.set('idle');
    this.chatError.set(null);
    this.chatInput.set('');
    this.lastAttemptedQuestion = null;
  }

  private resetChatState(): void {
    this.chatGeneration++;
    this.chatMessages.set([]);
    this.chatInput.set('');
    this.chatStatus.set('idle');
    this.chatError.set(null);
    this.lastAttemptedQuestion = null;
  }

  private clearDisplayedAiContent(): void {
    this.analysis.set(null);
    this.chatMessages.set([]);
  }

  private applyKeyMissingState(message = AI_KEY_MISSING_MESSAGE): void {
    this.clearDisplayedAiContent();
    this.status.set('key_missing');
    this.errorMessage.set(message);
    this.analysisStarted = false;
    this.chatStatus.set('disabled');
    this.chatError.set(null);
    this.lastAttemptedQuestion = null;
  }

  private applyResolvedError(error: unknown, fallback: string): void {
    const resolved = resolveAiPanelError(error, fallback);
    if (resolved.status === 'key_missing') {
      this.applyKeyMissingState(resolved.message);
      return;
    }

    this.analysis.set(null);
    const panelStatus = resolved.status === 'disabled' ? 'disabled' : 'error';
    this.status.set(panelStatus);
    this.errorMessage.set(resolved.status === 'disabled' ? resolved.message : fallback);
  }
}
