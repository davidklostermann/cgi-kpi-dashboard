package com.cgi.kpi.dashboard.ai.client;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;

/**
 * Abstract model provider — no secrets in frontend (FR-14).
 */
public interface AiModelClient {

    ProjectAiAnalysisResponseDto analyze(ApprovedProjectContextDto context);

    ProjectAiQuestionResponseDto answer(ApprovedProjectContextDto context, String question);
}
