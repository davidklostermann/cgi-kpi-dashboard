import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { ProjectAiAnalysis, ProjectAiQuestionResponse } from '../../shared/models/project-ai.model';

export interface AiReadinessResponse {
  ready: boolean;
}

/** Project AI panel API — never calls external model providers (FR-14). */
@Injectable({ providedIn: 'root' })
export class ProjectAiApiService extends ApiClient {
  checkReadiness() {
    return this.get<AiReadinessResponse>('/me/ai/readiness');
  }

  getAnalysis(projectId: string, refresh = false) {
    return this.get<ProjectAiAnalysis>(`/projects/${projectId}/ai/analysis`, {
      refresh: refresh ? 'true' : 'false',
    });
  }

  askQuestion(projectId: string, question: string) {
    return this.post<ProjectAiQuestionResponse>(`/projects/${projectId}/ai/questions`, {
      question,
    });
  }
}
