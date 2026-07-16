import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectDetailPageComponent } from './project-detail-page.component';

describe('ProjectDetailPageComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectDetailPageComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should show breadcrumb and project master data from the API (Story 6.2)', () => {
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    const request = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data');
    request.flush({
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Portfolio');
    expect(text).toContain('Nexus Analytics Pilot');
    expect(text).toContain('Acme Fabrications GmbH');
    expect(text).toContain('Mara Neumann');
    expect(text).toContain('Rollout & Betrieb');
    expect(text).toContain('Auf Kurs');
  });
});
