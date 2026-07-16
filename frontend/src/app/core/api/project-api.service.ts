import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { ProjectMasterData } from '../../shared/models/project-detail.model';

/** Project facts API — stub for Story 1.2 structure. */
@Injectable({ providedIn: 'root' })
export class ProjectApiService extends ApiClient {
  listProjects() {
    return this.get<unknown[]>('/projects');
  }

  getProjectMasterData(projectId: string) {
    return this.get<ProjectMasterData>(`/projects/${projectId}/master-data`);
  }
}
