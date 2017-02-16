package net.geant.nmaas.portal.api.security;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import net.geant.nmaas.portal.persistent.entity.User;

@Service
public class JWTTokenService {

	@Autowired
	JWTSettings jwtSettings;
	
	public JWTTokenService() {
	}
	
	protected JWTTokenService(JWTSettings jwtSettings) {
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
		.claim("scopes", user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority())).collect(Collectors.toList()))
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
					.claim("scopes", Arrays.asList(JWTSettings.Scopes.REFRESH_TOKEN))
					.signWith(SignatureAlgorithm.HS512, jwtSettings.getSigningKey())
					.compact();
	}
	
	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch(JwtException e) {
			return false;
		}
	}
	
	public boolean validateRefreshToken(String token) {
		try {
			Claims claims = getClaims(token);
			Object scope = claims.get("scopes");
			if(scope instanceof List<?>) {
				if(((List<String>)scope).contains(JWTSettings.Scopes.REFRESH_TOKEN.name()))
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
