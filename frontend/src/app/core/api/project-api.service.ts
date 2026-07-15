import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';

/** Project facts API — stub for Story 1.2 structure. */
@Injectable({ providedIn: 'root' })
export class ProjectApiService extends ApiClient {
  listProjects() {
    return this.get<unknown[]>('/projects');
  }
}
