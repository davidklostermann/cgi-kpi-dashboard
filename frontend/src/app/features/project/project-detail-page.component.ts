import { Component, computed, input } from '@angular/core';

import { FactsAiLayoutComponent } from '../../core/layout/facts-ai-layout.component';
import { BreadcrumbsComponent } from '../../core/navigation/breadcrumbs.component';
import { AiPanelPlaceholderComponent } from '../ai/ai-panel-placeholder.component';

/** Mock project titles until API wiring in Epic 3/6. */
const MOCK_PROJECT_NAMES: Record<string, string> = {
  'demo-alpha': 'Projekt Alpha',
  'demo-beta': 'Projekt Beta',
};

@Component({
  selector: 'app-project-detail-page',
  imports: [BreadcrumbsComponent, FactsAiLayoutComponent, AiPanelPlaceholderComponent],
  templateUrl: './project-detail-page.component.html',
  styleUrl: './project-detail-page.component.scss',
})
export class ProjectDetailPageComponent {
  readonly id = input.required<string>();

  readonly projectName = computed(
    () => MOCK_PROJECT_NAMES[this.id()] ?? `Projekt ${this.id()}`,
  );

  readonly breadcrumbs = computed(() => [
    { label: 'Portfolio', route: ['/portfolio'] as string[] },
    { label: this.projectName() },
  ]);
}
