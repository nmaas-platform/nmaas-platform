package net.geant.nmaas.portal.service.impl.security;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserApiToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * Find user behind uuid token
     * @param rawSecret
     * @param tokens
     * @return
     */
    public User findUserBasedOnToken(String rawSecret, List<UserApiToken> tokens) {
       for (UserApiToken token : tokens){
           if (verifySecret(rawSecret, token.getTokenValue()))
               return token.getUser();
       }
       throw new TokenAuthenticationException("This uuid token does not exist");
    }

}
