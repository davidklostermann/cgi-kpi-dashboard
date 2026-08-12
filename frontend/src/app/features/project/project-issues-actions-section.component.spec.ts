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

  function flushItems(items: unknown[]): void {
    httpMock.expectOne(`/api/projects/${projectId}/issues-actions`).flush({
      projectId,
      factsBadge: 'Fakten aus Backend',
      factsAsOf: '2026-07-01T08:00:00Z',
      items,
    });
  }

  it('should render issue cards from the API', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    flushItems([
      {
        id: 'f0000000-0000-4000-8000-000000000010',
        itemType: 'PROBLEM',
        itemTypeLabel: 'Problem',
        category: 'RESSOURCEN',
        title: 'Cloud-Engineering-Kapazität unterdeckt',
        cause: 'Bedarf und verfügbare Kapazität weichen ab.',
        impact: 'Auswirkung: +18 Tage',
        severity: 'CRITICAL',
        severityLabel: 'Kritisch',
        priority: 'Kritisch',
        metrics: [
          { label: 'Bedarf', value: '3,0 FTE' },
          { label: 'Verfügbar', value: '1,0 FTE' },
          { label: 'Lücke', value: '2,0 FTE' },
          { label: 'Auswirkung', value: '+18 Tage' },
        ],
        owner: 'Projektleitung',
        dueDate: '2026-06-24',
        overdueDays: 7,
        overdueLabel: 'Überfällig seit 7 Tagen',
        nextAction: 'Externe Verstärkung vorbereiten und Steering-Entscheidung einholen.',
        escalationNeeded: true,
        actionKind: 'COUNTERMEASURE',
        actionLabel: 'Laufende Maßnahme',
        actionText: 'Externe Verstärkung vorbereiten und Steering-Entscheidung einholen.',
        requiredDecision: {
          decideWho: 'Projektleitung',
          decideBy: '2026-06-24',
          impactIfDeferred: 'Bei Nichtentscheidung bleibt die Auswirkung bestehen: Auswirkung: +18 Tage',
        },
      },
    ]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Probleme, Risiken & Maßnahmen');
    expect(text).toContain('Fakten aus Backend');
    expect(text).toContain('Cloud-Engineering-Kapazität unterdeckt');
    expect(text).toContain('Kritisch');
    expect(text).toContain('Überfällig seit 7 Tagen');
    expect(text).toContain('Benötigte Entscheidung');
    expect(text).toContain('Nächste Aktion');
    expect(text).toContain('3,0 FTE');
    expect(text).toContain('Lücke');
  });

  it('should show decision block when escalation is required', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    flushItems([
      {
        id: 'f0000000-0000-4000-8000-000000000011',
        itemType: 'PROBLEM',
        itemTypeLabel: 'Problem',
        category: 'TERMIN',
        title: 'Meilenstein gefährdet',
        cause: 'Abhängigkeit offen.',
        impact: null,
        severity: 'CRITICAL',
        severityLabel: 'Kritisch',
        priority: 'Kritisch',
        metrics: [],
        owner: null,
        dueDate: null,
        overdueDays: null,
        overdueLabel: null,
        nextAction: null,
        escalationNeeded: true,
        actionKind: 'COUNTERMEASURE',
        actionLabel: 'Laufende Maßnahme',
        actionText: null,
        requiredDecision: null,
      },
    ]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Benötigte Entscheidung');
    expect(text).toContain('Meilenstein gefährdet');
    expect(text).toContain('Entscheidung');
    expect(text).toContain('Entscheider / Rolle');
    expect(text).toContain('nicht hinterlegt');
  });

  it('should hide decision block when escalation is not required', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    flushItems([
      {
        id: 'f0000000-0000-4000-8000-000000000012',
        itemType: 'RISK',
        itemTypeLabel: 'Risiko',
        category: 'QUALITY',
        title: 'Testabdeckung beobachten',
        cause: 'Leichte Abweichung.',
        impact: 'Gering',
        severity: 'MEDIUM',
        severityLabel: 'Mittel',
        priority: 'Mittel',
        metrics: [],
        owner: 'QA Lead',
        dueDate: '2026-08-01',
        overdueDays: null,
        overdueLabel: null,
        nextAction: 'Regressionstests planen',
        escalationNeeded: false,
        actionKind: 'MITIGATION',
        actionLabel: 'Vorbereitung / Gegensteuerung',
        actionText: 'Regressionstests planen',
        requiredDecision: {
          decideWho: 'QA Lead',
          decideBy: '2026-08-01',
          impactIfDeferred: 'Qualität kann sinken',
        },
      },
    ]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Testabdeckung beobachten');
    expect(text).not.toContain('Benötigte Entscheidung');
  });

  it('should show loading state before the response arrives', () => {
    const fixture = TestBed.createComponent(ProjectIssuesActionsSectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Probleme und Risiken werden geladen');
    flushItems([]);
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

    flushItems([]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Keine offenen Probleme oder Risiken für dieses Projekt.',
    );
  });
});
