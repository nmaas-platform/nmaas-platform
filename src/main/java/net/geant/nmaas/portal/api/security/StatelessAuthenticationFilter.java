package net.geant.nmaas.portal.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.security.config.SkipPathRequestMatcher;
import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import net.geant.nmaas.portal.service.TokenAuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

@Slf4j
public class StatelessAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private final TokenAuthenticationService tokenService;

	public StatelessAuthenticationFilter(SkipPathRequestMatcher skipPathRequestMatcher,
										 TokenAuthenticationService tokenService) {
		super(skipPathRequestMatcher);
		this.tokenService = tokenService;
	}

	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return tokenService.isJWTAuthorization(request);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		try {
			Authentication auth = tokenService.getAuthenticationForJWT(request);
			if (auth == null) {
				throw new TokenAuthenticationException("JWT Token missing or invalid");
			}
			return auth;
		} catch (Exception ex) {
			throw new TokenAuthenticationException("JWT Token invalid: " + ex.getMessage(), ex);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
											FilterChain chain, Authentication authResult)
			throws IOException, ServletException {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authResult);
		SecurityContextHolder.setContext(context);
		chain.doFilter(request, response);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
											  AuthenticationException failed)
			throws IOException, ServletException {
		log.warn("JWT FILTER – authentication failed for URI: {}", request.getRequestURI());
		SecurityContextHolder.clearContext();
		getFailureHandler().onAuthenticationFailure(request, response, failed);
	}
}