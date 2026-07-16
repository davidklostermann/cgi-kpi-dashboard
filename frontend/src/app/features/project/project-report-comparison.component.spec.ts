import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectReportComparisonComponent } from './project-report-comparison.component';

describe('ProjectReportComparisonComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectReportComparisonComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render deltas when comparison is available (Story 6.7)', () => {
    const fixture = TestBed.createComponent(ProjectReportComparisonComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      comparisonAvailable: true,
      unavailableReason: null,
      previousSnapshotDate: '2026-06-01',
      currentSnapshotDate: '2026-07-01',
      progressDeltaPercent: 5,
      budgetActualDelta: 12000,
      scheduleDeviationDeltaDays: 0,
      previousStatus: 'ON_TRACK',
      previousStatusLabel: 'Auf Kurs',
      currentStatus: 'ON_TRACK',
      currentStatusLabel: 'Auf Kurs',
      openRiskCountDelta: -1,
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Berichtsstandsvergleich');
    expect(text).toContain('2026-06-01');
    expect(text).toContain('+5 %');
  });

  it('should show defined hint when previous snapshot is missing (Story 6.7)', () => {
    const fixture = TestBed.createComponent(ProjectReportComparisonComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      comparisonAvailable: false,
      unavailableReason: 'Kein vorheriger Berichtsstand vorhanden — Vergleich nicht möglich.',
      previousSnapshotDate: null,
      currentSnapshotDate: null,
      progressDeltaPercent: null,
      budgetActualDelta: null,
      scheduleDeviationDeltaDays: null,
      previousStatus: null,
      previousStatusLabel: null,
      currentStatus: null,
      currentStatusLabel: null,
      openRiskCountDelta: null,
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Kein vorheriger Berichtsstand vorhanden — Vergleich nicht möglich.',
    );
  });
});
