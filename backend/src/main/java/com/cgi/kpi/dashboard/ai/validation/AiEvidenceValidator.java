package com.cgi.kpi.dashboard.ai.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.EvidenceItemDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.PriorityDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.SuggestedActionDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;

@Component
public class AiEvidenceValidator {

    private static final int MAX_PRIORITIES = 3;
    private static final int MIN_EVIDENCE_ITEMS = 2;

    public ProjectAiAnalysisResponseDto validateAnalysis(
            ApprovedProjectContextDto context,
            ProjectAiAnalysisResponseDto raw) {
        Map<String, ApprovedProjectFactDto> factsById = context.facts().stream()
                .collect(Collectors.toMap(
                        ApprovedProjectFactDto::factId,
                        fact -> fact,
                        (left, right) -> left));
        Set<String> allowed = new HashSet<>(factsById.keySet());

        List<PriorityDto> priorities = new ArrayList<>();
        if (raw.priorities() != null) {
            for (PriorityDto priority : raw.priorities()) {
                if (priority == null) {
                    continue;
                }
                PriorityDto validated = validatePriority(priority, factsById, allowed);
                if (validated != null) {
                    priorities.add(validated);
                }
                if (priorities.size() == MAX_PRIORITIES) {
                    break;
                }
            }
        }

        List<SuggestedActionDto> actions = new ArrayList<>();
        if (raw.suggestedActions() != null) {
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
                .map(ApprovedProjectFactDto::factId)
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

    private static PriorityDto validatePriority(
            PriorityDto priority,
            Map<String, ApprovedProjectFactDto> factsById,
            Set<String> allowed) {
        if (priority.title() == null || priority.title().isBlank()) {
            return null;
        }

        List<String> factIds = filterEvidence(priority.evidenceFactIds(), allowed);
        List<EvidenceItemDto> evidenceItems = new ArrayList<>();

        if (priority.evidence() != null) {
            for (EvidenceItemDto item : priority.evidence()) {
                if (item == null || item.label() == null || item.label().isBlank()) {
                    continue;
                }
                String source = item.sourceField();
                if (source == null || source.isBlank() || !allowed.contains(source)) {
                    continue;
                }
                ApprovedProjectFactDto fact = factsById.get(source);
                if (fact == null) {
                    continue;
                }
                evidenceItems.add(new EvidenceItemDto(fact.label(), fact.displayValue(), fact.factId()));
            }
        }

        if (evidenceItems.size() < MIN_EVIDENCE_ITEMS) {
            for (String factId : factIds) {
                ApprovedProjectFactDto fact = factsById.get(factId);
                if (fact == null) {
                    continue;
                }
                boolean already = evidenceItems.stream()
                        .anyMatch(e -> factId.equals(e.sourceField()));
                if (!already) {
                    evidenceItems.add(new EvidenceItemDto(fact.label(), fact.displayValue(), fact.factId()));
                }
                if (evidenceItems.size() >= MIN_EVIDENCE_ITEMS) {
                    break;
                }
            }
        }

        if (evidenceItems.size() < MIN_EVIDENCE_ITEMS) {
            return null;
        }

        factIds = evidenceItems.stream()
                .map(EvidenceItemDto::sourceField)
                .filter(id -> id != null && allowed.contains(id))
                .distinct()
                .toList();
        if (factIds.isEmpty()) {
            return null;
        }

        String implication = blankToNull(priority.managementImplication());
        String decision = blankToNull(priority.requiredDecision());
        if (implication == null || decision == null) {
            return null;
        }

        return new PriorityDto(
                priority.rank(),
                priority.title().trim(),
                implication,
                decision,
                List.copyOf(evidenceItems),
                List.copyOf(factIds));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static List<String> filterEvidence(List<String> evidenceFactIds, Set<String> allowed) {
        if (evidenceFactIds == null) {
            return List.of();
        }
        return evidenceFactIds.stream().filter(allowed::contains).distinct().toList();
    }
}
