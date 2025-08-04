package net.geant.nmaas.portal.api.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.api.security.exceptions.TokenAuthenticationException;
import net.geant.nmaas.portal.service.TokenAuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 * Security filter for stateless UUID-based authentication.
 * This filter activates only for requests matching any of the configured path predicates,
 * and only if a UUID-style token is present and recognized by the {@link TokenAuthenticationService}.
 */
@Slf4j
public class StatelessUUIDAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final List<Predicate<HttpServletRequest>> pathPredicates;
    private final TokenAuthenticationService tokenService;


    /**
     * Constructs a filter that processes requests matching any of the provided path predicates.
     *
     * @param pathPredicates list of predicates to determine if the request should be authenticated
     * @param tokenService   service responsible for UUID token parsing and authentication
     */
    public StatelessUUIDAuthenticationFilter(List<Predicate<HttpServletRequest>> pathPredicates, TokenAuthenticationService tokenService) {
        super("/**");
        this.pathPredicates = pathPredicates;
        this.tokenService = tokenService;
    }

    /**
     * Determines whether the request should be authenticated based on token presence and path match.
     *
     * @return true if UUID token is available and the path matches one of the predicates
     */
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (tokenService.isUUIDAuthorization(request)) {
            boolean match = pathPredicates.stream().anyMatch(p -> p.test(request));
            log.debug("UUID FILTER – requiresAuthentication={} for URI: {}", match, request.getRequestURI());

            return match;
        } else {
            return false;
        }
    }

    /**
     * Attempts to authenticate the request using a UUID token.
     *
     * @return authenticated principal, or null if token type is not UUID
     * @throws TokenAuthenticationException if token is invalid
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication auth = tokenService.getAuthenticationForUUID(request);
            if (auth == null) {
                throw new TokenAuthenticationException("UUID Token missing or invalid");
            }
            return auth;
        } catch (AuthenticationMethodNotSupportedException ex) {
            log.debug("UUID FILTER – skipping (not UUID token): {}", ex.getMessage());
            return null;
        } catch (Exception ex) {
            throw new TokenAuthenticationException("UUID Token invalid: " + ex.getMessage(), ex);
        }
    }

    /**
     * Called on successful authentication. Sets the authentication context and proceeds with the request.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }

    /**
     * Called on authentication failure. Clears the context and delegates to the failure handler.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.warn("UUID FILTER – authentication failed for URI: {}", request.getRequestURI());
        SecurityContextHolder.clearContext();
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}
