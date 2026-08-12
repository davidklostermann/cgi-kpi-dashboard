import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FactsAiLayoutComponent } from '../../core/layout/facts-ai-layout.component';
import { AiPanelPlaceholderComponent } from '../ai/ai-panel-placeholder.component';

/** Project list presentation — no HttpClient (AD-10). */
@Component({
  selector: 'app-project-list-page',
  imports: [RouterLink, FactsAiLayoutComponent, AiPanelPlaceholderComponent],
  templateUrl: './project-list-page.component.html',
  styleUrl: './project-list-page.component.scss',
})
export class ProjectListPageComponent {}
