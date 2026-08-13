import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';

import { ProjectApiService } from './project-api.service';
import {
  ProjectCapacity,
  ProjectIssuesActions,
} from '../../shared/models/project-issues-capacity.model';
import { ProjectIsoManagement } from '../../shared/models/project-iso-management.model';
import { AgileDelivery } from '../../shared/models/agile-delivery.model';

describe('ProjectApiService issues and capacity', () => {
  let service: ProjectApiService;
  let httpMock: HttpTestingController;
  const projectId = 'a0000000-0000-4000-8000-000000000001';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProjectApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getProjectIssuesActions should call /api/projects/{id}/issues-actions', async () => {
    const mock: ProjectIssuesActions = {
      projectId,
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    };

    const responsePromise = firstValueFrom(service.getProjectIssuesActions(projectId));
    const req = httpMock.expectOne(`/api/projects/${projectId}/issues-actions`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);

    await expect(responsePromise).resolves.toEqual(mock);
  });

  it('getProjectCapacity should call /api/projects/{id}/capacity', async () => {
    const mock: ProjectCapacity = {
      projectId,
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    };

    const responsePromise = firstValueFrom(service.getProjectCapacity(projectId));
    const req = httpMock.expectOne(`/api/projects/${projectId}/capacity`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);

    await expect(responsePromise).resolves.toEqual(mock);
  });

  it('getProjectIsoManagement should call /api/projects/{id}/iso-management', async () => {
    const mock: ProjectIsoManagement = {
      projectId,
      dataAvailable: true,
      emptyReason: null,
      factsAsOf: '2026-07-15T08:00:00Z',
      benefits: null,
      scope: null,
      changeRequests: null,
      quality: null,
      stakeholders: null,
    };

    const responsePromise = firstValueFrom(service.getProjectIsoManagement(projectId));
    const req = httpMock.expectOne(`/api/projects/${projectId}/iso-management`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);

    await expect(responsePromise).resolves.toEqual(mock);
  });

  it('getAgileDelivery should call /api/projects/{id}/agile-delivery', async () => {
    const mock: AgileDelivery = {
      projectId,
      deliveryMethod: 'AGILE',
      dataAvailable: false,
      emptyReason: 'Keine Sprint-Daten für dieses Projekt hinterlegt.',
      dataSource: 'INTERNAL_MOCK',
      factsAsOf: null,
      sprints: [],
      chart: {
        sprintLabels: [],
        plannedStoryPoints: [],
        completedStoryPoints: [],
        velocityTrend: [],
        futureFlags: [],
      },
      kpis: {
        sprintHealth: null,
        sprintHealthLabel: null,
        totalStoryPoints: 0,
        averageVelocity: null,
        carryOverNextSprint: 0,
        openBlockerCount: 0,
      },
    };

    const responsePromise = firstValueFrom(service.getAgileDelivery(projectId));
    const req = httpMock.expectOne(`/api/projects/${projectId}/agile-delivery`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);

    await expect(responsePromise).resolves.toEqual(mock);
  });
});
