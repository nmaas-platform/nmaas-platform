package net.geant.nmaas.portal.auth.basic;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.service.UserService;

@Service
public class TokenAuthenticationService {

	private final static String AUTH_HEADER="Authorization";
	private final static String AUTH_METHOD="Bearer";
	
	@Autowired
	JWTTokenService tokenService;
	
	public TokenAuthenticationService(JWTTokenService jwtTokenService) {
		this.tokenService = jwtTokenService;
	}

	public Authentication getAuthentication(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader(AUTH_HEADER);
		if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(AUTH_METHOD + " "))
			throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
		
		String token = authHeader.substring(AUTH_METHOD.length()+1);
		
		String username = tokenService.getClaims(token).getSubject();
		Object scopes = tokenService.getClaims(token).get("scopes");
		
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, null);

		return authentication;
	}

}
