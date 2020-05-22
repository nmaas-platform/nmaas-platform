package net.geant.nmaas.nmservice.configuration.api.security;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Log4j2
public class StatelessGitlabAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String GITLAB_TOKEN_HEADER = "X-Gitlab-Token";

    private final GitLabProjectRepository repository;

    public StatelessGitlabAuthenticationFilter(String defaultFilterProcessesUrl, GitLabProjectRepository repository) {
        super(defaultFilterProcessesUrl);
        this.repository = repository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // obtain token
        String incomingToken = request.getHeader(GITLAB_TOKEN_HEADER);
        // obtain uri
        String uri = request.getRequestURI();
        log.debug("GitlabTokenAuthFilter for URI: " + uri);
        if(incomingToken == null) {
            throw new GitlabTokenAuthenticationException("No token provided");
        }
        String[] element = uri.split("/");
        // take last element in uri path as webhook id
        String id = element[element.length - 1];
        log.debug("Webhook id: " + id);
        // find matching gitlab project in database
        Optional<GitLabProject> candidate = this.repository.findByWebhookId(id);
        if(!candidate.isPresent()) {
            throw new GitlabTokenAuthenticationException("No matching gitlab project found");
        }
        String projectToken = candidate.get().getWebhookToken();
        if(incomingToken.equals(projectToken)) {
            return new UsernamePasswordAuthenticationToken(id, null, null);
        } else {
            throw new GitlabTokenAuthenticationException("Invalid webhook token");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        log.debug("Authenticated");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
        SecurityContextHolder.clearContext();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        log.debug("Authentication unsuccessful");
        SecurityContextHolder.clearContext();
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }

}
