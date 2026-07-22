import { Component, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { take } from 'rxjs';

import { ProjectAiApiService } from '../../core/api/project-ai-api.service';
import { resolveAiPanelError } from '../../shared/utils/ai-error.util';
import {
  ProjectAiAnalysis,
  ProjectAiDraft,
  ProjectAiPriority,
  ProjectAiQuestionResponse,
  ProjectAiSuggestedAction,
} from '../../shared/models/project-ai.model';

type PanelTab = 'overview' | 'actions' | 'questions';
type LoadStatus = 'idle' | 'loading' | 'success' | 'error' | 'disabled';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  evidenceFactIds?: string[];
  insufficientEvidence?: boolean;
}

const FACT_ANCHORS: Record<string, string> = {
  'project.status': 'fact-master',
  'project.lead': 'fact-master',
  'kpi.progressPercent': 'fact-kpis',
  'kpi.scheduleDeviationDays': 'fact-kpis',
  'kpi.risks.openCount': 'fact-kpis',
  'kpi.problems.openCount': 'fact-kpis',
  'budget.planned': 'fact-budget',
  'budget.actual': 'fact-budget',
  'budget.forecastDeviation': 'fact-budget',
  'budget.forecastAtCompletion': 'fact-budget',
  'report.progressDeltaPercent': 'fact-report-comparison',
  'report.statusChange': 'fact-report-comparison',
};

@Component({
  selector: 'app-project-ai-panel',
  imports: [FormsModule],
  templateUrl: './project-ai-panel.component.html',
  styleUrl: './project-ai-panel.component.scss',
})
export class ProjectAiPanelComponent {
  private readonly projectAiApi = inject(ProjectAiApiService);

  readonly projectId = input.required<string>();

  readonly activeTab = signal<PanelTab>('overview');
  readonly status = signal<LoadStatus>('idle');
  readonly analysis = signal<ProjectAiAnalysis | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly draft = signal<ProjectAiDraft | null>(null);

  readonly chatMessages = signal<ChatMessage[]>([]);
  readonly chatInput = signal('');
  readonly chatStatus = signal<'idle' | 'sending' | 'error' | 'disabled'>('idle');
  readonly chatError = signal<string | null>(null);

  private analysisGeneration = 0;
  private chatGeneration = 0;

  readonly suggestedQuestions = [
    'Wie ist der aktuelle Fortschritt?',
    'Gibt es eine Terminabweichung?',
    'Wie steht das Budget?',
  ];

  constructor() {
    effect(() => {
      this.projectId();
      this.analysisGeneration++;
      this.chatGeneration++;
      this.chatMessages.set([]);
      this.chatStatus.set('idle');
      this.chatError.set(null);
      this.draft.set(null);
      this.loadAnalysis(false);
    });
  }

  selectTab(tab: PanelTab): void {
    this.activeTab.set(tab);
  }

  chatInputDisabled(): boolean {
    const status = this.chatStatus();
    return status === 'sending' || status === 'disabled';
  }

  /** Insights without at least two readable evidence items are not shown. */
  displayablePriorities(priorities: ProjectAiPriority[]): ProjectAiPriority[] {
    return (priorities ?? [])
      .filter((priority) => this.hasReadableEvidence(priority))
      .slice(0, 3);
  }

  /**
   * Keine generischen/leeren Maßnahmenkarten — nur belegte, konkrete Vorschläge.
   * Generische Steering-Listen werden bewusst ausgeblendet; Entscheidungen stehen im Überblick.
   */
  displayableActions(actions: ProjectAiSuggestedAction[]): ProjectAiSuggestedAction[] {
    return (actions ?? []).filter((action) => this.isConcreteAction(action));
  }

  formatEvidence(item: { label: string; value: string }): string {
    return `${item.label}: ${item.value}`;
  }

  formatOptionalField(value: string | null | undefined): string {
    if (!value || !value.trim()) {
      return 'Nicht in den Projektdaten hinterlegt';
    }
    return value.trim();
  }

  private hasReadableEvidence(priority: ProjectAiPriority): boolean {
    const evidence = priority.evidence ?? [];
    if (evidence.length < 2) {
      return false;
    }
    return evidence.every(
      (item) => !!item.label?.trim() && !!item.value?.trim() && !this.looksLikeTechField(item.label),
    );
  }

  private isConcreteAction(action: ProjectAiSuggestedAction): boolean {
    if (!action.title?.trim() || !action.reason?.trim()) {
      return false;
    }
    if (!(action.evidenceFactIds ?? []).some((id) => !!id?.trim())) {
      return false;
    }
    const title = action.title.toLowerCase();
    if (title.startsWith('steering-vorbereitung') || title.startsWith('steering vorbereiten')) {
      return false;
    }
    if (/allgemeine\s+ma[sß]nahme|generisch/i.test(title)) {
      return false;
    }
    return true;
  }

  private looksLikeTechField(label: string): boolean {
    return /^[a-z0-9]+(\.[a-z0-9]+)+$/i.test(label.trim());
  }

  onTabKeydown(event: KeyboardEvent): void {
    const tabs: PanelTab[] = ['overview', 'actions', 'questions'];
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

  loadAnalysis(refresh: boolean): void {
    const projectId = this.projectId();
    const generation = ++this.analysisGeneration;
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectAiApi
      .getAnalysis(projectId, refresh)
      .pipe(take(1))
      .subscribe({
        next: (analysis) => {
          if (generation !== this.analysisGeneration || projectId !== this.projectId()) {
            return;
          }
          this.analysis.set(analysis);
          this.status.set('success');
        },
        error: (error: unknown) => {
          if (generation !== this.analysisGeneration || projectId !== this.projectId()) {
            return;
          }
          this.analysis.set(null);
          const resolved = resolveAiPanelError(error, 'Die Analyse konnte nicht geladen werden.');
          this.status.set(resolved.status);
          this.errorMessage.set(resolved.message);
        },
      });
  }

  jumpToFact(factId: string): void {
    const anchor =
      FACT_ANCHORS[factId] ??
      (factId.startsWith('risk.') ||
      factId.startsWith('problem.') ||
      factId.startsWith('issue.')
        ? 'fact-issues-actions'
        : factId.startsWith('phase.') || factId.startsWith('milestone.')
          ? 'fact-phases'
          : null);
    if (!anchor) {
      return;
    }
    const el = document.getElementById(anchor);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      el.classList.add('fact-highlight');
      window.setTimeout(() => el.classList.remove('fact-highlight'), 1600);
    }
  }

  prepareDraft(action: ProjectAiSuggestedAction): void {
    this.draft.set({
      title: action.title,
      body: `${action.reason}\n\nErwartete Wirkung: ${this.formatOptionalField(action.expectedEffect)}`,
      owner: action.suggestedOwner ?? '',
      dueDate: action.suggestedDueDate ?? '',
    });
    this.activeTab.set('actions');
  }

  clearDraft(): void {
    this.draft.set(null);
  }

  sendQuestion(question?: string): void {
    if (this.chatInputDisabled()) {
      return;
    }
    const text = (question ?? this.chatInput()).trim();
    if (!text) {
      return;
    }
    this.chatMessages.update((messages) => [...messages, { role: 'user', text }]);
    this.chatInput.set('');
    this.chatStatus.set('sending');
    this.chatError.set(null);

    const projectId = this.projectId();
    const generation = ++this.chatGeneration;
    this.projectAiApi
      .askQuestion(projectId, text)
      .pipe(take(1))
      .subscribe({
        next: (response: ProjectAiQuestionResponse) => {
          if (generation !== this.chatGeneration || projectId !== this.projectId()) {
            return;
          }
          this.chatMessages.update((messages) => [
            ...messages,
            {
              role: 'assistant',
              text: response.answer,
              evidenceFactIds: response.evidenceFactIds,
              insufficientEvidence: response.insufficientEvidence,
            },
          ]);
          this.chatStatus.set('idle');
        },
        error: (error: unknown) => {
          if (generation !== this.chatGeneration || projectId !== this.projectId()) {
            return;
          }
          const resolved = resolveAiPanelError(error, 'Die Frage konnte nicht beantwortet werden.');
          this.chatStatus.set(resolved.status === 'disabled' ? 'disabled' : 'error');
          this.chatError.set(resolved.message);
        },
      });
  }

  retryLastQuestion(): void {
    if (this.chatStatus() === 'disabled') {
      return;
    }
    const lastUser = [...this.chatMessages()].reverse().find((message) => message.role === 'user');
    if (!lastUser) {
      return;
    }
    this.chatError.set(null);
    this.sendQuestion(lastUser.text);
  }

  formatInstant(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }
    return new Intl.DateTimeFormat('de-DE', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }
}
