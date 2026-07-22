import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectTeamCapacitySectionComponent } from './project-team-capacity-section.component';

describe('ProjectTeamCapacitySectionComponent', () => {
  let httpMock: HttpTestingController;
  const projectId = 'a0000000-0000-4000-8000-000000000001';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectTeamCapacitySectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render roles and capacity summary from the API', () => {
    const fixture = TestBed.createComponent(ProjectTeamCapacitySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/capacity`).flush({
      projectId,
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [
        {
          id: 'a1000000-0000-4000-8000-000000000001',
          roleName: 'Cloud Engineering',
          requiredFte: 3,
          availableFte: 1,
          coveragePercent: 33,
        },
        {
          id: 'a1000000-0000-4000-8000-000000000004',
          roleName: 'Change & Adoption',
          requiredFte: 1,
          availableFte: 1,
          coveragePercent: 100,
        },
      ],
      summary: {
        missingFte: 2,
        nextAvailabilityDate: '2026-08-05',
        overloadedRoles: 1,
        externalOptions: 2,
        impactHeadline: 'Kapazitätslücke mit Terminwirkung',
        impactDetail: 'Unterdeckung im Cloud Engineering.',
      },
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Team & Kapazität');
    expect(text).toContain('Cloud Engineering');
    expect(text).toContain('33');
    expect(text).toContain('Kapazitätslücke mit Terminwirkung');
    expect(text).toContain('2 FTE');
    expect(text).not.toContain('Krankheit');
  });

  it('should show loading state before the response arrives', () => {
    const fixture = TestBed.createComponent(ProjectTeamCapacitySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Kapazitätsdaten werden geladen');
    httpMock.expectOne(`/api/projects/${projectId}/capacity`).flush({
      projectId,
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });
  });

  it('should show error state when the API fails', () => {
    const fixture = TestBed.createComponent(ProjectTeamCapacitySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/capacity`).flush(
      { message: 'Kapazität nicht verfügbar' },
      { status: 503, statusText: 'Unavailable' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Kapazität nicht verfügbar');
  });

  it('should show empty state when no role capacity rows exist', () => {
    const fixture = TestBed.createComponent(ProjectTeamCapacitySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/capacity`).flush({
      projectId,
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Team & Kapazität');
    expect(text).toContain('Keine Kapazitätsdaten für dieses Projekt hinterlegt.');
  });
});
