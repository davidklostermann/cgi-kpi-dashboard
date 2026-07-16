import { Component, effect, inject, signal } from '@angular/core';
import { take } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { PortfolioApiService } from '../../core/api/portfolio-api.service';
import { ProjectTableComponent } from '../../shared/components/project-table.component';
import { PortfolioTable } from '../../shared/models/portfolio-table.model';
import { PortfolioFilterService } from './portfolio-filter.service';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-portfolio-table-section',
  imports: [ProjectTableComponent],
  templateUrl: './portfolio-table-section.component.html',
  styleUrl: './portfolio-table-section.component.scss',
})
export class PortfolioTableSectionComponent {
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly filterService = inject(PortfolioFilterService);

  readonly status = signal<LoadStatus>('loading');
  readonly table = signal<PortfolioTable | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.filterService.filters();
      this.load();
    });
  }

  load(): void {
    this.status.set('loading');
    this.errorMessage.set(null);

    this.portfolioApi
      .getPortfolioProjects(this.filterService.toQueryParams())
      .pipe(take(1))
      .subscribe({
        next: (data) => {
          this.table.set(data);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.table.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
  }

  private resolveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
      if (error.status === 0) {
        return 'Die Projekttabelle konnte nicht geladen werden. Bitte prüfen Sie die Verbindung zum Backend.';
      }
    }
    return 'Die Projekttabelle konnte nicht geladen werden. Bitte versuchen Sie es erneut.';
  }
}
