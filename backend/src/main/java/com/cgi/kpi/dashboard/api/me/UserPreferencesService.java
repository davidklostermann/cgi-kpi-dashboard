package com.cgi.kpi.dashboard.api.me;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.api.me.dto.UpdateUserPreferencesRequestDto;
import com.cgi.kpi.dashboard.api.me.dto.UserPreferencesResponseDto;
import com.cgi.kpi.dashboard.domain.model.UserUiPreferences;
import com.cgi.kpi.dashboard.infrastructure.persistence.UserUiPreferencesRepository;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class UserPreferencesService {

    private static final String[] IDENTITY_FIELD_NAMES = {"userId", "workspaceId", "user_id", "workspace_id"};

    private final UserUiPreferencesRepository preferencesRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public UserPreferencesService(
            UserUiPreferencesRepository preferencesRepository,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper) {
        this.preferencesRepository = preferencesRepository;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public UserPreferencesResponseDto getPreferences() {
        var principal = currentUserService.requirePrincipal();
        return preferencesRepository
                .findByWorkspaceIdAndUserId(principal.getWorkspaceId(), principal.getUserId())
                .map(entity -> new UserPreferencesResponseDto(sanitizePreferences(parseJson(entity.getPreferencesJson()))))
                .orElseGet(() -> new UserPreferencesResponseDto(objectMapper.createObjectNode()));
    }

    @Transactional
    public UserPreferencesResponseDto savePreferences(UpdateUserPreferencesRequestDto request) {
        var principal = currentUserService.requirePrincipal();
        JsonNode raw = request.preferences() != null ? request.preferences() : objectMapper.createObjectNode();
        JsonNode safe = sanitizePreferences(raw);

        UserUiPreferences entity = preferencesRepository
                .findByWorkspaceIdAndUserId(principal.getWorkspaceId(), principal.getUserId())
                .orElseGet(UserUiPreferences::new);

        if (entity.getId() == null) {
            entity.setWorkspaceId(principal.getWorkspaceId());
            entity.setUserId(principal.getUserId());
            entity.setCreatedAt(Instant.now());
        }
        entity.setPreferencesJson(writeJson(safe));
        entity.setUpdatedAt(Instant.now());
        preferencesRepository.save(entity);
        return new UserPreferencesResponseDto(safe);
    }

    private JsonNode parseJson(String raw) {
        try {
            return objectMapper.readTree(raw == null || raw.isBlank() ? "{}" : raw);
        } catch (JsonProcessingException exception) {
            return objectMapper.createObjectNode();
        }
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node == null ? objectMapper.createObjectNode() : node);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    /** Removes client-supplied identity fields at all nesting levels (AD-13). */
    private JsonNode sanitizePreferences(JsonNode node) {
        if (node == null || node.isNull()) {
            return objectMapper.createObjectNode();
        }
        JsonNode copy = node.deepCopy();
        stripIdentityFields(copy);
        return copy;
    }

    private void stripIdentityFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            for (String fieldName : IDENTITY_FIELD_NAMES) {
                objectNode.remove(fieldName);
            }
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                stripIdentityFields(fields.next().getValue());
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                stripIdentityFields(child);
            }
        }
    }
}
