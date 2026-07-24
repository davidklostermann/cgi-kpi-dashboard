import { DOCUMENT } from '@angular/common';
import { CdkTrapFocus } from '@angular/cdk/a11y';
import { Component, ElementRef, HostListener, inject, signal, viewChild } from '@angular/core';

import { AuthService } from '../../core/auth/auth.service';
import { PortfolioAiPanelComponent } from './portfolio-ai-panel.component';
import { PortfolioGanttSectionComponent } from './portfolio-gantt-section.component';
import { PortfolioTableSectionComponent } from './portfolio-table-section.component';
import { PortfolioTrendsSectionComponent } from './portfolio-trends-section.component';
import { PortfolioFilterBarComponent } from './portfolio-filter-bar.component';
import { PortfolioKpiSectionComponent } from './portfolio-kpi-section.component';

/** Portfolio presentation shell — no HttpClient (AD-10). */
@Component({
  selector: 'app-portfolio-page',
  imports: [
    CdkTrapFocus,
    PortfolioAiPanelComponent,
    PortfolioFilterBarComponent,
    PortfolioKpiSectionComponent,
    PortfolioTrendsSectionComponent,
    PortfolioGanttSectionComponent,
    PortfolioTableSectionComponent,
  ],
  templateUrl: './portfolio-page.component.html',
  styleUrl: './portfolio-page.component.scss',
})
export class PortfolioPageComponent {
  readonly authService = inject(AuthService);
  private readonly document = inject(DOCUMENT);
  private readonly launcher = viewChild<ElementRef<HTMLButtonElement>>('aiLauncher');

  readonly portfolioAiOpen = signal(false);
  private previousBodyOverflow = '';

  openPortfolioAi(): void {
    this.previousBodyOverflow = this.document.body.style.overflow;
    this.document.body.style.overflow = 'hidden';
    this.portfolioAiOpen.set(true);
  }

  closePortfolioAi(): void {
    if (!this.portfolioAiOpen()) {
      return;
    }

    this.portfolioAiOpen.set(false);
    this.document.body.style.overflow = this.previousBodyOverflow;
    this.launcher()?.nativeElement.focus();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closePortfolioAi();
  }
}
