package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.apache.commons.lang.RandomStringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApi.ApiVersion;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interacts with a remote GitLab repository instance through a REST API in order to upload a set of application
 * configuration files prepared for new application/tool deployment.
 * It is assumed that valid address and credentials of the repository API are provided through properties file.
 */
@Component
@Profile("env_kubernetes")
public class GitLabConfigUploader implements ConfigurationFileTransferProvider {

    private static final String GROUPS_PATH_PREFIX = "groups";
    private static final int DEFAULT_DOMAIN_LIMIT_ON_CREATED_PROJECTS = 100;
    private static final String DEFAULT_CLIENT_EMAIL_DOMAIN = "nmaas.geant.net";
    private static final String DEFAULT_BRANCH_FOR_COMMIT = "master";
    private static final int PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL = 40;
    private static final int PROJECT_MEMBER_DEVELOPER_ACCESS_LEVEL = 30;

    private NmServiceRepositoryManager serviceRepositoryManager;
    private NmServiceConfigFileRepository configurations;
    private GitLabManager gitLabManager;

    @Autowired
    GitLabConfigUploader(NmServiceRepositoryManager serviceRepositoryManager,
                         NmServiceConfigFileRepository configurations,
                         GitLabManager gitLabManager) {
        this.serviceRepositoryManager = serviceRepositoryManager;
        this.configurations = configurations;
        this.gitLabManager = gitLabManager;
    }

    private GitLabApi gitlab;

    /**
     * Uploads a set of configuration files to a new GitLab repository dedicated for the client requesting the deployment.
     * If an account for this client does not yet exists it is created.
     * Information on how to access the repository is stored in {@link GitLabProject} object.
     *
     * @param deploymentId unique identifier of service deployment
     * @param configIds list of identifiers of configuration files that should be loaded from database and uploaded to the git repository
     * @throws InvalidDeploymentIdException if a service for given deployment identifier could not be found in database
     * @throws ConfigFileNotFoundException if any of the configuration files for which an identifier is given could not be found in database
     * @throws FileTransferException if any error occurs during communication with the git repository API
     */
    @Override
    public void transferConfigFiles(Identifier deploymentId, List<String> configIds, boolean configFileRepositoryRequired) {
        if(configFileRepositoryRequired){
            String domain = serviceRepositoryManager.loadDomain(deploymentId);
            gitlab = new GitLabApi(ApiVersion.V4, gitLabManager.getGitLabApiUrl(), gitLabManager.getGitLabApiToken());
            String gitLabPassword = generateRandomPassword();
            Integer gitLabUserId = createUser(domain, deploymentId, gitLabPassword);
            Integer gitLabGroupId = getOrCreateGroupWithMemberForUserIfNotExists(gitLabUserId, domain);
            Integer gitLabProjectId = createProjectWithinGroupWithMember(gitLabGroupId, gitLabUserId, deploymentId);
            addRepositoryAccessUserToProject(gitLabProjectId);
            GitLabProject project = project(deploymentId, gitLabUserId, gitLabPassword, gitLabProjectId);
            serviceRepositoryManager.updateGitLabProject(deploymentId, project);
            uploadConfigFilesToProject(gitLabProjectId, configIds);
        }
    }

    @Override
    public void updateConfigFiles(Identifier deploymentId, List<String> configIds, boolean configFileRepositoryRequired){
        if(configFileRepositoryRequired){
            gitlab = new GitLabApi(ApiVersion.V4, gitLabManager.getGitLabApiUrl(), gitLabManager.getGitLabApiToken());
            GitLabProject project = serviceRepositoryManager.loadService(deploymentId).getGitLabProject();
            uploadUpdateConfigFilesToProject(project.getProjectId(), configIds);
        }
    }

    private Integer createUser(String domain, Identifier deploymentId, String password) {
        try {
            return gitlab.getUserApi().createUser(createStandardUser(domain, deploymentId), password, limitOnProjects()).getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    private User createStandardUser(String domain, Identifier deploymentId) {
        User user = new User();
        user.setName(name(domain, deploymentId));
        String userName = userName(domain, deploymentId);
        user.setUsername(userName);
        user.setEmail(userEmail(userName));
        user.setCanCreateGroup(false);
        return user;
    }

    private int limitOnProjects() {
        return DEFAULT_DOMAIN_LIMIT_ON_CREATED_PROJECTS;
    }

    private String generateRandomPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    private String name(String domain, Identifier deploymentId) {
        return domain + " (" + deploymentId + ")";
    }

    private String userName(String domain, Identifier deploymentId) {
        return domain + "-" + deploymentId;
    }

    private String userEmail(String username) {
        return username + "@" + DEFAULT_CLIENT_EMAIL_DOMAIN;
    }

    private Integer getOrCreateGroupWithMemberForUserIfNotExists(Integer gitLabUserId, String domain) {
        try {
            return gitlab.getGroupApi().getGroup(groupPath(domain)).getId();
        } catch (GitLabApiException e) {
            if (statusIsDifferentThenNotFound(e.getHttpStatus()))
                throw new FileTransferException("" + e.getMessage());
            try {
                gitlab.getGroupApi().addGroup(groupName(domain), groupPath(domain));
                Integer groupId = gitlab.getGroupApi().getGroup(groupPath(domain)).getId();
                gitlab.getGroupApi().addMember(groupId, gitLabUserId, fullAccessCode());
                return groupId;
            } catch (GitLabApiException e1) {
                throw new FileTransferException("" + e1.getMessage());
            }
        }
    }

    private String groupName(String domain) {
        return domain;
    }

    private String groupPath(String domain) {
        return GROUPS_PATH_PREFIX + "-" + groupName(domain);
    }

    private boolean statusIsDifferentThenNotFound(int httpStatus) {
        return httpStatus != HttpStatus.NOT_FOUND.value();
    }

    private Integer fullAccessCode() {
        return PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL;
    }

    private Integer createProjectWithinGroupWithMember(Integer groupId, Integer userId, Identifier deploymentId) {
        try {
            Project project = gitlab.getProjectApi().createProject(groupId, projectName(deploymentId));
            gitlab.getProjectApi().addMember(project.getId(), userId, fullAccessCode());
            return project.getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException("" + e.getMessage() + " " + e.getReason());
        }
    }

    private void addRepositoryAccessUserToProject(Integer projectId) {
        try{
            gitlab.getProjectApi().addMember(projectId, getUserIdByUsername(defaultRepositoryAccessUsername()), PROJECT_MEMBER_DEVELOPER_ACCESS_LEVEL);
        } catch(GitLabApiException e){
            throw new FileTransferException("" + e.getMessage() + e.getReason());
        }
    }

    private String projectName(Identifier deploymentId) {
        return deploymentId.value();
    }

    private GitLabProject project(Identifier deploymentId, Integer gitLabUserId, String gitLabPassword, Integer gitLabProjectId) {
        try {
            String gitLabUser = getUser(gitLabUserId);
            String gitLabRepoUrl = getHttpUrlToRepo(gitLabProjectId);
            return new GitLabProject(deploymentId, gitLabUser, gitLabPassword, gitLabRepoUrl, gitLabProjectId);
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    private String defaultRepositoryAccessUsername(){
        return gitLabManager.getGitLabRepositoryAccessUsername();
    }

    private Integer getUserIdByUsername(String username) throws GitLabApiException{
        return gitlab.getUserApi().getUser(username).getId();
    }

    private String getUser(Integer gitLabUserId) throws GitLabApiException {
        return gitlab.getUserApi().getUser(gitLabUserId).getUsername();
    }

    String getHttpUrlToRepo(Integer gitLabProjectId) throws GitLabApiException {
        String[] urlFromGitlabApiParts = gitlab.getProjectApi().getProject(gitLabProjectId).getHttpUrlToRepo().split("//");
        String[] urlParts = urlFromGitlabApiParts[1].split("/");
        urlParts[0] = gitLabManager.getGitlabServer() + ":" + gitLabManager.getGitlabPort();
        return urlFromGitlabApiParts[0] + "//" + String.join("/", urlParts);
    }

    private void uploadConfigFilesToProject(Integer gitLabProjectId, List<String> configIds) {
        for (String configId : configIds) {
            NmServiceConfiguration configuration = loadConfigurationFromDatabase(configId);
            RepositoryFile file = committedFile(configuration);
            try {
                gitlab.getRepositoryFileApi().createFile(file, gitLabProjectId, commitBranch(), commitMessage(configuration.getConfigFileName()));
            } catch (GitLabApiException e) {
                throw new FileTransferException("Could not commit file " + configuration.getConfigFileName() + " due to exception: " + e.getMessage());
            }
        }
    }

    private void uploadUpdateConfigFilesToProject(Integer gitLabProjectId, List<String> configIds){
        for (String configId : configIds) {
            NmServiceConfiguration configuration = loadConfigurationFromDatabase(configId);
            RepositoryFile file = committedFile(configuration);
            try {
                gitlab.getRepositoryFileApi().updateFile(file, gitLabProjectId, commitBranch(), updateCommitMessage(configuration.getConfigFileName()));
            } catch (GitLabApiException e) {
                throw new FileTransferException("Could not commit file " + configuration.getConfigFileName() + " due to exception: " + e.getMessage());
            }
        }
    }

    private NmServiceConfiguration loadConfigurationFromDatabase(String configId) {
        return configurations.findByConfigId(configId)
                .orElseThrow(() -> new ConfigFileNotFoundException("Required configuration file not found in repository"));
    }

    private RepositoryFile committedFile(NmServiceConfiguration configuration) {
        RepositoryFile file = new RepositoryFile();
        file.setFilePath(configuration.getConfigFileName());
        file.setContent(configuration.getConfigFileContent());
        return file;
    }

    private String commitBranch() {
        return DEFAULT_BRANCH_FOR_COMMIT;
    }

    private String commitMessage(String fileName) {
        return "Initial commit of " + fileName;
    }

    private String updateCommitMessage(String fileName) {
        return "Update commit of " + fileName;
    }

    void setGitlab(GitLabApi gitlab) {
        this.gitlab = gitlab;
    }
}
