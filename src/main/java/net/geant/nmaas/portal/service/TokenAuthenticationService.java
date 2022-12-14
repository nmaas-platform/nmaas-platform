package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TokenAuthenticationService {

	private static final String AUTH_HEADER = "Authorization";
	private static final String AUTH_METHOD = "Bearer";
	
	private JWTTokenService jwtTokenService;

	@Autowired
	public TokenAuthenticationService(JWTTokenService jwtTokenService) {
		this.jwtTokenService = jwtTokenService;
	}

	public Authentication getAuthentication(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader(AUTH_HEADER);
		if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AUTH_METHOD + " "))
			throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");

		String token = authHeader.substring(AUTH_METHOD.length() + 1);

		String username = jwtTokenService.getClaims(token).getSubject();
		Object scopes = jwtTokenService.getClaims(token).get("scopes");

		Set<SimpleGrantedAuthority> authorities = null;

		if (scopes instanceof List<?>) {
			authorities = new HashSet<>();
			for (Map<String, String> authority : (List<Map<String, String>>) scopes)
				for (String role : authority.values())
					authorities.add(new SimpleGrantedAuthority(role.substring(role.indexOf(':') + 1)));
		}

		return new UsernamePasswordAuthenticationToken(username, null, authorities);
	}

}
