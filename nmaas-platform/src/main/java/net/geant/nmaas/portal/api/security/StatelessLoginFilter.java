package net.geant.nmaas.portal.api.security;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StringUtils;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.api.security.exceptions.BasicAuthenticationException;

public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

	private final static String AUTH_HEADER="Authorization";
	private final static String AUTH_METHOD="Basic";
	
	UserDetailsService userDetailsService;
	

	public StatelessLoginFilter(String defaultFilterProcessesUrl, UserDetailsService userDetailsService) {
		super(defaultFilterProcessesUrl);
		this.userDetailsService = userDetailsService;
	}


	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		
		String authHeader = httpRequest.getHeader(AUTH_HEADER);
		if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AUTH_METHOD + " "))
			throw new AuthenticationMethodNotSupportedException(AUTH_HEADER + " contains unsupported method.");
		
		String credentials = authHeader.substring(AUTH_METHOD.length()+1);
		if(StringUtils.isEmpty(credentials))
			throw new BasicAuthenticationException("Missing credentials");
		
		
		Base64.getDecoder().decode(credentials);
		credentials = new String(Base64.getDecoder().decode(credentials));
		if(StringUtils.isEmpty(credentials))
			throw new BasicAuthenticationException("Missing credentials");
		
		String[] userdata = credentials.split(":");
		
		if(userdata.length < 1 || userdata.length > 2)
			throw new BasicAuthenticationException("Invalid user credentials format");
		String username = userdata[0];
		String password = userdata[1];
		
		UserDetails user = userDetailsService.loadUserByUsername(username);
		if(password != user.getPassword())
			throw new BasicAuthenticationException("Invalid credentials.");	
						
		UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
				
		return userToken;
	}

	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		super.unsuccessfulAuthentication(request, response, failed);
	}
}
