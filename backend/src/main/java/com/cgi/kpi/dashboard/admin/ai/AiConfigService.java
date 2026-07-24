package com.cgi.kpi.dashboard.admin.ai;

import com.cgi.kpi.dashboard.admin.ai.dto.AiProviderConfigDto;
import com.cgi.kpi.dashboard.ai.cache.ProjectAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.client.GeminiApiTransport;
import com.cgi.kpi.dashboard.ai.config.AiActiveConfigProvider;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.api.admin.AdminAiController;
import com.cgi.kpi.dashboard.infrastructure.persistence.AiProviderConfigRepository;
import com.cgi.kpi.dashboard.security.crypto.EncryptionService;
import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
    private final GeminiApiTransport geminiTransport;
    private final MessageSource messageSource;
    
    private final AtomicLong configVersion = new AtomicLong(System.currentTimeMillis());

    public AiConfigService(
            AiProviderConfigRepository repository, 
            EncryptionService encryptionService,
            AiProperties aiProperties,
            ProjectAiAnalysisCache cache,
            @Lazy GeminiApiTransport geminiTransport,
            MessageSource messageSource) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.aiProperties = aiProperties;
        this.cache = cache;
        this.geminiTransport = geminiTransport;
        this.messageSource = messageSource;
    }

    public AiProviderConfigDto saveConfig(String provider, String model, String apiKey, boolean enabled) {
        // Find existing or create new
        AiProviderConfig config = repository.findByProvider(provider)
                .orElse(new AiProviderConfig());

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

    public Optional<AiProviderConfigDto> getConfig(String provider) {
        return repository.findByProvider(provider).map(this::toDto);
    }

    @Override
    public String getActiveApiKey(String provider) {
        return repository.findByProvider(provider)
                .filter(AiProviderConfig::isEnabled)
                .map(c -> Optional.ofNullable(c.getApiKeyCiphertext())
                                    .map(encryptionService::decrypt)
                                    .orElseGet(aiProperties::getApiKey))
                .orElseGet(aiProperties::getApiKey);
    }

    @Override
    public String getActiveModel(String provider) {
        return repository.findByProvider(provider)
                .filter(AiProviderConfig::isEnabled)
                .map(AiProviderConfig::getModel)
                .orElseGet(aiProperties::getModel);
    }

    @Override
    public long getCurrentVersion() {
        return configVersion.get();
    }

    @Override
    public boolean isEnabled(String provider) {
        return repository.findByProvider(provider)
                .map(AiProviderConfig::isEnabled)
                .orElseGet(aiProperties::isEnabled);
    }

    public AdminAiController.ConnectionTestResponseDto testConnection(String provider) {
        if (!"gemini".equalsIgnoreCase(provider)) {
            return new AdminAiController.ConnectionTestResponseDto(false, 
                messageSource.getMessage("ai.config.test.providerUnsupported", new Object[]{provider}, Locale.getDefault()));
        }
        
        try {
            // Simple echo prompt to test connection
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
    }

    private AiProviderConfigDto toDto(AiProviderConfig entity) {
        String decryptedKey = encryptionService.decrypt(entity.getApiKeyCiphertext());
        return new AiProviderConfigDto(
                entity.getId(),
                entity.getProvider(),
                entity.getModel(),
                AiProviderConfigDto.maskApiKey(decryptedKey),
                entity.isEnabled()
        );
    }
}
