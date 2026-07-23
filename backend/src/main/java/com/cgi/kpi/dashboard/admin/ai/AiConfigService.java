package com.cgi.kpi.dashboard.admin.ai;

import com.cgi.kpi.dashboard.admin.ai.dto.AiProviderConfigDto;
import com.cgi.kpi.dashboard.infrastructure.persistence.AiProviderConfigRepository;
import com.cgi.kpi.dashboard.security.crypto.EncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing AI provider configuration with encryption.
 */
@Service
@Transactional
public class AiConfigService {

    private final AiProviderConfigRepository repository;
    private final EncryptionService encryptionService;

    public AiConfigService(AiProviderConfigRepository repository, EncryptionService encryptionService) {
        this.repository = repository;
        this.encryptionService = encryptionService;
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
        
        return toDto(saved);
    }

    public Optional<AiProviderConfigDto> getConfig(String provider) {
        return repository.findByProvider(provider).map(this::toDto);
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
