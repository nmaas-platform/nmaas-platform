package net.geant.nmaas.portal.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistent.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("jwtTokenService")
@NoArgsConstructor
@Slf4j
public class JWTTokenService {

	private JWTSettings jwtSettings;

	private static final String SCOPES = "scopes";

	private static final String LANGUAGE = "language";

	@Autowired
	public JWTTokenService(JWTSettings jwtSettings){
		this.jwtSettings = jwtSettings;
	}

	public String getToken(User user) {
		if(user == null || StringUtils.isEmpty(user.getUsername())) {
			throw new IllegalArgumentException("User or username is not set");
		}
		log.error("Get request for a token");
		log.error("user = {} {} {}", user.getId(), user.getUsername(), user.getSelectedLanguage());
		log.error("jwtSigningKey= {}", jwtSettings.getSigningKey());
		user.getRoles().forEach(role -> {
			log.error("Role = {} {} {} {}", role.getRole().toString(), role.getAuthority(), role.getDomain().getCodename(), role.getUser().getId());
		});
			String result =Jwts.builder()
					.setSubject(user.getUsername())
					.setIssuer(jwtSettings.getIssuer())
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + jwtSettings.getTokenValidFor()))
					.claim(SCOPES, user.getRoles().stream()
							.filter(role -> role.getDomain().isActive())
							.map(role -> new SimpleGrantedAuthority(role.getAuthority()))
							.collect(Collectors.toList()))
					.claim(LANGUAGE, user.getSelectedLanguage())
					.signWith(getSignInKey(jwtSettings.getSigningKey()), SignatureAlgorithm.HS512)
					.compact();
			log.error(result);
		return result;
	}
	
	public String getRefreshToken(User user) {
		if(user == null || StringUtils.isEmpty(user.getUsername())) {
			throw new IllegalArgumentException("User or username is not set");
		}

		return Jwts.builder()
					.setSubject(user.getUsername())
					.setIssuer(jwtSettings.getIssuer())
					.setId(UUID.randomUUID().toString())
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + jwtSettings.getRefreshTokenExpTime()))
					.claim(SCOPES, Collections.singletonList(JWTSettings.Scopes.REFRESH_TOKEN))
					.claim(LANGUAGE, user.getSelectedLanguage())
					.signWith(getSignInKey(jwtSettings.getSigningKey()), SignatureAlgorithm.HS512)
					.compact();
	}

	public String getResetToken(String email) {
		if(email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty");
		}

		return Jwts.builder()
				.setSubject(email)
				.setIssuer(jwtSettings.getIssuer())
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtSettings.getResetTokenExpTime()))
				.signWith(getSignInKey(jwtSettings.getResetSigningKey()), SignatureAlgorithm.HS384)

				.compact();
	}

	public String getResetToken24Hours(String email) {
		if(email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty");
		}

		return Jwts.builder()
				.setSubject(email)
				.setIssuer(jwtSettings.getIssuer())
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtSettings.getRegistrationResetTokenExpTime()))
				.signWith(SignatureAlgorithm.HS384, jwtSettings.getResetSigningKey())
				.compact();
	}
	
	public boolean validateRefreshToken(String token) {
		try {
			Claims claims = getClaims(token);
			Object scope = claims.get(SCOPES);
			return scope instanceof List<?> && ((List<String>) scope).contains(JWTSettings.Scopes.REFRESH_TOKEN.name());
		} catch(JwtException e) {
			return false;
		}
	}
	
	public Claims getClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(jwtSettings.getSigningKey()).build().parseClaimsJws(token).getBody();
	}

	public Claims getResetClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(jwtSettings.getResetSigningKey()).build().parseClaimsJws(token).getBody();
	}

	private Key getSignInKey(String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
