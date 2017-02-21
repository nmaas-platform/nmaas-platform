package net.geant.nmaas.portal.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.impl.crypto.MacProvider;

@Component
public class JWTSettings {

	public enum Scopes {
		REFRESH_TOKEN;
	}
	
	@Value("${jwt.tokenValidFor}")
	private Long tokenValidFor;
	
	@Value("${jwt.issuer}")
	private String issuer;
	
	@Value("${jwt.signingKey}")
	private String signingKey = MacProvider.generateKey().toString();
	
	@Value("${jwt.refreshTokenValidFor}")
	private Long refreshTokenExpTime;

	public Long getTokenValidFor() {
		return tokenValidFor;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getSigningKey() {
		return signingKey;
	}

	public Long getRefreshTokenExpTime() {
		return refreshTokenExpTime;
	}
	
}
