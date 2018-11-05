package net.geant.nmaas.portal.api.security;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.jsonwebtoken.*;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.persistent.entity.User;

@Service("jwtTokenService")
@NoArgsConstructor
public class JWTTokenService {

	private JWTSettings jwtSettings;

	private static final String SCOPES = "scopes";

	@Autowired
	public JWTTokenService(JWTSettings jwtSettings){
		this.jwtSettings = jwtSettings;
	}

	public String getToken(User user) {
		if(user == null || StringUtils.isEmpty(user.getUsername())) 
			throw new IllegalArgumentException("User or username is not set");
			
		return Jwts.builder()
		.setSubject(user.getUsername())
		.setIssuer(jwtSettings.getIssuer())
		.setIssuedAt(new Date())
		.setExpiration(new Date(System.currentTimeMillis() + jwtSettings.getTokenValidFor()))
		.claim(SCOPES, user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority())).collect(Collectors.toList()))
		.signWith(SignatureAlgorithm.HS512, jwtSettings.getSigningKey())
		.compact();
	}
	
	public String getRefreshToken(User user) {
		if(user == null || StringUtils.isEmpty(user.getUsername())) 
			throw new IllegalArgumentException("User or username is not set");
		
		return Jwts.builder()
					.setSubject(user.getUsername())
					.setIssuer(jwtSettings.getIssuer())
					.setId(UUID.randomUUID().toString())
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + jwtSettings.getRefreshTokenExpTime()))
					.claim(SCOPES, Arrays.asList(JWTSettings.Scopes.REFRESH_TOKEN))
					.signWith(SignatureAlgorithm.HS512, jwtSettings.getSigningKey())
					.compact();
	}
	
	public boolean validateRefreshToken(String token) {
		try {
			Claims claims = getClaims(token);
			Object scope = claims.get(SCOPES);
			if(scope instanceof List<?> && ((List<String>)scope).contains(JWTSettings.Scopes.REFRESH_TOKEN.name())) {
					return true;
			} 
			return false;
		} catch(JwtException e) {
			return false;
		}
		
	}
	
	public Claims getClaims(String token) {
		return Jwts.parser().setSigningKey(jwtSettings.getSigningKey()).parseClaimsJws(token).getBody();
	}
	
}
