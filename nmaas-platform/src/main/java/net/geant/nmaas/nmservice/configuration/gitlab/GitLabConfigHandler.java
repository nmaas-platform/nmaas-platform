package net.geant.nmaas.nmservice.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.commitBranch;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.commitMessage;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.committedFile;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.createStandardUser;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.fullAccessCode;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.generateRandomPassword;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.groupName;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.groupPath;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.projectName;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.statusIsDifferentThenNotFound;
import static net.geant.nmaas.nmservice.configuration.GitLabConfigHelper.updateCommitMessage;

/**
 * Interacts with a remote GitLab repository instance through a REST API in order to upload a set of application
 * configuration files prepared for new application/tool deployment.
 * It is assumed that valid address and credentials of the repository API are provided through properties file.
 */
@Component
@Profile("env_kubernetes")
@AllArgsConstructor
@Log4j2
public class GitLabConfigHandler {

    private KubernetesRepositoryManager repositoryManager;
    private NmServiceConfigFileRepository configurations;
    private GitLabManager gitLabManager;

    /**
     * Creates a new GitLab repository dedicated for the client requesting the deployment.
     * If an account for this client does not yet exists it is created.
     * Information on how to access the repository is stored in {@link GitLabProject} object.
     *
     * @param deploymentId unique identifier of service deployment
     * @param descriptiveDeploymentId human readable identifier of the deployment
     * @throws InvalidDeploymentIdException if a service for given deployment identifier could not be found in database
     */
    public void createRepository(Identifier deploymentId, Identifier descriptiveDeploymentId, User owner) {
        String domain = repositoryManager.loadDomain(deploymentId);
        String gitLabPassword = generateRandomPassword();
        Integer gitLabUserId = createUser(domain, descriptiveDeploymentId, gitLabPassword);
        //TODO addUserSshKey();
        Integer gitLabGroupId = getOrCreateGroupWithMemberForUserIfNotExists(gitLabUserId, domain);
        Integer gitLabProjectId = createProjectWithinGroupWithMember(gitLabGroupId, gitLabUserId, descriptiveDeploymentId);
        GitLabProject project = project(descriptiveDeploymentId, gitLabUserId, gitLabPassword, gitLabProjectId);
        repositoryManager.updateGitLabProject(deploymentId, project);
    }

    private Integer createUser(String domain, Identifier deploymentId, String password) {
        try {
            return gitLabManager.users().createUser(createStandardUser(domain, deploymentId), password, false).getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException(e.getClass().getName() + e.getMessage());
        }
    }

    private Integer getOrCreateGroupWithMemberForUserIfNotExists(Integer gitLabUserId, String domain) {
        try {
            return gitLabManager.groups().getGroup(groupPath(domain)).getId();
        } catch (GitLabApiException e) {
            if (statusIsDifferentThenNotFound(e.getHttpStatus()))
                throw new FileTransferException("" + e.getMessage());
            try {
                gitLabManager.groups().addGroup(groupName(domain), groupPath(domain));
                Integer groupId = gitLabManager.groups().getGroup(groupPath(domain)).getId();
                gitLabManager.groups().addMember(groupId, gitLabUserId, fullAccessCode());
                return groupId;
            } catch (GitLabApiException e1) {
                throw new FileTransferException("" + e1.getMessage());
            }
        }
    }

    private Integer createProjectWithinGroupWithMember(Integer groupId, Integer userId, Identifier deploymentId) {
        try {
            Project project = gitLabManager.projects().createProject(groupId, projectName(deploymentId));
            gitLabManager.projects().addMember(project.getId(), userId, fullAccessCode());
            return project.getId();
        } catch (GitLabApiException e) {
            throw new FileTransferException("" + e.getMessage() + " " + e.getReason());
        }
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

    private String getUser(Integer gitLabUserId) throws GitLabApiException {
        return gitLabManager.users().getUser(gitLabUserId).getUsername();
    }

    String getHttpUrlToRepo(Integer gitLabProjectId) throws GitLabApiException {
        String[] urlFromGitlabApiParts = gitLabManager.projects().getProject(gitLabProjectId).getHttpUrlToRepo().split("//");
        String[] urlParts = urlFromGitlabApiParts[1].split("/");
        urlParts[0] = gitLabManager.getGitlabServer() + ":" + gitLabManager.getGitlabPort();
        return urlFromGitlabApiParts[0] + "//" + String.join("/", urlParts);
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
    public void commitConfigFiles(Identifier deploymentId, List<String> configIds) {
        loadGitlabProject(deploymentId).ifPresent(p -> uploadConfigFilesToProject(p.getProjectId(), configIds));
    }

    private void uploadConfigFilesToProject(Integer gitLabProjectId, List<String> configIds) {
        configIds.forEach(configId -> {
            NmServiceConfiguration configuration = loadConfigurationFromDatabase(configId);
            RepositoryFile file = committedFile(configuration);
            try {
                if (gitLabManager.repositoryFiles().getOptionalFile(gitLabProjectId, file.getFileName(), commitBranch()).isPresent()) {
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

    private Optional<GitLabProject> loadGitlabProject(Identifier deploymentId){
        return Optional.of(repositoryManager.loadService(deploymentId).getGitLabProject());
    }

}
