package com.cgi.kpi.dashboard.security.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private final String masterKey = "12345678901234567890123456789012"; // 32 characters for AES-256

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "masterKey", masterKey);
        encryptionService.init();
    }

    @Test
    void shouldEncryptAndDecryptSuccessfully() {
        String originalText = "sensitive-api-key-123";
        
        String encryptedText = encryptionService.encrypt(originalText);
        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);
        
        String decryptedText = encryptionService.decrypt(encryptedText);
        assertEquals(originalText, decryptedText);
    }

    @Test
    void shouldThrowExceptionWhenMasterKeyIsMissing() {
        EncryptionService serviceWithoutKey = new EncryptionService();
        // Skip init or init with null
        assertThrows(EncryptionException.class, () -> serviceWithoutKey.encrypt("test"));
    }

    @Test
    void shouldThrowExceptionWhenDecryptingInvalidText() {
        assertThrows(EncryptionException.class, () -> encryptionService.decrypt("invalid-base64"));
    }
}
