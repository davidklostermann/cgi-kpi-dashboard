import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, RouterLink } from '@angular/router';
import { computed, signal, WritableSignal } from '@angular/core';

import { PortfolioAiPanelComponent } from './portfolio-ai-panel.component';
import { PortfolioFilterService } from './portfolio-filter.service';
import { AuthService } from '../../core/auth/auth.service';
import { AI_KEY_MISSING_MESSAGE } from '../project/project-ai-panel.component';

describe('PortfolioAiPanelComponent', () => {
  let httpMock: HttpTestingController;
  let filterService: PortfolioFilterService;
  let isAdminSignal: WritableSignal<boolean>;

  const readinessUrl = '/api/me/ai/readiness';
  const trendUrl = '/api/portfolio/ai/trend-analysis';

  const insightPayload = {
    insights: [
      {
        id: 'deteriorating-1',
        type: 'DETERIORATING_TREND',
        title: 'Mehrere Projekte verschlechtern sich',
        finding: 'Terminabweichung steigt in mehreren Projekten.',
        managementImplication: 'Gebündelte Steuerung erforderlich.',
        recommendedAction: 'Steering ansetzen.',
        affectedProjectIds: [
          'a0000000-0000-4000-8000-000000000001',
          'a0000000-0000-4000-8000-000000000002',
        ],
        affectedProjectNames: ['Nexus Analytics Pilot', 'Atlas'],
        evidence: [
          {
            label: 'Nexus Terminabweichung',
            value: '10 → 14 Tage',
            projectId: 'a0000000-0000-4000-8000-000000000001',
            reportDate: '2026-07-01',
          },
          {
            label: 'Atlas Terminabweichung',
            value: '8 → 12 Tage',
            projectId: 'a0000000-0000-4000-8000-000000000002',
            reportDate: '2026-07-01T12:00:00Z',
          },
        ],
        confidence: 'HIGH',
        dataQuality: 'COMPLETE',
        detectedAt: '2026-07-16T12:00:00Z',
      },
      {
        id: 'single-reject',
        type: 'DETERIORATING_TREND',
        title: 'Einzelprojekt ohne Portfoliobezug',
        finding: 'Nur ein Projekt',
        managementImplication: 'n/a',
        affectedProjectIds: ['a0000000-0000-4000-8000-000000000001'],
        affectedProjectNames: ['Nexus'],
        evidence: [
          { label: 'A', value: '1' },
          { label: 'B', value: '2' },
        ],
        confidence: 'LOW',
        dataQuality: 'INSUFFICIENT',
        detectedAt: '2026-07-16T12:00:00Z',
      },
    ],
    aiGenerated: true,
    disclaimer: 'Disclaimer',
    generatedAt: '2026-07-16T12:00:00Z',
  };

  beforeEach(async () => {
    isAdminSignal = signal(true);
    const authServiceMock = {
      currentUser: signal({ id: 'user-a', roles: ['ROLE_ADMIN'] }),
      isAdmin: computed(() => isAdminSignal()),
    };

    await TestBed.configureTestingModule({
      imports: [PortfolioAiPanelComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
    filterService = TestBed.inject(PortfolioFilterService);
    filterService.reset();
  });

  afterEach(() => httpMock.verify());

  function createPanel(): ReturnType<typeof TestBed.createComponent<PortfolioAiPanelComponent>> {
    const fixture = TestBed.createComponent(PortfolioAiPanelComponent);
    fixture.detectChanges();
    return fixture;
  }

  function flushReadiness(): void {
    httpMock.expectOne(readinessUrl).flush({ ready: true });
  }

  function flushKeyMissing(): void {
    httpMock.expectOne(readinessUrl).flush(
      { code: 'AI_KEY_MISSING', message: AI_KEY_MISSING_MESSAGE },
      { status: 403, statusText: 'Forbidden' },
    );
  }

  it('should render portfolio pattern cards and hide single-project insights', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(insightPayload);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Portfolio-Muster und systemische Risiken');
    expect(text).toContain('Gemini');
    expect(text).toContain('Mehrere Projekte verschlechtern sich');
    expect(text).toContain('Nexus Analytics Pilot');
    expect(text).toContain('Belege anzeigen');
    expect(text).toContain('Konfidenz Hoch');
    expect(text).toContain('Datenqualität Vollständig');
    expect(text).not.toContain('Einzelprojekt ohne Portfoliobezug');
    expect(text).not.toContain('KI-Einschätzung');
    expect(text).not.toContain('Top-3 Handlungsbedarf');
  });

  it('should link affected projects to detail routes', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(insightPayload);
    fixture.detectChanges();

    const links = fixture.debugElement.queryAll(By.directive(RouterLink));
    const hrefs = links.map((link) => link.injector.get(RouterLink).href);
    expect(hrefs).toContain('/projects/a0000000-0000-4000-8000-000000000001');
    expect(hrefs).toContain('/projects/a0000000-0000-4000-8000-000000000002');
  });

  it('should show defined empty state when no insights exist', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush({
      insights: [],
      aiGenerated: false,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Regelbasiert');
    expect(text).toContain(
      'Für den gewählten Berichtsstand wurden keine belastbaren projektübergreifenden Muster erkannt.',
    );
  });

  it('should distinguish filtered empty state when raw insights exist but none are displayable', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush({
      insights: [
        {
          id: 'inactive-type',
          type: 'CAPACITY_CONFLICT',
          title: 'Kapazitätskonflikt',
          finding: 'Mehrere Teams überlastet',
          managementImplication: 'Priorisieren',
          affectedProjectIds: [
            'a0000000-0000-4000-8000-000000000001',
            'a0000000-0000-4000-8000-000000000002',
          ],
          affectedProjectNames: ['Nexus', 'Atlas'],
          evidence: [
            { label: 'A', value: '1' },
            { label: 'B', value: '2' },
          ],
          confidence: 'MEDIUM',
          dataQuality: 'PARTIAL',
          detectedAt: '2026-07-16T12:00:00Z',
        },
      ],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Es liegen Muster vor, die für die Anzeige nicht ausreichend belegt sind.',
    );
  });

  it('should not render project links for drifted id/name arrays', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush({
      insights: [
        {
          id: 'drift-1',
          type: 'REPORTING_PATTERN',
          title: 'Berichtsmuster',
          finding: 'Risiken steigen',
          managementImplication: 'Prüfen',
          affectedProjectIds: ['a0000000-0000-4000-8000-000000000001', ''],
          affectedProjectNames: ['Nexus', 'Atlas', 'Extra'],
          evidence: [
            { label: 'A', value: '1' },
            { label: 'B', value: '2' },
          ],
          confidence: 'MEDIUM',
          dataQuality: 'PARTIAL',
          detectedAt: '2026-07-16T12:00:00Z',
        },
      ],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    const links = fixture.debugElement.queryAll(By.directive(RouterLink));
    const hrefs = links.map((link) => link.injector.get(RouterLink).href);
    expect(hrefs).toEqual(['/projects/a0000000-0000-4000-8000-000000000001']);
    expect(fixture.nativeElement.textContent).not.toContain('undefined');
  });

  it('should show non-blocking error message and never Failed to fetch', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(
      { message: 'KI nicht erreichbar' },
      { status: 500, statusText: 'Error' },
    );
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain(
      'Die Portfolio-Musteranalyse ist derzeit nicht verfügbar. KPIs und Projektdaten bleiben uneingeschränkt nutzbar.',
    );
    expect(text).not.toContain('Failed to fetch');
  });

  it('should hide provider raw text for AI_PROVIDER_ERROR', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(
      {
        code: 'AI_PROVIDER_ERROR',
        message: 'Gemini-Authentifizierung fehlgeschlagen. API-Key und Berechtigungen prüfen.',
      },
      { status: 503, statusText: 'Service Unavailable' },
    );
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain(
      'Die Portfolio-Musteranalyse ist derzeit nicht verfügbar. KPIs und Projektdaten bleiben uneingeschränkt nutzbar.',
    );
    expect(text).not.toContain('Gemini-Authentifizierung fehlgeschlagen');
    expect(text).toContain('Erneut versuchen');
  });

  it('should show disabled only for AI_DISABLED without retry', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(
      { code: 'AI_DISABLED', message: 'Portfolio-Assistent ist deaktiviert.' },
      { status: 503, statusText: 'Service Unavailable' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('disabled');
    expect(fixture.nativeElement.textContent).toContain('Portfolio-Assistent ist deaktiviert.');
    expect(fixture.nativeElement.textContent).not.toContain('Erneut versuchen');
  });

  it('should treat null HTTP body as error', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(null);
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('error');
    expect(fixture.nativeElement.textContent).toContain(
      'Die Portfolio-Musteranalyse ist derzeit nicht verfügbar. KPIs und Projektdaten bleiben uneingeschränkt nutzbar.',
    );
  });

  it('should pass active portfolio filters to the API and reload on filter change', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush({
      insights: [],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    filterService.update({ customer: 'Acme GmbH' });
    fixture.detectChanges();

    flushReadiness();
    const req = httpMock.expectOne(
      (request) =>
        request.url === trendUrl && request.params.get('customer') === 'Acme GmbH',
    );
    req.flush({
      insights: [],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Für den gewählten Berichtsstand wurden keine belastbaren projektübergreifenden Muster erkannt.',
    );
  });

  it('should show only the key-missing notice for ADMIN without API key', () => {
    isAdminSignal.set(true);
    const fixture = createPanel();
    flushKeyMissing();
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.componentInstance.analysis()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain(AI_KEY_MISSING_MESSAGE);
    expect(fixture.nativeElement.textContent).toContain('KI-Einstellungen öffnen');
    expect(fixture.nativeElement.textContent).not.toContain('Mehrere Projekte verschlechtern sich');
    httpMock.expectNone(trendUrl);
  });

  it('should not call trend analysis when readiness returns ready=false', () => {
    const fixture = createPanel();
    httpMock.expectOne(readinessUrl).flush({ ready: false });
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.componentInstance.analysis()).toBeNull();
    httpMock.expectNone(trendUrl);
  });

  it('should not call trend analysis endpoint when readiness reports missing key', () => {
    const fixture = createPanel();
    flushKeyMissing();
    fixture.detectChanges();

    fixture.componentInstance.load();
    fixture.detectChanges();

    httpMock.expectNone(trendUrl);
    expect(fixture.componentInstance.analysis()).toBeNull();
  });

  it('should clear displayed AI content when key is removed after successful analysis', () => {
    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(insightPayload);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Mehrere Projekte verschlechtern sich');

    fixture.componentInstance.load();
    fixture.detectChanges();

    httpMock.expectOne(readinessUrl).flush(
      { code: 'AI_KEY_MISSING', message: AI_KEY_MISSING_MESSAGE },
      { status: 403, statusText: 'Forbidden' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.componentInstance.analysis()).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Mehrere Projekte verschlechtern sich');
    httpMock.expectNone(trendUrl);
  });

  it('should clear AI content when the authenticated user changes', () => {
    const authServiceMock = TestBed.inject(AuthService) as unknown as {
      currentUser: WritableSignal<{ id: string; roles: string[] } | null>;
    };

    const fixture = createPanel();
    flushReadiness();
    httpMock.expectOne(trendUrl).flush(insightPayload);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Mehrere Projekte verschlechtern sich');

    authServiceMock.currentUser.set({ id: 'user-b', roles: ['ROLE_ADMIN'] });
    fixture.detectChanges();

    flushKeyMissing();
    fixture.detectChanges();

    expect(fixture.componentInstance.analysis()).toBeNull();
    expect(fixture.componentInstance.status()).toBe('key_missing');
    expect(fixture.nativeElement.textContent).not.toContain('Mehrere Projekte verschlechtern sich');
    httpMock.expectNone(trendUrl);
  });
});
