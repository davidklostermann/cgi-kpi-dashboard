import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { ProjectTableComponent } from './project-table.component';
import { PortfolioTableRow } from '../models/portfolio-table.model';

const mockRows: PortfolioTableRow[] = [
  {
    id: 'a0000000-0000-4000-8000-000000000001',
    name: 'Alpha Project',
    customerName: 'Acme GmbH',
    projectLead: 'Anna Keller',
    status: 'ON_TRACK',
    statusLabel: 'Auf Kurs',
    currentPhaseName: 'Umsetzung',
    progressPercent: 62,
    plannedEndDate: '2026-06-30',
    forecastEndDate: null,
    scheduleDeviationDays: 0,
    budgetUtilizationPercent: 95.0,
    budgetDeviationPercent: -5.0,
    effortDeviationPercent: 2.0,
    openRiskCount: 1,
    criticalIssueCount: 0,
    lastDataUpdate: '2026-07-10T08:00:00Z',
  },
  {
    id: 'a0000000-0000-4000-8000-000000000002',
    name: 'Beta Project',
    customerName: 'Beta AG',
    projectLead: 'Markus Brenner',
    status: 'CRITICAL',
    statusLabel: 'Kritisch',
    currentPhaseName: 'Analyse',
    progressPercent: 40,
    plannedEndDate: '2026-05-31',
    forecastEndDate: '2026-07-01',
    scheduleDeviationDays: 21,
    budgetUtilizationPercent: 110.0,
    budgetDeviationPercent: 10.0,
    effortDeviationPercent: 8.0,
    openRiskCount: 2,
    criticalIssueCount: 3,
    lastDataUpdate: '2026-07-12T08:00:00Z',
  },
];

describe('ProjectTableComponent', () => {
  let fixture: ComponentFixture<ProjectTableComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectTableComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(ProjectTableComponent);
    fixture.componentRef.setInput('rows', mockRows);
    fixture.detectChanges();
  });

  it('should render management table columns (Story 5.3)', () => {
    const headers = fixture.nativeElement.querySelectorAll('thead th');
    expect(headers.length).toBeGreaterThanOrEqual(10);
    expect(fixture.nativeElement.textContent).toContain('Alpha Project');
    expect(fixture.nativeElement.textContent).toContain('Budgetabw.');
  });

  it('should sort by schedule deviation when header clicked (Story 5.3)', () => {
    const scheduleHeader = Array.from(
      fixture.nativeElement.querySelectorAll('.project-table__sort') as NodeListOf<HTMLButtonElement>,
    ).find((button) => button.textContent?.includes('Terminabw.'));

    scheduleHeader?.click();
    fixture.detectChanges();

    const firstRowName = fixture.nativeElement.querySelector('tbody th')?.textContent?.trim();
    expect(firstRowName).toBe('Alpha Project');
  });

  it('should navigate to project detail on row click (Story 5.5)', () => {
    const rows = fixture.nativeElement.querySelectorAll('.project-table__row') as NodeListOf<HTMLTableRowElement>;
    const alphaRow = Array.from(rows).find((row) => row.textContent?.includes('Alpha Project'));
    alphaRow?.click();
    expect(router.navigate).toHaveBeenCalledWith([
      '/projects',
      'a0000000-0000-4000-8000-000000000001',
    ]);
  });

  it('should navigate to project detail on Enter key (Story 5.5)', () => {
    const rows = fixture.nativeElement.querySelectorAll('.project-table__row') as NodeListOf<HTMLTableRowElement>;
    const alphaRow = Array.from(rows).find((row) => row.textContent?.includes('Alpha Project')) as HTMLTableRowElement;

    alphaRow.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, cancelable: true }),
    );

    expect(router.navigate).toHaveBeenCalledWith([
      '/projects',
      'a0000000-0000-4000-8000-000000000001',
    ]);
  });

  it('should expose keyboard-focusable row links (Story 5.5)', () => {
    const rows = fixture.nativeElement.querySelectorAll('.project-table__row') as NodeListOf<HTMLTableRowElement>;
    const alphaRow = Array.from(rows).find((row) => row.textContent?.includes('Alpha Project')) as HTMLTableRowElement;

    expect(alphaRow.getAttribute('role')).toBe('link');
    expect(alphaRow.getAttribute('tabindex')).toBe('0');
    expect(alphaRow.getAttribute('aria-label')).toBe('Projektdetails für Alpha Project');
  });

  it('should expose horizontal scroll region (Story 5.3)', () => {
    const scrollRegion = fixture.nativeElement.querySelector('.project-table__scroll') as HTMLElement;
    expect(scrollRegion).toBeTruthy();
    const styles = getComputedStyle(scrollRegion);
    expect(styles.overflow === 'auto' || styles.overflowX === 'auto').toBe(true);
  });
});
