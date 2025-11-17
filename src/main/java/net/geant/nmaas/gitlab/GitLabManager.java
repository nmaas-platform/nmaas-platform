package net.geant.nmaas.gitlab;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper;
import org.apache.commons.lang3.Validate;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.RepositoryFileApi;
import org.gitlab4j.api.UserApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
@Slf4j
public class GitLabManager {

    private static final String GITLAB_API_NAMESPACE = "/api/v4";

    @Getter
    @Value("${gitlab.apiUrl}")
    private String gitLabApiUrl;

    @Value("${gitlab.token}")
    private String gitLabToken;

    @Getter
    @Setter
    @Value("${gitlab.sharedInstance}")
    private boolean sharedInstance;

    @Getter
    @Setter
    @Value("${gitlab.topLevelGroupName}")
    private String topLevelGroupName;

    @Getter
    private Optional<String> topLevelGroupPath = Optional.empty();

    public GroupApi groups() {
        return api().getGroupApi();
    }

    public ProjectApi projects() {
        return api().getProjectApi();
    }

    public UserApi users() {
        return api().getUserApi();
    }

    public RepositoryApi repository() {
        return api().getRepositoryApi();
    }

    public RepositoryFileApi repositoryFiles() {
        return api().getRepositoryFileApi();
    }

    private GitLabApi api() {
        return new GitLabApi(GitLabApi.ApiVersion.V4, getApiUrl(), this.gitLabToken);
    }

    String getApiUrl() {
        return gitLabApiUrl.endsWith(GITLAB_API_NAMESPACE)
                ? gitLabApiUrl.substring(0, gitLabApiUrl.length() - GITLAB_API_NAMESPACE.length())
                : gitLabApiUrl;
    }

    public void validateGitLabInstance() {
        Validate.isTrue(this.gitLabApiUrl != null && !this.gitLabApiUrl.isEmpty(), "GitLab api URL is null or empty");
        Validate.isTrue(this.gitLabToken != null && !this.gitLabToken.isEmpty(), "GitLab token is null or empty");
        try {
            api().getVersion();
            log.trace("GitLab instance is running");
        } catch (GitLabApiException e) {
            throw new GitLabInvalidConfigurationException("GitLab instance doesn't respond -> " + e.getMessage());
        }
        if (sharedInstance){
            topLevelGroupPath = GitLabConfigHelper.sanitizeGroupPathSegment(topLevelGroupName);
        }
    }

}
