package net.geant.nmaas.portal.auth.basic;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import org.apache.commons.lang.StringUtils;
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
@Slf4j
public class TokenAuthenticationService {

	private final static String AUTH_HEADER="Authorization";
	private final static String AUTH_METHOD="Bearer";
	
	@Autowired
	JWTTokenService tokenService;
	
	public TokenAuthenticationService(JWTTokenService jwtTokenService) {
		this.tokenService = jwtTokenService;
	}

	public Authentication getAuthentication(HttpServletRequest httpRequest) {
		try {
			String authHeader = httpRequest.getHeader(AUTH_HEADER);
			if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(AUTH_METHOD + " "))
				throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");

			String token = authHeader.substring(AUTH_METHOD.length() + 1);

			String username = tokenService.getClaims(token).getSubject();
			Object scopes = tokenService.getClaims(token).get("scopes");

			Set<SimpleGrantedAuthority> authorities = null;

			if (scopes != null && scopes instanceof List<?>) {
				authorities = new HashSet<SimpleGrantedAuthority>();
				for (Map<String, String> authority : (List<Map<String, String>>) scopes)
					for (String role : authority.values())
						authorities.add(new SimpleGrantedAuthority(role.substring(role.indexOf(":") + 1)));
			}

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

			return authentication;
		} catch(RuntimeException e) {
			long timestamp = System.currentTimeMillis();
			log.info("JWT Token expired at - " + timestamp, e.getMessage());
			return null;
		}
	}

}
