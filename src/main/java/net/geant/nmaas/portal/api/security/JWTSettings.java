package net.geant.nmaas.portal.api.security;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.KeyGenerator;

@Component
@Getter
public class JWTSettings {

	public enum Scopes {
		REFRESH_TOKEN;
	}
	KeyGenerator keyGenerator;

    {
        try {
            keyGenerator = KeyGenerator.getInstance("HmacSha256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    @Value("${jwt.tokenValidFor}")
	private Long tokenValidFor;

	@Value("${jwt.resetTokenValidFor}")
	private Long resetTokenExpTime;

	@Value("${jwt.resetTokenRegistrationValid}")
	private Long registrationResetTokenExpTime;
	
	@Value("${jwt.issuer}")
	private String issuer;
	
	@Value("${jwt.signingKey}")
	private String signingKey = String.valueOf(keyGenerator.generateKey());
	
	@Value("${jwt.refreshTokenValidFor}")
	private Long refreshTokenExpTime;

	@Value("${jwt.resetSigningKey}")
	private String resetSigningKey = UUID.randomUUID().toString();
}
