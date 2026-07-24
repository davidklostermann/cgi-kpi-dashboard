import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { computed, signal, WritableSignal } from '@angular/core';

import {
  AI_KEY_MISSING_MESSAGE,
  ProjectAiPanelComponent,
} from './project-ai-panel.component';
import { AuthService } from '../../core/auth/auth.service';

describe('ProjectAiPanelComponent', () => {
  let httpMock: HttpTestingController;
  let isAdminSignal: WritableSignal<boolean>;

  const projectId = 'a0000000-0000-4000-8000-000000000001';
  const analysisUrl = `/api/projects/${projectId}/ai/analysis?refresh=false`;
  const analysisRefreshUrl = `/api/projects/${projectId}/ai/analysis?refresh=true`;
  const questionsUrl = `/api/projects/${projectId}/ai/questions`;
  const readinessUrl = '/api/me/ai/readiness';

  const analysis = {
    projectId,
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
    isAdminSignal = signal(true);
    const authServiceMock = {
      currentUser: signal({ id: 'user-a', roles: ['ROLE_ADMIN'] }),
      isAdmin: computed(() => isAdminSignal()),
    };

    await TestBed.configureTestingModule({
      imports: [ProjectAiPanelComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function createPanel(): ReturnType<typeof TestBed.createComponent<ProjectAiPanelComponent>> {
    const fixture = TestBed.createComponent(ProjectAiPanelComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();
    return fixture;
  }

  function flushReadiness(): void {
    httpMock.expectOne(readinessUrl).flush({ ready: true });
  }

  function flushKeyMissing(): void {
    httpMock
      .expectOne(readinessUrl)
      .flush(
        {
          code: 'AI_KEY_MISSING',
          message: AI_KEY_MISSING_MESSAGE,
        },
        { status: 403, statusText: 'Forbidden' },
      );
  }

  it('should require evidence for AI insights and hide technical field names by default', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
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
    const openDetails = fixture.nativeElement.querySelectorAll('details[open]');
    expect(openDetails.length).toBe(0);
    expect(text).toContain('Fehlende Daten');
    expect(text).toContain('KI-generierte Einschätzung.');
  });

  it('should hide empty and generic AI action cards', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();

    fixture.componentInstance.selectTab('actions');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).not.toContain('Steering-Vorbereitung für:');
    expect(text).not.toContain('Leere Maßnahme');
    expect(text).toContain('Externe Verstärkung freigeben');
    expect(text).toContain('Nicht in den Projektdaten hinterlegt');
  });

  it('should prepare editable draft without backend save for concrete actions', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();

    fixture.componentInstance.selectTab('actions');
    fixture.componentInstance.prepareDraft(analysis.suggestedActions[1]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Entwurf (nur lokal, nicht gespeichert)');
    expect(httpMock.match(() => true).length).toBe(0);
  });

  it('should answer chat questions via API', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();

    fixture.componentInstance.selectTab('questions');
    fixture.componentInstance.sendQuestion('Wie ist der aktuelle Fortschritt?');
    fixture.detectChanges();

    httpMock.expectOne(readinessUrl).flush({ ready: true });
    const req = httpMock.expectOne(questionsUrl);
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
    const fixture = createPanel();
    flushReadiness();
    httpMock
      .expectOne(analysisUrl)
      .flush(
        { code: 'AI_DISABLED', message: 'Projekt-Assistent ist deaktiviert.' },
        { status: 503, statusText: 'Service Unavailable' },
      );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('disabled');
    expect(fixture.nativeElement.textContent).toContain('Projekt-Assistent ist deaktiviert.');
  });

  it('should show provider error for AI_PROVIDER_ERROR', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock
      .expectOne(analysisUrl)
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
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();

    fixture.componentInstance.selectTab('questions');
    fixture.componentInstance.sendQuestion('Wie ist der Fortschritt?');
    fixture.detectChanges();

    httpMock.expectOne(readinessUrl).flush({ ready: true });
    httpMock
      .expectOne(questionsUrl)
      .flush(
        { code: 'AI_DISABLED', message: 'Projekt-Assistent ist deaktiviert.' },
        { status: 503, statusText: 'Service Unavailable' },
      );
    fixture.detectChanges();

    expect(fixture.componentInstance.chatStatus()).toBe('disabled');
    expect(fixture.nativeElement.textContent).toContain('Projekt-Assistent ist deaktiviert.');
    expect(fixture.componentInstance.chatInputDisabled()).toBe(true);
    fixture.componentInstance.sendQuestion('Noch eine Frage');
    expect(httpMock.match(questionsUrl).length).toBe(0);
  });

  it('should keep questions tab usable when analysis fails with a generic error', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock
      .expectOne(analysisUrl)
      .flush({ message: 'Analyse ausgefallen' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();

    fixture.componentInstance.selectTab('questions');
    fixture.detectChanges();
    fixture.componentInstance.sendQuestion('Wie ist der Fortschritt?');
    fixture.detectChanges();

    httpMock.expectOne(readinessUrl).flush({ ready: true });
    const req = httpMock.expectOne(questionsUrl);
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

  it('should show only the key-missing notice for ADMIN without API key', () => {
    isAdminSignal.set(true);
    const fixture = createPanel();
    flushKeyMissing();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(text).toContain(AI_KEY_MISSING_MESSAGE);
    expect(text).toContain('KI-Einstellungen öffnen');
    expect(fixture.nativeElement.querySelector('.project-ai-panel__tabs')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Managementbewertung');
    expect(fixture.nativeElement.textContent).not.toContain('Verfügbarkeit wird geprüft');
    httpMock.expectNone(analysisUrl);
  });

  it('should show key-missing notice without admin link for USER without API key', () => {
    isAdminSignal.set(false);
    const fixture = createPanel();
    flushKeyMissing();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain(AI_KEY_MISSING_MESSAGE);
    expect(text).not.toContain('KI-Einstellungen öffnen');
    expect(fixture.nativeElement.querySelector('a[routerLink="/admin/ai-config"]')).toBeNull();
    httpMock.expectNone(analysisUrl);
  });

  it('should not call analysis when readiness returns ready=false', () => {
    const fixture = createPanel();
    httpMock.expectOne(readinessUrl).flush({ ready: false });
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.componentInstance.analysis()).toBeNull();
    httpMock.expectNone(analysisUrl);
  });

  it('should not call analysis endpoint when readiness reports missing key', () => {
    const fixture = createPanel();
    flushKeyMissing();
    fixture.detectChanges();

    fixture.componentInstance.loadAnalysis(true);
    fixture.detectChanges();

    httpMock.expectNone(analysisRefreshUrl);
    expect(fixture.componentInstance.analysis()).toBeNull();
  });

  it('should clear displayed AI content when key is removed after successful analysis', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Managementbewertung');

    fixture.componentInstance.loadAnalysis(true);
    fixture.detectChanges();

    httpMock.expectOne(readinessUrl).flush(
      { code: 'AI_KEY_MISSING', message: AI_KEY_MISSING_MESSAGE },
      { status: 403, statusText: 'Forbidden' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.componentInstance.analysis()).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Managementbewertung');
    httpMock.expectNone(analysisRefreshUrl);
  });

  it('should clear AI content when the authenticated user changes', () => {
    const authServiceMock = TestBed.inject(AuthService) as unknown as {
      currentUser: WritableSignal<{ id: string; roles: string[] } | null>;
    };

    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Managementbewertung');

    authServiceMock.currentUser.set({ id: 'user-b', roles: ['ROLE_ADMIN'] });
    fixture.detectChanges();

    flushKeyMissing();
    fixture.detectChanges();

    expect(fixture.componentInstance.analysis()).toBeNull();
    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.nativeElement.textContent).not.toContain('Managementbewertung');
    httpMock.expectNone(analysisUrl);
  });

  it('should work unchanged with a valid own API key', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(analysisUrl).flush(analysis);
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('success');
    expect(fixture.nativeElement.textContent).toContain('Managementbewertung');
    expect(fixture.nativeElement.querySelector('.project-ai-panel__tabs')).toBeTruthy();
  });
});
