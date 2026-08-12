package com.cgi.kpi.dashboard.admin.ai;

import com.cgi.kpi.dashboard.infrastructure.persistence.AiProviderConfigRepository;
import com.cgi.kpi.dashboard.security.crypto.EncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiConfigEncryptionIntegrationTest {

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private AiProviderConfigRepository repository;

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private CurrentUserService currentUserService;

    private final UUID TEST_USER_ID = UUID.randomUUID();

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);
        AppUser user = new AppUser();
        user.setId(TEST_USER_ID);
        user.setUsername("testencryptuser");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setActive(true);
        user.setMustChangePassword(false);
        appUserRepository.save(user);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.master-key", () -> "test-master-key-32-chars-long-abc");
    }

    @Test
    void shouldStoreEncryptedApiKeyInDatabaseViaService() {
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);

        String originalKey = "secret-gemini-key";
        String provider = "gemini-test";
        
        // Use service to save config
        aiConfigService.saveConfig(provider, "gemini-1.5-pro", originalKey, true);
        
        AiProviderConfig entity = repository.findByUserIdAndProvider(TEST_USER_ID, provider).orElseThrow();
        
        // 1. Verify via JDBC (direct DB access)
        Map<String, Object> dbRow = jdbcTemplate.queryForMap(
                "SELECT api_key_ciphertext FROM ai_provider_config WHERE id = ?", entity.getId());
        
        String dbValue = (String) dbRow.get("api_key_ciphertext");
        assertNotEquals(originalKey, dbValue, "Key should be encrypted in DB");
        
        // 2. Verify Decryption
        String decryptedValue = encryptionService.decrypt(dbValue);
        assertEquals(originalKey, decryptedValue, "Should be able to decrypt the key");
    }

    @Test
    void shouldReturnMaskedKeyInDto() {
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);

        String originalKey = "secret-gemini-key-123456";
        String provider = "gemini-mask-test";
        
        var dto = aiConfigService.saveConfig(provider, "gemini-1.5-pro", originalKey, true);
        
        assertNotNull(dto.apiKeyMasked());
        assertTrue(dto.apiKeyMasked().startsWith("********"));
        assertTrue(dto.apiKeyMasked().endsWith("3456"));
        assertFalse(dto.apiKeyMasked().contains(originalKey.substring(0, 10)), "Masked key should not contain original key prefix");
    }
}
