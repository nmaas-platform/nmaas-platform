package net.geant.nmaas.externalservices.inventory.gitlab;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryFileApi;
import org.gitlab4j.api.UserApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkArgument;

@Component
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
@Log4j2
public class GitLabManager {

    @Value("${gitlab.address}")
    private String gitLabAddress;

    @Value("${gitlab.port}")
    private Integer gitLabPort;

    @Value("${gitlab.token}")
    private String gitLabToken;

    public String getGitlabServer() {
        return this.gitLabAddress;
    }

    public int getGitlabPort() {
        return this.gitLabPort;
    }

    public GroupApi groups() {
        return api().getGroupApi();
    }

    public ProjectApi projects() {
        return api().getProjectApi();
    }

    public UserApi users() {
        return api().getUserApi();
    }

    public RepositoryFileApi repositoryFiles() {
        return api().getRepositoryFileApi();
    }

    private GitLabApi api() {
        return new GitLabApi(GitLabApi.ApiVersion.V4, getApiUrl(), this.gitLabToken);
    }

    private String getApiUrl(){
        return String.format("http://%s:%d", this.gitLabAddress, this.gitLabPort);
    }

    public void validateGitLabInstance() {
        checkArgument(this.gitLabAddress != null && !this.gitLabAddress.isEmpty(), "GitLab address is null or empty");
        checkArgument(this.gitLabPort != null, "GitLab port is null");
        checkArgument(this.gitLabToken != null && !this.gitLabToken.isEmpty(), "GitLab token is null or empty");
        try {
            api().getVersion();
            log.trace("GitLab instance is running");
        } catch (GitLabApiException e){
            throw new GitLabInvalidConfigurationException("GitLab instance is not running -> " + e.getMessage());
        }
    }

}
