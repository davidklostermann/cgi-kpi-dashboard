import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectIssuesActionsSectionComponent } from './project-issues-actions-section.component';

describe('ProjectIssuesActionsSectionComponent', () => {
  let httpMock: HttpTestingController;
  const projectId = 'a0000000-0000-4000-8000-000000000001';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectIssuesActionsSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render issue cards from the API', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/issues-actions`).flush({
      projectId,
      factsBadge: 'Fakten aus Backend',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [
        {
          id: 'f0000000-0000-4000-8000-000000000010',
          itemType: 'PROBLEM',
          itemTypeLabel: 'Problem',
          category: 'RESSOURCEN',
          title: 'Cloud-Engineering-Kapazität unterdeckt',
          description: 'Bedarf und verfügbare Kapazität weichen ab.',
          severity: 'CRITICAL',
          severityLabel: 'Kritisch',
          metrics: [
            { label: 'Bedarf', value: '3,0 FTE' },
            { label: 'Verfügbar', value: '1,0 FTE' },
            { label: 'Lücke', value: '2,0 FTE' },
            { label: 'Auswirkung', value: '+18 Tage' },
          ],
          owner: 'Projektleitung',
          dueDate: '2026-08-05',
          actionKind: 'COUNTERMEASURE',
          actionLabel: 'Laufende Maßnahme',
          actionText: 'Externe Verstärkung vorbereiten.',
        },
      ],
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Probleme, Risiken & Maßnahmen');
    expect(text).toContain('Fakten aus Backend');
    expect(text).toContain('Cloud-Engineering-Kapazität unterdeckt');
    expect(text).toContain('Kritisch');
    expect(text).toContain('3,0 FTE');
    expect(text).toContain('Laufende Maßnahme');
  });

  it('should show loading state before the response arrives', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Probleme und Risiken werden geladen');
    httpMock.expectOne(`/api/projects/${projectId}/issues-actions`).flush({
      projectId,
      factsBadge: 'Fakten aus Backend',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
  });

  it('should show error state when the API fails', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/issues-actions`).flush(
      { message: 'Backend nicht erreichbar' },
      { status: 500, statusText: 'Server Error' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Backend nicht erreichbar');
  });

  it('should show empty state when no open items exist', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/issues-actions`).flush({
      projectId,
      factsBadge: 'Fakten aus Backend',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Keine offenen Probleme oder Risiken für dieses Projekt.',
    );
  });
});
