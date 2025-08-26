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

import java.io.IOException;

/**
 * Security filter for stateless JWT-based authentication.
 * This filter intercepts incoming HTTP requests, checks for a valid JWT token,
 * and authenticates the user by delegating to {@link TokenAuthenticationService}.
 * It only processes requests that are not matched by {@link SkipPathRequestMatcher}.
 */
@Slf4j
public class StatelessAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final TokenAuthenticationService tokenService;

    /**
     * Constructs the filter with a custom path matcher and a JWT token service.
     *
     * @param skipPathRequestMatcher matcher that defines which paths to skip
     * @param tokenService           service responsible for parsing and validating JWT tokens
     */
    public StatelessAuthenticationFilter(SkipPathRequestMatcher skipPathRequestMatcher,
                                         TokenAuthenticationService tokenService) {
        super(skipPathRequestMatcher);
        this.tokenService = tokenService;
    }


    /**
     * Determines whether this request should be authenticated.
     * Returns true if a valid JWT token is present in the request.
     */
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return tokenService.isJWTAuthorization(request);
    }


    /**
     * Attempts to authenticate the request using a JWT token.
     *
     * @throws TokenAuthenticationException if token is missing or invalid
     */
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

    /**
     * Called when authentication is successful. Sets the authenticated context and proceeds with the filter chain.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }


    /**
     * Called when authentication fails. Clears the security context and delegates to failure handler.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {
        log.warn("JWT FILTER – authentication failed for URI: {}", request.getRequestURI());
        SecurityContextHolder.clearContext();
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}