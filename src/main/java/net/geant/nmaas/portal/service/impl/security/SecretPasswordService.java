package net.geant.nmaas.portal.service.impl.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecretPasswordService {

    private final PasswordEncoder passwordEncoder;

    public String hashSecret(String secret) {
        return passwordEncoder.encode(secret);
    }

    public boolean verifySecret(String rawSecret, String hashedSecret) {
        return passwordEncoder.matches(rawSecret, hashedSecret);
    }

}
