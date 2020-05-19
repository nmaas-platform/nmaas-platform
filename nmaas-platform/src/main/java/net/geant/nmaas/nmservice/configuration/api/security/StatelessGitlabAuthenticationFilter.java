package net.geant.nmaas.nmservice.configuration.api.security;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Log4j2
public class StatelessGitlabAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String GITLAB_TOKEN_HEADER = "X-Gitlab-Token";

    private final GitLabProjectRepository repository;

    // https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/authentication/AbstractAuthenticationProcessingFilter.html#AbstractAuthenticationProcessingFilter-java.lang.String-
    public StatelessGitlabAuthenticationFilter(String defaultFilterProcessesUrl, GitLabProjectRepository repository) {
        super(defaultFilterProcessesUrl);
        this.repository = repository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // obtain token
        String incomingToken = request.getHeader(GITLAB_TOKEN_HEADER);
        String uri = request.getRequestURI(); // obtain uri
        log.info("GitlabTokenAuthFilter for URI: " + uri);
        String[] element = uri.split("/");
        String id = element[element.length - 1]; // take last element in uri path
        Optional<GitLabProject> candidate = this.repository.findByWebhookId(id);
        if(!candidate.isPresent()) {
            throw new RuntimeException("GitLabProjectNotAvailable");
        }
        String projectToken = candidate.get().getWebhookToken();
        if(incomingToken.equals(projectToken)) {
            return new UsernamePasswordAuthenticationToken(id, null, null);
        } else {
            return null; // authentication still in progress
        }
    }
}
