import { DatePipe } from '@angular/common';
import {
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';

import { StatusBadgeComponent } from './status-badge.component';
import {
  PortfolioTableRow,
  PortfolioTableSortKey,
} from '../models/portfolio-table.model';

const STATUS_ORDER: Record<string, number> = {
  CRITICAL: 0,
  AT_RISK: 1,
  ON_TRACK: 2,
  COMPLETED: 3,
};

/** Portfolio management table (FR-2 / Story 5.3). */
@Component({
  selector: 'app-project-table',
  imports: [DatePipe, StatusBadgeComponent],
  templateUrl: './project-table.component.html',
  styleUrl: './project-table.component.scss',
})
export class ProjectTableComponent {
  private readonly router = inject(Router);

  readonly rows = input.required<PortfolioTableRow[]>();
  readonly rowClick = output<string>();

  readonly sortKey = signal<PortfolioTableSortKey>('status');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  readonly sortedRows = computed(() => {
    const key = this.sortKey();
    const direction = this.sortDirection();
    const multiplier = direction === 'asc' ? 1 : -1;

    return [...this.rows()].sort(
      (left, right) => multiplier * this.compareRows(left, right, key),
    );
  });

  toggleSort(key: PortfolioTableSortKey): void {
    if (this.sortKey() === key) {
      this.sortDirection.update((current) =>
        current === 'asc' ? 'desc' : 'asc',
      );
      return;
    }
    this.sortKey.set(key);
    this.sortDirection.set(key === 'lastDataUpdate' ? 'desc' : 'asc');
  }

  sortIndicator(key: PortfolioTableSortKey): string {
    if (this.sortKey() !== key) {
      return '';
    }
    return this.sortDirection() === 'asc' ? '↑' : '↓';
  }

  ariaSort(key: PortfolioTableSortKey): 'ascending' | 'descending' | 'none' {
    if (this.sortKey() !== key) {
      return 'none';
    }
    return this.sortDirection() === 'asc' ? 'ascending' : 'descending';
  }

  formatPercent(value: number | null): string {
    if (value == null) {
      return '—';
    }
    return `${value.toLocaleString('de-DE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} %`;
  }

  onRowClick(projectId: string): void {
    this.rowClick.emit(projectId);
    void this.router.navigate(['/projects', projectId]);
  }

  onRowKeydown(event: KeyboardEvent, projectId: string): void {
    if (event.key !== 'Enter') {
      return;
    }
    event.preventDefault();
    this.onRowClick(projectId);
  }

  formatSignedPercent(value: number | null): string {
    if (value == null) {
      return '—';
    }
    const formatted = Math.abs(value).toLocaleString('de-DE', {
      minimumFractionDigits: 1,
      maximumFractionDigits: 1,
    });
    if (value > 0) {
      return `+${formatted} %`;
    }
    if (value < 0) {
      return `−${formatted} %`;
    }
    return `${formatted} %`;
  }

  formatDays(value: number | null): string {
    if (value == null) {
      return '—';
    }
    if (value > 0) {
      return `+${value} T`;
    }
    if (value < 0) {
      return `${value} T`;
    }
    return '0 T';
  }

  private compareRows(
    left: PortfolioTableRow,
    right: PortfolioTableRow,
    key: PortfolioTableSortKey,
  ): number {
    switch (key) {
      case 'status':
        return (
          (STATUS_ORDER[left.status] ?? 99) - (STATUS_ORDER[right.status] ?? 99)
        );
      case 'progressPercent':
        return left.progressPercent - right.progressPercent;
      case 'scheduleDeviationDays':
        return (
          (left.scheduleDeviationDays ?? -9999) -
          (right.scheduleDeviationDays ?? -9999)
        );
      case 'budgetDeviationPercent':
        return (
          (left.budgetDeviationPercent ?? -9999) -
          (right.budgetDeviationPercent ?? -9999)
        );
      case 'criticalIssueCount':
        return left.criticalIssueCount - right.criticalIssueCount;
      case 'lastDataUpdate':
        return (left.lastDataUpdate ?? '').localeCompare(
          right.lastDataUpdate ?? '',
        );
      default:
        return 0;
    }
  }
}
