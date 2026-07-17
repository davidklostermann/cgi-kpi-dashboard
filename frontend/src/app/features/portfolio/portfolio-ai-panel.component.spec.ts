import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { PortfolioAiPanelComponent } from './portfolio-ai-panel.component';

describe('PortfolioAiPanelComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioAiPanelComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render trend text and top projects', () => {
    const fixture = TestBed.createComponent(PortfolioAiPanelComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush({
      text: 'Drei Projekte benötigen Aufmerksamkeit.',
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
      topProjects: [
        {
          projectId: 'a0000000-0000-4000-8000-000000000001',
          projectName: 'Nexus Analytics Pilot',
          reason: 'Status kritisch',
          evidenceFactIds: ['portfolio.criticalRiskCount'],
        },
      ],
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('KI-Einschätzung');
    expect(text).toContain('Drei Projekte benötigen Aufmerksamkeit.');
    expect(text).toContain('Nexus Analytics Pilot');
    expect(text).toContain('Top-3 Handlungsbedarf');
  });

  it('should show retry on error', () => {
    const fixture = TestBed.createComponent(PortfolioAiPanelComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush(
      { message: 'KI nicht erreichbar' },
      { status: 500, statusText: 'Error' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('KI nicht erreichbar');
  });

  it('should show disabled only for AI_DISABLED', () => {
    const fixture = TestBed.createComponent(PortfolioAiPanelComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush(
      { code: 'AI_DISABLED', message: 'Portfolio-Assistent ist deaktiviert.' },
      { status: 503, statusText: 'Service Unavailable' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('disabled');
    expect(fixture.nativeElement.textContent).toContain('Portfolio-Assistent ist deaktiviert.');
  });

  it('should show provider error for AI_PROVIDER_ERROR', () => {
    const fixture = TestBed.createComponent(PortfolioAiPanelComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush(
      {
        code: 'AI_PROVIDER_ERROR',
        message: 'Gemini-Modell nicht gefunden. APP_AI_MODEL prüfen.',
      },
      { status: 503, statusText: 'Service Unavailable' },
    );
    fixture.detectChanges();

    expect(fixture.componentInstance.status()).toBe('error');
    expect(fixture.nativeElement.textContent).toContain('Gemini-Modell nicht gefunden');
    expect(fixture.nativeElement.textContent).toContain('AI_PROVIDER_ERROR');
  });
});
