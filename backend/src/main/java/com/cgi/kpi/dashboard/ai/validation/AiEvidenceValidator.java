package com.cgi.kpi.dashboard.ai.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.PriorityDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.SuggestedActionDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;

@Component
public class AiEvidenceValidator {

    public ProjectAiAnalysisResponseDto validateAnalysis(
            ApprovedProjectContextDto context,
            ProjectAiAnalysisResponseDto raw) {
        Set<String> allowed = context.facts().stream()
                .map(ApprovedProjectContextDto.ApprovedProjectFactDto::factId)
                .collect(Collectors.toCollection(HashSet::new));

        List<PriorityDto> priorities = new ArrayList<>();
        for (PriorityDto priority : raw.priorities()) {
            List<String> evidence = filterEvidence(priority.evidenceFactIds(), allowed);
            if (!evidence.isEmpty()) {
                priorities.add(new PriorityDto(priority.rank(), priority.title(), priority.reason(), evidence));
            }
        }

        List<SuggestedActionDto> actions = new ArrayList<>();
        for (SuggestedActionDto action : raw.suggestedActions()) {
            List<String> evidence = filterEvidence(action.evidenceFactIds(), allowed);
            if (!evidence.isEmpty()) {
                actions.add(new SuggestedActionDto(
                        action.title(),
                        action.reason(),
                        action.suggestedOwner(),
                        action.suggestedDueDate(),
                        action.addressesType(),
                        action.addressesId(),
                        action.expectedEffect(),
                        evidence,
                        true));
            }
        }

        return new ProjectAiAnalysisResponseDto(
                raw.projectId(),
                raw.factsAsOf(),
                raw.generatedAt(),
                raw.status(),
                raw.availableSources(),
                raw.summary(),
                List.copyOf(priorities),
                List.copyOf(actions),
                raw.missingData(),
                true,
                raw.disclaimer());
    }

    public ProjectAiQuestionResponseDto validateQuestion(
            ApprovedProjectContextDto context,
            ProjectAiQuestionResponseDto raw) {
        Set<String> allowed = context.facts().stream()
                .map(ApprovedProjectContextDto.ApprovedProjectFactDto::factId)
                .collect(Collectors.toCollection(HashSet::new));
        List<String> evidence = filterEvidence(raw.evidenceFactIds(), allowed);
        if (evidence.isEmpty()) {
            return new ProjectAiQuestionResponseDto(
                    "Dazu liegen keine ausreichend konkreten freigegebenen Projektdaten vor.",
                    List.of(),
                    raw.factsAsOf(),
                    raw.generatedAt(),
                    true,
                    true,
                    raw.disclaimer());
        }
        return new ProjectAiQuestionResponseDto(
                raw.answer(),
                evidence,
                raw.factsAsOf(),
                raw.generatedAt(),
                false,
                true,
                raw.disclaimer());
    }

    private static List<String> filterEvidence(List<String> evidenceFactIds, Set<String> allowed) {
        if (evidenceFactIds == null) {
            return List.of();
        }
        return evidenceFactIds.stream().filter(allowed::contains).distinct().toList();
    }
}
