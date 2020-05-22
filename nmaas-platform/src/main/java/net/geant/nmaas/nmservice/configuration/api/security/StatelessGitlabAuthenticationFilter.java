package net.geant.nmaas.nmservice.configuration.api.security;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        log.info("GitlabTokenAuthFilter for URI: " + uri);
        if(incomingToken == null) {
            throw new GitlabTokenAuthenticationException("No token provided");
        }
        String[] element = uri.split("/");
        // take last element in uri path as webhook id
        String id = element[element.length - 1];
        log.info("Webhook id: " + id);
        Optional<GitLabProject> candidate = this.repository.findByWebhookId(id);
        if(!candidate.isPresent()) {
            throw new RuntimeException("GitLabProjectNotAvailable");
        }
        String projectToken = candidate.get().getWebhookToken();
        if(incomingToken.equals(projectToken)) {
            return new UsernamePasswordAuthenticationToken(id, null, null);
        } else {
            throw new GitlabTokenAuthenticationException("Invalid token");
//            return null; // authentication still in progress
        }
    }
}
