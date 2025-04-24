package net.geant.nmaas.portal.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    @Value("${security.encryption.secret-key}")
    private String secretKey;

    @Value("${security.encryption.algorithm}")
    private String algorithm;

    public String encrypt(String plainText) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKey key = getKey();

        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combinedData = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, combinedData, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, combinedData, IV_LENGTH, encryptedData.length);

        return Base64.getEncoder().encodeToString(combinedData);
    }

    public String decrypt(String encryptedText) throws GeneralSecurityException {
        byte[] decodedData = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);

        byte[] encryptedData = new byte[decodedData.length - IV_LENGTH];
        System.arraycopy(decodedData, IV_LENGTH, encryptedData, 0, encryptedData.length);

        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKey key = getKey();
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    private SecretKey getKey() {
        return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
    }
}
