package com.cgi.kpi.dashboard.admin.ai;

import com.cgi.kpi.dashboard.admin.ai.dto.AiProviderConfigDto;
import com.cgi.kpi.dashboard.ai.cache.PortfolioAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.cache.ProjectAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.client.GeminiApiTransport;
import com.cgi.kpi.dashboard.ai.config.AiActiveConfigProvider;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.api.admin.AdminAiController;
import com.cgi.kpi.dashboard.infrastructure.persistence.AiProviderConfigRepository;
import com.cgi.kpi.dashboard.security.crypto.EncryptionService;
import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import org.springframework.context.annotation.Lazy;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.MessageSource;
import java.util.Locale;

/**
 * Service for managing AI provider configuration with encryption.
 */
@Service
@Transactional
public class AiConfigService implements AiActiveConfigProvider {

    private final AiProviderConfigRepository repository;
    private final EncryptionService encryptionService;
    private final AiProperties aiProperties;
    private final ProjectAiAnalysisCache cache;
    private final PortfolioAiAnalysisCache portfolioCache;
    private final GeminiApiTransport geminiTransport;
    private final MessageSource messageSource;
    private final CurrentUserService currentUserService;

    private final AtomicLong configVersion = new AtomicLong(System.currentTimeMillis());

    public AiConfigService(
            AiProviderConfigRepository repository, 
            EncryptionService encryptionService,
            AiProperties aiProperties,
            ProjectAiAnalysisCache cache,
            PortfolioAiAnalysisCache portfolioCache,
            @Lazy GeminiApiTransport geminiTransport,
            MessageSource messageSource,
            CurrentUserService currentUserService) {
        this.repository = repository; 
        this.encryptionService = encryptionService;
        this.aiProperties = aiProperties;
        this.cache = cache;
        this.portfolioCache = portfolioCache;
        this.geminiTransport = geminiTransport;
        this.messageSource = messageSource;
        this.currentUserService = currentUserService;
    }

    public AiProviderConfigDto saveConfig(String provider, String model, String apiKey, boolean enabled) {
        UUID userId = currentUserService.requireUserId();
        // Find existing or create new
        AiProviderConfig config = repository.findByUserIdAndProvider(userId, provider)
                .orElse(new AiProviderConfig());

        config.setUserId(userId);

        config.setProvider(provider);
        config.setModel(model);
        config.setEnabled(enabled);
        
        if (apiKey != null && !apiKey.isBlank()) {
            config.setApiKeyCiphertext(encryptionService.encrypt(apiKey));
        }

        AiProviderConfig saved = repository.save(config);
        
        // Invalidate cache and bump version (AD-18)
        bumpVersionAndInvalidateCache();
        
        return toDto(saved);
    }

    public void deleteConfig(UUID userId, String provider) {
        repository.findByUserIdAndProvider(userId, provider).ifPresent(config -> {
            repository.delete(config);
            bumpVersionAndInvalidateCache();
        });
    }

    public Optional<AiProviderConfigDto> getConfig(String provider) {
        UUID userId = currentUserService.requireUserId();
        return repository.findByUserIdAndProvider(userId, provider).map(this::toDto);
    }

    @Override
    public String getActiveApiKey(String provider) {
        UUID userId = currentUserService.requireUserId();
        Optional<AiProviderConfig> configOptional = repository.findByUserIdAndProvider(userId, provider);

        if ("gemini".equalsIgnoreCase(provider)) {
            // For Gemini, only use the user's specific DB config. No global fallback.
            return configOptional
                    .filter(AiProviderConfig::isEnabled)
                    .map(c -> encryptionService.decrypt(c.getApiKeyCiphertext()))
                    .orElse(null); // Return null if no enabled key found for the user
        } else {
            // For other providers (e.g., mock), use existing logic
            return configOptional
                    .filter(AiProviderConfig::isEnabled)
                    .map(c -> Optional.ofNullable(c.getApiKeyCiphertext())
                                        .map(encryptionService::decrypt)
                                        .orElseGet(aiProperties::getApiKey))
                    .orElseGet(aiProperties::getApiKey);
        }
    }

    @Override
    public String getActiveModel(String provider) {
        UUID userId = currentUserService.requireUserId();
        Optional<AiProviderConfig> configOptional = repository.findByUserIdAndProvider(userId, provider);

        if ("gemini".equalsIgnoreCase(provider)) {
            return configOptional
                    .filter(AiProviderConfig::isEnabled)
                    .map(AiProviderConfig::getModel)
                    .orElse(null); // No model if no active config for user
        } else {
            return configOptional
                    .filter(AiProviderConfig::isEnabled)
                    .map(AiProviderConfig::getModel)
                    .orElseGet(aiProperties::getModel);
        }
    }

    @Override
    public long getCurrentVersion() {
        return configVersion.get();
    }

    @Override
    public boolean isEnabled(String provider) {
        UUID userId = currentUserService.requireUserId();
        Optional<AiProviderConfig> configOptional = repository.findByUserIdAndProvider(userId, provider);

        if ("gemini".equalsIgnoreCase(provider)) {
            // For Gemini, it's enabled if there is an active user-specific config
            return configOptional.map(AiProviderConfig::isEnabled).orElse(false);
        } else {
            return configOptional
                    .map(AiProviderConfig::isEnabled)
                    .orElseGet(aiProperties::isEnabled);
        }
    }

    public AdminAiController.ConnectionTestResponseDto testConnection(String provider) {
        UUID userId = currentUserService.requireUserId();

        if (!"gemini".equalsIgnoreCase(provider)) {
            return new AdminAiController.ConnectionTestResponseDto(false, 
                messageSource.getMessage("ai.config.test.providerUnsupported", new Object[]{provider}, Locale.getDefault()));
        }

        // Get the active API key for the current user. If null, it means no key is configured.
        String apiKey = getActiveApiKey(provider);
        if (apiKey == null || apiKey.isBlank()) {
            return new AdminAiController.ConnectionTestResponseDto(false, 
                messageSource.getMessage("ai.config.test.noApiKeyConfigured", null, Locale.getDefault()));
        }

        try {
            // Simple echo prompt to test connection using the user's API key
            String response = geminiTransport.generateJson("Echo 'OK' if you can read this.");
            if (response != null && response.toUpperCase().contains("OK")) {
                return new AdminAiController.ConnectionTestResponseDto(true, 
                    messageSource.getMessage("ai.config.test.success", null, Locale.getDefault()));
            } else {
                return new AdminAiController.ConnectionTestResponseDto(false, 
                    messageSource.getMessage("ai.config.test.unexpectedResponse", new Object[]{response}, Locale.getDefault()));
            }
        } catch (GeminiTransportException e) {
            return new AdminAiController.ConnectionTestResponseDto(false, 
                messageSource.getMessage("ai.config.test.connectionError", new Object[]{e.getMessage()}, Locale.getDefault()));
        } catch (Exception e) {
            return new AdminAiController.ConnectionTestResponseDto(false, 
                messageSource.getMessage("ai.config.test.unexpectedError", new Object[]{e.getMessage()}, Locale.getDefault()));
        }
    }

    private void bumpVersionAndInvalidateCache() {
        configVersion.incrementAndGet();
        cache.invalidateAll();
        portfolioCache.invalidateAll();
    }

    private AiProviderConfigDto toDto(AiProviderConfig entity) {
        String decryptedKey = encryptionService.decrypt(entity.getApiKeyCiphertext());
        return new AiProviderConfigDto(
                entity.getId(),
                entity.getProvider(),
                entity.getModel(),
                AiProviderConfigDto.maskApiKey(decryptedKey),
                entity.isEnabled(),
                entity.getUserId()
        );
    }
}
