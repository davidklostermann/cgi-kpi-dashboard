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
    summary: 'Warum auffällig: Terminabweichung. Auswirkung: Lieferdruck. Nötige Entscheidung: Gegensteuerung freigeben.',
    priorities: [
      {
        rank: 1,
        title: 'Terminabweichung adressieren',
        managementImplication: 'Liefertermin gerät unter Druck.',
        requiredDecision: 'Gegensteuerung im Steering freigeben.',
        evidence: [
          { label: 'Terminabweichung', value: '14 Tage', sourceField: 'kpi.scheduleDeviationDays' },
          { label: 'Fortschritt', value: '62 %', sourceField: 'kpi.progressPercent' },
        ],
        evidenceFactIds: ['kpi.scheduleDeviationDays', 'kpi.progressPercent'],
      },
      {
        rank: 2,
        title: 'Ohne Belege — darf nicht erscheinen',
        managementImplication: 'n/a',
        requiredDecision: 'n/a',
        evidence: [{ label: 'Nur einer', value: '1', sourceField: 'kpi.progressPercent' }],
        evidenceFactIds: ['kpi.progressPercent'],
      },
    ],
    suggestedActions: [
      {
        title: 'Steering-Vorbereitung für: Terminabweichung',
        reason: 'Generische Liste.',
        suggestedOwner: 'Projektleitung',
        suggestedDueDate: null,
        addressesType: 'KPI',
        addressesId: 'kpi.scheduleDeviationDays',
        expectedEffect: 'Transparenz',
        evidenceFactIds: ['kpi.scheduleDeviationDays'],
        isProposal: true,
      },
      {
        title: 'Externe Verstärkung freigeben',
        reason: 'Kapazitätslücke belegt den Terminverzug.',
        suggestedOwner: 'Steering Committee',
        suggestedDueDate: null,
        addressesType: 'PROBLEM',
        addressesId: null,
        expectedEffect: 'Terminabweichung reduzieren',
        evidenceFactIds: ['kpi.scheduleDeviationDays', 'kpi.progressPercent'],
        isProposal: true,
      },
      {
        title: 'Leere Maßnahme',
        reason: '',
        suggestedOwner: null,
        suggestedDueDate: null,
        addressesType: null,
        addressesId: null,
        expectedEffect: null,
        evidenceFactIds: [],
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

  it('should require evidence for AI insights and hide technical field names by default', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(analysis);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Projekt-Assistent');
    expect(text).toContain('Managementbewertung');
    expect(text).toContain('Terminabweichung adressieren');
    expect(text).toContain('Terminabweichung: 14 Tage');
    expect(text).toContain('Fortschritt: 62 %');
    expect(text).toContain('Wichtigste Auswirkung');
    expect(text).toContain('Nötige Entscheidung oder Handlung');
    expect(text).toContain('Technische Feldnamen');
    expect(text).not.toContain('Ohne Belege — darf nicht erscheinen');
    // Technische IDs sind nur im zugeklappten details — nicht prominent im Überblickstext
    const openDetails = fixture.nativeElement.querySelectorAll('details[open]');
    expect(openDetails.length).toBe(0);
    expect(text).toContain('Fehlende Daten');
    expect(text).toContain('KI-generierte Einschätzung.');
  });

  it('should hide empty and generic AI action cards', () => {
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

    const text = fixture.nativeElement.textContent as string;
    expect(text).not.toContain('Steering-Vorbereitung für:');
    expect(text).not.toContain('Leere Maßnahme');
    expect(text).toContain('Externe Verstärkung freigeben');
    expect(text).toContain('Nicht in den Projektdaten hinterlegt');
  });

  it('should prepare editable draft without backend save for concrete actions', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();
    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(analysis);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.selectTab('actions');
    component.prepareDraft(analysis.suggestedActions[1]);
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

  it('should show disabled chat message for AI_DISABLED', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();
    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush(analysis);
    fixture.detectChanges();

    fixture.componentInstance.selectTab('questions');
    fixture.componentInstance.sendQuestion('Wie ist der Fortschritt?');
    fixture.detectChanges();

    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/questions')
      .flush(
        { code: 'AI_DISABLED', message: 'Projekt-Assistent ist deaktiviert.' },
        { status: 503, statusText: 'Service Unavailable' },
      );
    fixture.detectChanges();

    expect(fixture.componentInstance.chatStatus()).toBe('disabled');
    expect(fixture.nativeElement.textContent).toContain('Projekt-Assistent ist deaktiviert.');
    expect(fixture.componentInstance.chatInputDisabled()).toBe(true);
    fixture.componentInstance.sendQuestion('Noch eine Frage');
    expect(httpMock.match('/api/projects/a0000000-0000-4000-8000-000000000001/ai/questions').length).toBe(0);
  });

  it('should keep questions tab usable when analysis fails', () => {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush({ message: 'Analyse ausgefallen' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();

    fixture.componentInstance.selectTab('questions');
    fixture.detectChanges();
    fixture.componentInstance.sendQuestion('Wie ist der Fortschritt?');
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/questions');
    req.flush({
      answer: 'Fortschritt laut Daten 62 %.',
      evidenceFactIds: ['kpi.progressPercent'],
      factsAsOf: '2026-07-01T08:00:00Z',
      generatedAt: '2026-07-16T12:00:00Z',
      insufficientEvidence: true,
      aiGenerated: true,
      disclaimer: 'KI-generierte Einschätzung.',
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Fortschritt laut Daten 62 %.');
    expect(text).toContain('reichen für eine belastbare Antwort nicht aus');
  });
});
