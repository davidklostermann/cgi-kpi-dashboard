package com.cgi.kpi.dashboard.security.crypto;

/**
 * Exception thrown when encryption or decryption fails.
 */
public class EncryptionException extends RuntimeException {
    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
