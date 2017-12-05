package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesNmServiceRepositoryManager;
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
import org.springframework.beans.factory.annotation.Value;
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
@Profile({"kubernetes"})
public class GitLabConfigUploader implements ConfigurationFileTransferProvider {

    private static final String GROUPS_PATH_PREFIX = "groups";
    private static final int DEFAULT_CLIENT_LIMIT_ON_CREATED_PROJECTS = 10;
    private static final String DEFAULT_CLIENT_EMAIL_DOMAIN = "nmaas.geant.net";
    private static final String DEFAULT_BRANCH_FOR_COMMIT = "master";
    private static final int PROJECT_MEMBER_MASTER_ACCESS_LEVEL = 40;

    @Autowired
    private KubernetesNmServiceRepositoryManager serviceRepositoryManager;
    @Autowired
    private NmServiceConfigFileRepository configurations;

    @Value("${gitlab.api.url}")
    private String gitLabApiUrl;
    @Value("${gitlab.api.token}")
    private String gitLabApiToken;

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
        Identifier clientId = serviceRepositoryManager.loadClientId(deploymentId);
        gitlab = new GitLabApi(ApiVersion.V4, gitLabApiUrl, gitLabApiToken);
        String gitLabPassword = generateRandomPassword();
        Integer gitLabUserId = createUser(clientId, deploymentId, gitLabPassword);
        Integer gitLabGroupId = getOrCreateGroupWithMemberForUserIfNotExists(gitLabUserId, clientId);
        Integer gitLabProjectId = createProjectWithinGroupWithMember(gitLabGroupId, gitLabUserId, deploymentId);
        GitLabProject project = project(deploymentId, gitLabUserId, gitLabPassword, gitLabProjectId);
        serviceRepositoryManager.updateGitLabProject(deploymentId, project);
        uploadConfigFilesToProject(gitLabProjectId, configIds);
    }

    private Integer createUser(Identifier clientId, Identifier deploymentId, String password) throws FileTransferException {
        try {
            return gitlab.getUserApi().createUser(createStandardUser(clientId, deploymentId), password, limitOnProjects()).getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    private User createStandardUser(Identifier clientId, Identifier deploymentId) {
        User user = new User();
        user.setName(name(clientId, deploymentId));
        String userName = userName(clientId, deploymentId);
        user.setUsername(userName);
        user.setEmail(userEmail(userName));
        user.setCanCreateGroup(false);
        return user;
    }

    private int limitOnProjects() {
        return DEFAULT_CLIENT_LIMIT_ON_CREATED_PROJECTS;
    }

    private String generateRandomPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    private String name(Identifier clientId, Identifier deploymentId) {
        return "Client " + clientId + " (" + deploymentId + ")";
    }

    private String userName(Identifier clientId, Identifier deploymentId) {
        return "client-" + clientId + "-" + deploymentId;
    }

    private String userEmail(String username) {
        return username + "@" + DEFAULT_CLIENT_EMAIL_DOMAIN;
    }

    private Integer getOrCreateGroupWithMemberForUserIfNotExists(Integer gitLabUserId, Identifier clientId) throws FileTransferException {
        try {
            return gitlab.getGroupApi().getGroup(groupPath(clientId)).getId();
        } catch (GitLabApiException e) {
            if (statusIsDifferentThenNotFound(e.getHttpStatus()))
                throw new FileTransferException("" + e.getMessage());
            try {
                gitlab.getGroupApi().addGroup(groupName(clientId), groupPath(clientId));
                Integer groupId = gitlab.getGroupApi().getGroup(groupPath(clientId)).getId();
                gitlab.getGroupApi().addMember(groupId, gitLabUserId, fullAccessCode());
                return groupId;
            } catch (GitLabApiException e1) {
                throw new FileTransferException("" + e1.getMessage());
            }
        }
    }

    private String groupName(Identifier clientId) {
        return "client-" + clientId;
    }

    // TODO group path should include the name of client's company/organisation
    private String groupPath(Identifier clientId) {
        return GROUPS_PATH_PREFIX + "-" + groupName(clientId);
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
            gitlab.getProjectApi().addMember(project.getId(), userId, fullAccessCode());
            return project.getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException("" + e.getMessage() + e.getReason());
        }
    }

    private String projectName(Identifier deploymentId) {
        return deploymentId.value();
    }

    private GitLabProject project(Identifier deploymentId, Integer gitLabUserId, String gitLabPassword, Integer gitLabProjectId)
            throws FileTransferException {
        try {
            String gitLabUser = getUser(gitLabUserId);
            String gitLabRepoUrl = getHttpUrlToRepo(gitLabProjectId);
            String gitCloneUrl = generateCompleteGitCloneUrl(gitLabUser, gitLabPassword, gitLabRepoUrl);
            return new GitLabProject(deploymentId, gitLabUser, gitLabPassword, gitLabRepoUrl, gitCloneUrl);
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    private String getUser(Integer gitLabUserId) throws GitLabApiException {
        return gitlab.getUserApi().getUser(gitLabUserId).getUsername();
    }

    private String getHttpUrlToRepo(Integer gitLabProjectId) throws GitLabApiException {
        return gitlab.getProjectApi().getProject(gitLabProjectId).getHttpUrlToRepo();
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
