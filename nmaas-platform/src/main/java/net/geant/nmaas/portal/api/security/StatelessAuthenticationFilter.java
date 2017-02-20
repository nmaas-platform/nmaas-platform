package net.geant.nmaas.portal.api.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import net.geant.nmaas.portal.auth.basic.TokenAuthenticationService;

public class StatelessAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	final static Logger log = LogManager.getLogger(StatelessAuthenticationFilter.class);
		
	TokenAuthenticationService tokenService;

	public StatelessAuthenticationFilter(RequestMatcher skipPaths, TokenAuthenticationService tokenService) {
		super(skipPaths);
		this.tokenService = tokenService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		log.debug("Request: " + request.getRequestURI());
		try {
			return tokenService.getAuthentication(request);
		} catch(Exception e) {
			throw new TokenAuthenticationException("Token is not valid");
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		//super.successfulAuthentication(request, response, chain, authResult);
		log.debug("Authentication: " + authResult);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authResult);
		SecurityContextHolder.setContext(context);
		chain.doFilter(request, response);
		SecurityContextHolder.clearContext();
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		//super.unsuccessfulAuthentication(request, response, failed);
		log.debug("Authentication unsuccessful");
		SecurityContextHolder.clearContext();
		getFailureHandler().onAuthenticationFailure(request, response, failed);
	}
	
}
