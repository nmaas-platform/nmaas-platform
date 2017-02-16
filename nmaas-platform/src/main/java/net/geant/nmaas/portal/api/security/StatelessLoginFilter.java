package net.geant.nmaas.portal.api.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.geant.nmaas.portal.api.auth.UserLogin;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;

public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

	public StatelessLoginFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		
		if(!HttpMethod.POST.name().equals(request.getMethod()))
			throw new AuthenticationMethodNotSupportedException("Request authentication method is not supported. ");
		
		ObjectMapper objectMapper = new ObjectMapper();
		UserLogin userLogin = objectMapper.readValue(request.getInputStream(), UserLogin.class);
		
		if(StringUtils.isEmpty(userLogin.getUsername()) || StringUtils.isEmpty(userLogin.getPassword()))
			throw new AuthenticationServiceException("Missing credentials");
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword());
				
		return getAuthenticationManager().authenticate(token);
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
