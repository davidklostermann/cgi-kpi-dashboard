import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { ProjectMasterData } from '../../shared/models/project-detail.model';
import { ProjectInsights, ProjectTrends } from '../../shared/models/project-insights.model';
import {
  ProjectCapacity,
  ProjectIssuesActions,
} from '../../shared/models/project-issues-capacity.model';
import { ProjectKpis } from '../../shared/models/project-kpi.model';
import { ProjectPhases } from '../../shared/models/project-phases.model';

/** Project facts API (FR-5, FR-20, FR-21). */
@Injectable({ providedIn: 'root' })
export class ProjectApiService extends ApiClient {
  listProjects() {
    return this.get<unknown[]>('/projects');
  }

  getProjectMasterData(projectId: string) {
    return this.get<ProjectMasterData>(`/projects/${projectId}/master-data`);
  }

  getProjectKpis(projectId: string) {
    return this.get<ProjectKpis>(`/projects/${projectId}/kpis`);
  }

  getProjectPhases(projectId: string) {
    return this.get<ProjectPhases>(`/projects/${projectId}/phases`);
  }

  getProjectInsights(projectId: string) {
    return this.get<ProjectInsights>(`/projects/${projectId}/insights`);
  }

  getProjectTrends(projectId: string) {
    return this.get<ProjectTrends>(`/projects/${projectId}/trends`);
  }

  getProjectIssuesActions(projectId: string) {
    return this.get<ProjectIssuesActions>(`/projects/${projectId}/issues-actions`);
  }

  getProjectCapacity(projectId: string) {
    return this.get<ProjectCapacity>(`/projects/${projectId}/capacity`);
  }
}
