import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectInsightsSectionComponent } from './project-insights-section.component';

describe('ProjectInsightsSectionComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectInsightsSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render deterministic insight list in facts area (Story 6.6)', () => {
    const fixture = TestBed.createComponent(ProjectInsightsSectionComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/insights').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      insights: [
        {
          code: 'PROGRESS_BEHIND_TIME',
          statement: 'Fortschritt liegt hinter dem erwarteten Zeitverbrauch.',
          metrics: 'Fortschritt vs. Zeitverbrauch',
          comparisonValue: 'Schwelle −20 Prozentpunkte',
          period: 'Aktueller Berichtsstand vs. Plan',
          rationale: 'Der Ist-Fortschritt liegt unter dem erwarteten Fortschritt.',
          type: 'deterministisch',
        },
      ],
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Management Insights');
    expect(text).toContain('deterministisch');
    expect(text).toContain('Fortschritt liegt hinter dem erwarteten Zeitverbrauch.');
  });
});
