import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectAiPanelComponent } from './project-ai-panel.component';

describe('ProjectAiPanelComponent', () => {
  let httpMock: HttpTestingController;

  const analysis = {
    projectId: 'a0000000-0000-4000-8000-000000000001',
    factsAsOf: '2026-07-01T08:00:00Z',
    generatedAt: '2026-07-16T12:00:00Z',
    status: 'SUCCESS',
    availableSources: ['KPI', 'BUDGET'],
    summary: 'Zusammenfassung aus freigegebenen Fakten.',
    priorities: [
      {
        rank: 1,
        title: 'Terminabweichung adressieren',
        reason: 'Abweichung belegt.',
        evidenceFactIds: ['kpi.scheduleDeviationDays'],
      },
    ],
    suggestedActions: [
      {
        title: 'Steering vorbereiten',
        reason: 'Entscheidungsgrundlage schaffen.',
        suggestedOwner: 'Projektleitung',
        suggestedDueDate: null,
        addressesType: 'KPI',
        addressesId: 'kpi.scheduleDeviationDays',
        expectedEffect: 'Transparenz',
        evidenceFactIds: ['kpi.scheduleDeviationDays'],
        isProposal: true,
      },
    ],
    missingData: [{ area: 'QUALITY', description: 'Testdaten fehlen.' }],
    aiGenerated: true,
    disclaimer: 'KI-generierte Einschätzung.',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectAiPanelComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render summary, priorities and evidence chips (Story 9.4)', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(analysis);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Projekt-Assistent');
    expect(text).toContain('Zusammenfassung aus freigegebenen Fakten.');
    expect(text).toContain('Terminabweichung adressieren');
    expect(text).toContain('kpi.scheduleDeviationDays');
    expect(text).toContain('Fehlende Daten');
  });

  it('should switch tabs and prepare editable draft without backend save', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();
    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(analysis);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.selectTab('actions');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('KI-Vorschlag');

    component.prepareDraft(analysis.suggestedActions[0]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Entwurf (nur lokal, nicht gespeichert)');
    expect(httpMock.match(() => true).length).toBe(0);
  });

  it('should answer chat questions via API', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();
    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(analysis);
    fixture.detectChanges();

    fixture.componentInstance.selectTab('questions');
    fixture.componentInstance.sendQuestion('Wie ist der aktuelle Fortschritt?');
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/questions');
    expect(req.request.body).toEqual({ question: 'Wie ist der aktuelle Fortschritt?' });
    req.flush({
      answer: 'Laut freigegebenen Projektdaten: Fortschritt = 62 %.',
      evidenceFactIds: ['kpi.progressPercent'],
      factsAsOf: '2026-07-01T08:00:00Z',
      generatedAt: '2026-07-16T12:00:00Z',
      insufficientEvidence: false,
      aiGenerated: true,
      disclaimer: 'KI-generierte Einschätzung.',
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Fortschritt = 62 %');
  });

  it('should show disabled message only for AI_DISABLED', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(
        { code: 'AI_DISABLED', message: 'Projekt-Assistent ist deaktiviert.' },
        { status: 503, statusText: 'Service Unavailable' },
      );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('disabled');
    expect(fixture.nativeElement.textContent).toContain('Projekt-Assistent ist deaktiviert.');
  });

  it('should show provider error for AI_PROVIDER_ERROR', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(
        {
          code: 'AI_PROVIDER_ERROR',
          message: 'Gemini-Authentifizierung fehlgeschlagen. API-Key und Berechtigungen prüfen.',
        },
        { status: 503, statusText: 'Service Unavailable' },
      );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('error');
    expect(fixture.nativeElement.textContent).toContain('Gemini-Authentifizierung fehlgeschlagen');
    expect(fixture.nativeElement.textContent).toContain('AI_PROVIDER_ERROR');
  });
});
