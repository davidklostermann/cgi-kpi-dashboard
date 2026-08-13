import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectIsoManagementSectionComponent } from './project-iso-management-section.component';

describe('ProjectIsoManagementSectionComponent', () => {
  let httpMock: HttpTestingController;
  const projectId = 'a0000000-0000-4000-8000-000000000001';
  const emptyProjectId = 'a0000000-0000-4000-8000-000000000006';

  const fullPayload = {
    projectId,
    dataAvailable: true,
    emptyReason: null,
    factsAsOf: '2026-07-15T08:00:00Z',
    benefits: {
      expectedBenefit: 'Reduktion manueller Reporting-Aufwände um 35 %',
      benefitUnit: 'Std./Monat',
      realizedPercent: 68,
      status: 'GREEN',
      statusLabel: 'Auf Kurs',
    },
    scope: {
      scopeStatus: 'Im vereinbarten Scope',
      deviations: ['Integrations-Meilenstein überfällig', 'Zusätzlicher Cutover-Workstream'],
      trend: 'STABLE',
      trendLabel: 'Stabil',
    },
    changeRequests: {
      total: 4,
      open: 1,
      inReview: 1,
      approved: 2,
      impactSchedule: 'LOW',
      impactScheduleLabel: 'Gering',
      impactCost: 'NONE',
      impactCostLabel: 'Keine',
      impactScope: 'LOW',
      impactScopeLabel: 'Gering',
    },
    quality: {
      qualityStatus: 'Qualität stabil',
      openDefects: 6,
      criticalDefects: 0,
      testAcceptanceStatus: 'Systemtest 78 % abgeschlossen',
      progressPercent: 78,
    },
    stakeholders: {
      sponsorCustomer: 'Acme Fabrications GmbH / Dr. Keller',
      stakeholderStatus: 'Engagiert',
      escalationStatus: 'Keine Eskalation',
      lastSteeringDate: '2026-07-10',
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectIsoManagementSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render five compact management cards from the API', () => {
    const fixture = TestBed.createComponent(ProjectIsoManagementSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/iso-management`).flush(fullPayload);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Erweiterte Projektsteuerung');
    expect(text).toContain('Nutzen');
    expect(text).toContain('Scope');
    expect(text).toContain('Changes');
    expect(text).toContain('Qualität');
    expect(text).toContain('Stakeholder');
    expect(text).toContain('Realisierung');
    expect(text).toContain('68');
    expect(text).toContain('Auf Kurs');
    expect(text).toContain('Stabil');
    expect(text).toContain('Offen');
    expect(text).toContain('Prüfung');
    expect(fixture.nativeElement.querySelectorAll('.mgmt-tile').length).toBe(5);
    expect(fixture.nativeElement.querySelectorAll('.mgmt-progress').length).toBe(2);
    expect(text).not.toContain('ISO 21502');
    expect(text).not.toContain('ISO-konform');
    expect(text).not.toContain('Normzertifizierung');
  });

  it('should show loading state before the response arrives', () => {
    const fixture = TestBed.createComponent(ProjectIsoManagementSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Steuerungsdaten werden geladen');
    expect(fixture.nativeElement.textContent).not.toContain('ISO');
    httpMock.expectOne(`/api/projects/${projectId}/iso-management`).flush(fullPayload);
  });

  it('should show error state and allow retry', () => {
    const fixture = TestBed.createComponent(ProjectIsoManagementSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/iso-management`).flush(
      { message: 'Steuerungsdaten nicht verfügbar' },
      { status: 503, statusText: 'Unavailable' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Erweiterte Steuerungsdaten konnten nicht geladen werden.',
    );
    expect(fixture.nativeElement.textContent).not.toContain('Steuerungsdaten nicht verfügbar');

    fixture.nativeElement.querySelector('button')?.dispatchEvent(new Event('click'));
    fixture.detectChanges();
    httpMock.expectOne(`/api/projects/${projectId}/iso-management`).flush(fullPayload);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.mgmt-tile').length).toBe(5);
  });

  it('should show empty state without ISO wording when dataAvailable is false', () => {
    const fixture = TestBed.createComponent(ProjectIsoManagementSectionComponent);
    fixture.componentRef.setInput('projectId', emptyProjectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${emptyProjectId}/iso-management`).flush({
      projectId: emptyProjectId,
      dataAvailable: false,
      emptyReason: 'Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.',
      factsAsOf: '2026-01-15T08:00:00Z',
      benefits: null,
      scope: null,
      changeRequests: null,
      quality: null,
      stakeholders: null,
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Keine erweiterten Steuerungsdaten für dieses Projekt hinterlegt.');
    expect(text).not.toContain('ISO');
    expect(fixture.nativeElement.querySelectorAll('.mgmt-tile').length).toBe(0);
  });

  it('should hide steering date when lastSteeringDate is null', () => {
    const fixture = TestBed.createComponent(ProjectIsoManagementSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/iso-management`).flush({
      ...fullPayload,
      stakeholders: {
        ...fullPayload.stakeholders,
        lastSteeringDate: null,
      },
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).not.toContain('Steering');
  });
});
