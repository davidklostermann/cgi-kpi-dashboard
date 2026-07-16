import { Component } from '@angular/core';

import { FactsAiLayoutComponent } from '../../core/layout/facts-ai-layout.component';
import { AiPanelPlaceholderComponent } from '../ai/ai-panel-placeholder.component';
import { PortfolioGanttSectionComponent } from './portfolio-gantt-section.component';
import { PortfolioTableSectionComponent } from './portfolio-table-section.component';
import { PortfolioTrendsSectionComponent } from './portfolio-trends-section.component';
import { PortfolioFilterBarComponent } from './portfolio-filter-bar.component';
import { PortfolioKpiSectionComponent } from './portfolio-kpi-section.component';

/** Portfolio presentation shell — no HttpClient (AD-10). */
@Component({
  selector: 'app-portfolio-page',
  imports: [
    FactsAiLayoutComponent,
    AiPanelPlaceholderComponent,
    PortfolioFilterBarComponent,
    PortfolioKpiSectionComponent,
    PortfolioTrendsSectionComponent,
    PortfolioGanttSectionComponent,
    PortfolioTableSectionComponent,
  ],
  templateUrl: './portfolio-page.component.html',
  styleUrl: './portfolio-page.component.scss',
})
export class PortfolioPageComponent {}
