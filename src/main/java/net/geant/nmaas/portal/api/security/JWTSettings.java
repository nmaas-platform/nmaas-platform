package net.geant.nmaas.portal.api.security;

import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.impl.crypto.MacProvider;

@Component
@Getter
public class JWTSettings {

	public enum Scopes {
		REFRESH_TOKEN;
	}
	
	@Value("${jwt.tokenValidFor}")
	private Long tokenValidFor;

	@Value("${jwt.resetTokenValidFor}")
	private Long resetTokenExpTime;
	
	@Value("${jwt.issuer}")
	private String issuer;
	
	@Value("${jwt.signingKey}")
	private String signingKey = MacProvider.generateKey().toString();
	
	@Value("${jwt.refreshTokenValidFor}")
	private Long refreshTokenExpTime;

	@Value("${jwt.resetSigningKey}")
	private String resetSigningKey = UUID.randomUUID().toString();
}
