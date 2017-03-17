package net.geant.nmaas.portal.auth.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jnr.ffi.Struct.key_t;
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
		
		List<SimpleGrantedAuthority> authorities = null;
		
		if (scopes != null && scopes instanceof List<?>) {
			authorities = new ArrayList<SimpleGrantedAuthority>();
			for(Map<String,String> authority : (List<Map<String,String>>)scopes)
				for(String role : authority.values())
					authorities.add(new SimpleGrantedAuthority(role));
		} 
			
		
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

		return authentication;
	}

}
