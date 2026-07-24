package com.cgi.kpi.dashboard.api.admin;

import com.cgi.kpi.dashboard.admin.ai.AiConfigService;
import com.cgi.kpi.dashboard.admin.ai.dto.AiProviderConfigDto;
import com.cgi.kpi.dashboard.api.admin.dto.SaveAiConfigRequestDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for AI configuration management (ADMIN only).
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {

    private final AiConfigService aiConfigService;

    public AdminAiController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @GetMapping("/config")
    public Optional<AiProviderConfigDto> getConfig(@RequestParam(defaultValue = "gemini") String provider) {
        return aiConfigService.getConfig(provider);
    }

    @PutMapping("/config")
    public AiProviderConfigDto saveConfig(@Valid @RequestBody SaveAiConfigRequestDto request) {
        return aiConfigService.saveConfig(
                request.provider(),
                request.model(),
                request.apiKey(),
                request.enabled()
        );
    }

    @PostMapping("/test-connection")
    public ConnectionTestResponseDto testConnection(@RequestParam(defaultValue = "gemini") String provider) {
        return aiConfigService.testConnection(provider);
    }

    public record ConnectionTestResponseDto(boolean success, String message) {
    }
}
