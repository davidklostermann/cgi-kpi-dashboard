import { Component, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { take } from 'rxjs';

import { ProjectAiApiService } from '../../core/api/project-ai-api.service';
import { resolveAiPanelError } from '../../shared/utils/ai-error.util';
import {
  ProjectAiAnalysis,
  ProjectAiDraft,
  ProjectAiQuestionResponse,
  ProjectAiSuggestedAction,
} from '../../shared/models/project-ai.model';

type PanelTab = 'overview' | 'actions' | 'questions';
type LoadStatus = 'idle' | 'loading' | 'success' | 'error' | 'disabled';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  evidenceFactIds?: string[];
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
  readonly chatStatus = signal<'idle' | 'sending' | 'error'>('idle');
  readonly chatError = signal<string | null>(null);

  readonly suggestedQuestions = [
    'Wie ist der aktuelle Fortschritt?',
    'Gibt es eine Terminabweichung?',
    'Wie steht das Budget?',
  ];

  constructor() {
    effect(() => {
      this.projectId();
      this.loadAnalysis(false);
    });
  }

  selectTab(tab: PanelTab): void {
    this.activeTab.set(tab);
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
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectAiApi
      .getAnalysis(this.projectId(), refresh)
      .pipe(take(1))
      .subscribe({
        next: (analysis) => {
          this.analysis.set(analysis);
          this.status.set('success');
        },
        error: (error: unknown) => {
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
      (factId.startsWith('insight.')
        ? 'fact-insights'
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
      body: `${action.reason}\n\nErwartete Wirkung: ${action.expectedEffect ?? 'nicht ableitbar'}`,
      owner: action.suggestedOwner ?? '',
      dueDate: action.suggestedDueDate ?? '',
    });
    this.activeTab.set('actions');
  }

  clearDraft(): void {
    this.draft.set(null);
  }

  sendQuestion(question?: string): void {
    const text = (question ?? this.chatInput()).trim();
    if (!text) {
      return;
    }
    this.chatMessages.update((messages) => [...messages, { role: 'user', text }]);
    this.chatInput.set('');
    this.chatStatus.set('sending');
    this.chatError.set(null);

    this.projectAiApi
      .askQuestion(this.projectId(), text)
      .pipe(take(1))
      .subscribe({
        next: (response: ProjectAiQuestionResponse) => {
          this.chatMessages.update((messages) => [
            ...messages,
            {
              role: 'assistant',
              text: response.answer,
              evidenceFactIds: response.evidenceFactIds,
            },
          ]);
          this.chatStatus.set('idle');
        },
        error: (error: unknown) => {
          this.chatStatus.set('error');
          this.chatError.set(
            resolveAiPanelError(error, 'Die Frage konnte nicht beantwortet werden.').message,
          );
        },
      });
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
