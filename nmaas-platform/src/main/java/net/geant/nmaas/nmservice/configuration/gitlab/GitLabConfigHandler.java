package net.geant.nmaas.nmservice.configuration.gitlab;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabNotFoundException;
import net.geant.nmaas.nmservice.configuration.GitConfigHandler;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigRepositoryAccessDetailsNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.commitBranch;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.commitMessage;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.committedFile;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.createStandardUser;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.fullAccessCode;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.generateRandomPassword;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.generateRandomToken;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.generateWebhookId;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.groupName;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.groupPath;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.projectName;
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.updateCommitMessage;

/**
 * Interacts with a remote GitLab repository instance through a REST API in order to upload a set of application
 * configuration files prepared for new application/tool deployment.
 * It is assumed that valid address and credentials of the repository API are provided through properties file.
 */
@Component
@Profile("env_kubernetes")
@Log4j2
public class GitLabConfigHandler implements GitConfigHandler {

    private KubernetesRepositoryManager repositoryManager;
    private NmServiceConfigFileRepository configurations;
    private GitLabManager gitLabManager;

    private static final String LOG_PREFIX = "GITLAB: ";

    public GitLabConfigHandler(KubernetesRepositoryManager repositoryManager, NmServiceConfigFileRepository configurations, GitLabManager gitLabManager) {
        this.repositoryManager = repositoryManager;
        this.configurations = configurations;
        this.gitLabManager = gitLabManager;
    }

    @Value("${nmaas.platform.webhooks.baseurl}")
    private String webhooksBaseUrl;

    /**
     * Creates a new GitLab user if one with given email does not exist already and add / replaces his SSH keys
     *
     * @param userUsername username of the new user
     * @param userEmail email of the new user
     * @param userName full name of the new user
     * @param userSshKeys list of SSH keys of the new user
     * @throws FileTransferException if a problem with during user creation is encountered
     */
    @Override
    @Loggable(LogLevel.DEBUG)
    public void createUser(String userUsername, String userEmail, String userName, List<String> userSshKeys) {
        try {
            if (!gitLabManager.users().getOptionalUser(userUsername).isPresent()) {
                gitLabManager.users().createUser(
                        createStandardUser(userUsername, userEmail, userName),
                        generateRandomPassword(),
                        false
                );
            }
            replaceUserSshKeys(userUsername, userSshKeys);
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    private void replaceUserSshKeys(String username, List<String> sshKeys) throws GitLabApiException {
        Integer userId = getUserId(username);
        gitLabManager.users().getSshKeys(userId).forEach(k -> {
            try {
                gitLabManager.users().deleteSshKey(userId, k.getId());
            } catch (GitLabApiException e) {
                throw new FileTransferException(e.getMessage());
            }
        });
        sshKeys.forEach(k -> {
            try {
                gitLabManager.users().addSshKey(userId, LocalTime.now().toString(), k);
            } catch (GitLabApiException e) {
                throw new FileTransferException(e.getMessage());
            }
        });
    }

    /**
     * Creates a new GitLab repository dedicated for the client requesting the deployment.
     * If an account for this client does not yet exists it is created.
     * Information on how to access the repository is stored in {@link GitLabProject} object.
     *
     * @param deploymentId unique identifier of service deployment
     * @param member username of an existing user to be added as a member for created repository
     * @throws InvalidDeploymentIdException if a service for given deployment identifier could not be found in database
     * @throws FileTransferException if a problem with repository creation is encountered
     */
    @Override
    @Loggable(LogLevel.DEBUG)
    public void createRepository(Identifier deploymentId, String member) {
        String domain = repositoryManager.loadDomain(deploymentId);
        Identifier descriptiveDeploymentId = repositoryManager.loadDescriptiveDeploymentId(deploymentId);
        log.info(String.format("Retrieving or creating user %s", member));
        Integer gitLabUserId = getUserId(member);
        log.info(String.format("Retrieving or creating group %s", domain));
        Integer gitLabGroupId = getOrCreateGroupWithMemberForUserIfNotExists(gitLabUserId, domain);
        log.info(String.format("Creating project %s within the group %s", descriptiveDeploymentId, domain));
        Integer gitLabProjectId = createProjectWithinGroup(gitLabGroupId, descriptiveDeploymentId);
        log.info("Adding member to the project");
        addMemberToProject(gitLabProjectId, gitLabUserId);
        String webhookId = generateWebhookId();
        String webhookToken = generateRandomToken();
        log.info("Adding webhook to the project");
        addWebhookToProject(gitLabProjectId, webhookId, webhookToken);
        GitLabProject project = project(descriptiveDeploymentId, member, gitLabProjectId);
        project.setWebhookId(webhookId);
        project.setWebhookToken(webhookToken);
        repositoryManager.updateGitLabProject(deploymentId, project);
    }

    private Integer getUserId(String username) {
        try {
            return gitLabManager.users()
                    .getOptionalUser(username)
                    .orElseThrow(
                            () -> new GitLabNotFoundException(String.format("User [%s] not found with gitlab", username))
                    ).getId();
        } catch (GitLabNotFoundException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage());
        }
    }

    private Integer getOrCreateGroupWithMemberForUserIfNotExists(Integer gitLabUserId, String domain) {
        try {
            Optional<Group> group = gitLabManager.groups().getOptionalGroup(groupPath(domain));
            if (group.isPresent()) {
                return group.get().getId();
            } else {
                gitLabManager.groups().addGroup(groupName(domain), groupPath(domain));
                Integer groupId = gitLabManager.groups().getGroup(groupPath(domain)).getId();
                gitLabManager.groups().addMember(groupId, gitLabUserId, fullAccessCode());
                return groupId;
            }
        } catch (GitLabApiException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage());
        }
    }

    private Integer createProjectWithinGroup(Integer groupId, Identifier deploymentId) {
        try {
            return gitLabManager.projects().createProject(groupId, projectName(deploymentId)).getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage() + " " + e.getReason());
        }
    }

    private GitLabProject project(Identifier deploymentId, String member, Integer gitLabProjectId) {
        try {
            String gitLabRepoUrl = getHttpUrlToRepo(gitLabProjectId);
            String gitLabSshRepoUrl = getSshUrlToRepo(gitLabProjectId);
            return new GitLabProject(deploymentId, member, "", gitLabRepoUrl, gitLabSshRepoUrl, gitLabProjectId);
        } catch (GitLabApiException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage());
        }
    }

    String getSshUrlToRepo(Integer gitLabProjectId) throws GitLabApiException {
        return StringUtils.replace(gitLabManager.projects().getProject(gitLabProjectId).getSshUrlToRepo(), ":", "/");
    }

    String getHttpUrlToRepo(Integer gitLabProjectId) throws GitLabApiException {
        String[] urlFromGitlabApiParts = gitLabManager.projects().getProject(gitLabProjectId).getHttpUrlToRepo().split("//");
        String[] urlParts = urlFromGitlabApiParts[1].split("/");
        urlParts[0] = gitLabManager.getGitlabServer() + ":" + gitLabManager.getGitlabPort();
        return urlFromGitlabApiParts[0] + "//" + String.join("/", urlParts);
    }

    @Override
    public void addMemberToProject(Integer gitLabProjectId, Integer gitLabUserId) {
        try {
            gitLabManager.projects().addMember(gitLabProjectId, gitLabUserId, fullAccessCode());
        } catch (GitLabApiException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage() + " " + e.getReason());
        }
    }

    @Override
    public void addMemberToProject(Integer gitLabProjectId, String username) {
        Integer userId = getUserId(username);
        this.addMemberToProject(gitLabProjectId, userId);
    }

    @Override
    public void removeMemberFromProject(Integer gitLabProjectId, Integer gitLabUserId) {
        try {
            gitLabManager.projects().removeMember(gitLabProjectId, gitLabUserId);
        } catch (GitLabApiException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage() + " " + e.getReason());
        }
    }

    @Override
    public void removeMemberFromProject(Integer gitLabProjectId, String username) {
        Integer userId = getUserId(username);
        this.removeMemberFromProject(gitLabProjectId, userId);

    }

    private void addWebhookToProject(Integer gitLabProjectId, String webhookId, String webhookToken) {
        try {
            ProjectHook hook = new ProjectHook();
            hook.setPushEvents(true);
            String completeWebhookUrl = getWebhookUrl(webhookId);
            log.info(String.format("completeWebhookUrl: %s", completeWebhookUrl));
            gitLabManager.projects().addHook(gitLabProjectId, completeWebhookUrl, hook, true, webhookToken);
        } catch (GitLabApiException e) {
            throw new FileTransferException(LOG_PREFIX + e.getMessage() + " " + e.getReason());
        }
    }

    private String getWebhookUrl(String webhookId) {
        return webhooksBaseUrl + "/" + webhookId;
    }

    private NmServiceConfiguration loadConfigurationFromDatabase(String configId) {
        return configurations.findByConfigId(configId)
                .orElseThrow(() -> new ConfigFileNotFoundException("Required configuration file not found in repository"));
    }

    /**
     * Uploads a set of configuration files to a GitLab repository
     *
     * @param deploymentId unique identifier of service deployment
     * @param configIds list of identifiers of configuration files that should be loaded from database and uploaded to the git repository
     * @throws InvalidDeploymentIdException if a service for given deployment identifier could not be found in database
     * @throws ConfigFileNotFoundException if any of the configuration files for which an identifier is given could not be found in database
     * @throws FileTransferException if any error occurs during communication with the git repository API
     */
    @Override
    @Loggable(LogLevel.DEBUG)
    public void commitConfigFiles(Identifier deploymentId, List<String> configIds) {
        loadGitlabProject(deploymentId).ifPresent(p -> uploadConfigFilesToProject(p.getProjectId(), configIds));
    }

    private void uploadConfigFilesToProject(Integer gitLabProjectId, List<String> configIds) {
        configIds.forEach(configId -> {
            NmServiceConfiguration configuration = loadConfigurationFromDatabase(configId);
            RepositoryFile file = committedFile(configuration);
            try {
                if (gitLabManager.repositoryFiles().getOptionalFile(gitLabProjectId, file.getFilePath(), commitBranch()).isPresent()) {
                    gitLabManager.repositoryFiles().updateFile(gitLabProjectId, file, commitBranch(), updateCommitMessage(configuration.getConfigFileName()));
                } else {
                    gitLabManager.repositoryFiles().createFile(gitLabProjectId, file, commitBranch(), commitMessage(configuration.getConfigFileName()));
                }
            } catch (GitLabApiException e) {
                throw new FileTransferException("Could not commit file " + configuration.getConfigFileName() + " due to exception: " + e.getMessage());
            }
        });
    }

    /**
     * Removes all files from a GitLab repository
     *
     * @param deploymentId unique identifier of service deployment
     * @throws InvalidDeploymentIdException if a service for given deployment identifier could not be found in database
     * @throws ConfigFileNotFoundException if any of the configuration files for which an identifier is given could not be found in database
     * @throws FileTransferException if any error occurs during communication with the git repository API
     */
    @Override
    @Loggable(LogLevel.DEBUG)
    public void removeConfigFiles(Identifier deploymentId){
        loadGitlabProject(deploymentId).ifPresent(p -> removeProject(p.getProjectId()));
    }

    private void removeProject(Integer projectId){
        gitLabManager.projects().getOptionalProject(projectId).ifPresent(p -> {
            try {
                gitLabManager.projects().deleteProject(projectId);
            } catch (GitLabApiException e) {
                throw new FileTransferException(e.getClass().getName() + e.getMessage());
            }
        });
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId) {
        Optional<GitLabProject> gitLabProject = loadGitlabProject(deploymentId);
        if (gitLabProject.isPresent()) {
            String cloneUrl = gitLabProject.get().getCloneUrl();
            if (!StringUtils.isEmpty(cloneUrl)) {
                return AppConfigRepositoryAccessDetails.of(cloneUrl);
            }
        }
        throw new ConfigRepositoryAccessDetailsNotFoundException("Could not find GitLab project for deployment or cloneUrl is empty");
    }

    private Optional<GitLabProject> loadGitlabProject(Identifier deploymentId){
        return repositoryManager.loadGitLabProject(deploymentId);
    }

}
