package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
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
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("conf_repo")
public class GitLabConfigUploader implements ConfigurationFileTransferProvider {

    private static final String GROUPS_PATH_PREFIX = "groups";
    private static final int DEFAULT_DOMAIN_LIMIT_ON_CREATED_PROJECTS = 100;
    private static final String DEFAULT_CLIENT_EMAIL_DOMAIN = "nmaas.geant.net";
    private static final String DEFAULT_BRANCH_FOR_COMMIT = "master";
    private static final int PROJECT_MEMBER_MASTER_ACCESS_LEVEL = 40;

    static final String DEFAULT_REPO_CLONE_USER = "nmaas-conf-automation";

    private NmServiceRepositoryManager serviceRepositoryManager;
    private NmServiceConfigFileRepository configurations;
    private GitLabManager gitLabManager;
    private KClusterDeploymentManager kClusterDeployment;

    @Autowired
    public GitLabConfigUploader(NmServiceRepositoryManager serviceRepositoryManager, NmServiceConfigFileRepository configurations,
                                GitLabManager gitLabManager, KClusterDeploymentManager kClusterDeployment) {
        this.serviceRepositoryManager = serviceRepositoryManager;
        this.configurations = configurations;
        this.gitLabManager = gitLabManager;
        this.kClusterDeployment = kClusterDeployment;
    }

    private GitLabApi gitlab;

    /**
     * Uploads a set of configuration files to a new GitLab repository dedicated for the client requesting the deployment.
     * If an account for this client does not yet exists it is created.
     * Information on how to access the repository (e.g. perform "git clone") is stored in {@link GitLabProject} object.
     *
     * @param deploymentId unique identifier of service deployment
     * @param configIds list of identifiers of configuration files that should be loaded from database and uploaded to the git repository
     * @throws InvalidDeploymentIdException if a service for given deployment identifier could not be found in database
     * @throws ConfigFileNotFoundException if any of the configuration files for which an identifier is given could not be found in database
     * @throws FileTransferException if any error occurs during communication with the git repository API
     */
    @Override
    public void transferConfigFiles(Identifier deploymentId, List<String> configIds)
            throws InvalidDeploymentIdException, ConfigFileNotFoundException, FileTransferException {
        String domain = serviceRepositoryManager.loadDomain(deploymentId);
        gitlab = new GitLabApi(ApiVersion.V4, gitLabManager.getGitLabApiUrl(), gitLabManager.getGitLabApiToken());
        String gitLabPassword = generateRandomPassword();
        Integer gitLabUserId = createUser(domain, deploymentId, gitLabPassword);
        Integer gitLabGroupId = getOrCreateGroupWithMemberForUserIfNotExists(gitLabUserId, domain);
        Integer gitLabProjectId = createProjectWithinGroupWithMember(gitLabGroupId, gitLabUserId, deploymentId);
        GitLabProject project = project(deploymentId, gitLabUserId, gitLabPassword, gitLabProjectId);
        serviceRepositoryManager.updateGitLabProject(deploymentId, project);
        uploadConfigFilesToProject(gitLabProjectId, configIds);
    }

    private Integer createUser(String domain, Identifier deploymentId, String password) throws FileTransferException {
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

    private Integer getOrCreateGroupWithMemberForUserIfNotExists(Integer gitLabUserId, String domain) throws FileTransferException {
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
        return PROJECT_MEMBER_MASTER_ACCESS_LEVEL;
    }

    private Integer createProjectWithinGroupWithMember(Integer groupId, Integer userId, Identifier deploymentId) throws FileTransferException {
        try {
            Project project = gitlab.getProjectApi().createProject(groupId, projectName(deploymentId));
            gitlab.getProjectApi().addMember(
                    project.getId(),
                    userId,
                    fullAccessCode());
            gitlab.getProjectApi().addMember(
                    project.getId(),
                    gitlab.getUserApi().getUser(DEFAULT_REPO_CLONE_USER).getId(),
                    fullAccessCode());
            return project.getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException("" + e.getMessage() + " " + e.getReason());
        }
    }

    private String projectName(Identifier deploymentId) {
        return deploymentId.value();
    }

    GitLabProject project(Identifier deploymentId, Integer gitLabUserId, String gitLabPassword, Integer gitLabProjectId)
            throws FileTransferException {
        try {
            String gitLabUser = getUser(gitLabUserId);
            String gitLabRepoUrl = getHttpUrlToRepo(gitLabProjectId);
            String gitCloneUrl = getGitCloneUrl(gitLabUser, gitLabPassword, gitLabRepoUrl);
            return new GitLabProject(deploymentId, gitLabUser, gitLabPassword, gitLabRepoUrl, gitCloneUrl);
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    String getGitCloneUrl(String gitLabUser, String gitLabPassword, String gitLabRepoUrl) {
        return kClusterDeployment.getUseInClusterGitLabInstance()
                        ? generateCompleteGitCloneUrl(DEFAULT_REPO_CLONE_USER, gitLabRepoUrl)
                        : generateCompleteGitCloneUrl(gitLabUser, gitLabPassword, gitLabRepoUrl);
    }

    private String getUser(Integer gitLabUserId) throws GitLabApiException {
        return gitlab.getUserApi().getUser(gitLabUserId).getUsername();
    }

    private String getHttpUrlToRepo(Integer gitLabProjectId) throws GitLabApiException {
        return gitlab.getProjectApi().getProject(gitLabProjectId).getHttpUrlToRepo();
    }

    private String generateCompleteGitCloneUrl(String gitLabUser, String gitLabRepoUrl) {
        String[] urlParts = gitLabRepoUrl.split("//");
        return urlParts[0] + "//" + gitLabUser + "@" + urlParts[1];
    }

    private String generateCompleteGitCloneUrl(String gitLabUser, String gitLabPassword, String gitLabRepoUrl) {
        String[] urlParts = gitLabRepoUrl.split("//");
        return urlParts[0] + "//" + gitLabUser + ":" + gitLabPassword + "@" + urlParts[1];
    }

    private void uploadConfigFilesToProject(Integer gitLabProjectId, List<String> configIds)
            throws FileTransferException, ConfigFileNotFoundException {
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

    private NmServiceConfiguration loadConfigurationFromDatabase(String configId) throws ConfigFileNotFoundException {
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
}
