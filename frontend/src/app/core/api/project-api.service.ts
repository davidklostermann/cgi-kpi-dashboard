import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { ProjectMasterData } from '../../shared/models/project-detail.model';
import { ProjectKpis } from '../../shared/models/project-kpi.model';
import { ProjectPhases } from '../../shared/models/project-phases.model';

/** Project facts API (FR-5). */
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
}
