package net.geant.nmaas.portal.api.security;

import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionServiceTest {

    private static final String KEY = "nmaasplatformgn5";
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private final EncryptionService service = new EncryptionService(KEY, ALGORITHM);

    @Test
    void shouldEncryptAndDecrypt() throws GeneralSecurityException {
        final String plainText = "test-content";
        String encryptedText = service.encrypt(plainText);
        assertThat(plainText).isEqualTo(service.decrypt(encryptedText));
    }

}
