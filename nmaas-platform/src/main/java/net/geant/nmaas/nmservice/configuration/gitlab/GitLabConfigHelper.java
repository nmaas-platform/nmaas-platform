package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.orchestration.Identifier;
import org.apache.commons.lang3.RandomStringUtils;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.User;
import org.springframework.http.HttpStatus;

public class GitLabConfigHelper {

    static final String GROUPS_PATH_PREFIX = "groups";
    static final int DEFAULT_DOMAIN_LIMIT_ON_CREATED_PROJECTS = 100;
    static final String DEFAULT_CLIENT_EMAIL_DOMAIN = "nmaas.geant.net";
    static final String DEFAULT_BRANCH_FOR_COMMIT = "master";
    static final int PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL = 40;

    static String projectName(Identifier deploymentId) {
        return deploymentId.value();
    }

    static User createStandardUser(String domain, Identifier deploymentId) {
        User user = new User();
        user.setName(name(domain, deploymentId));
        String userName = userName(domain, deploymentId);
        user.setUsername(userName);
        user.setEmail(userEmail(userName));
        user.setCanCreateGroup(false);
        user.setProjectsLimit(limitOnProjects());
        return user;
    }

    private static String name(String domain, Identifier deploymentId) {
        return domain + " (" + deploymentId + ")";
    }

    private static String userName(String domain, Identifier deploymentId) {
        return domain + "-" + deploymentId;
    }

    private static String userEmail(String username) {
        return username + "@" + DEFAULT_CLIENT_EMAIL_DOMAIN;
    }

    static String generateRandomPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    static int limitOnProjects() {
        return DEFAULT_DOMAIN_LIMIT_ON_CREATED_PROJECTS;
    }

    static String groupName(String domain) {
        return domain;
    }

    static String groupPath(String domain) {
        return GROUPS_PATH_PREFIX + "-" + groupName(domain);
    }

    static boolean statusIsDifferentThenNotFound(int httpStatus) {
        return httpStatus != HttpStatus.NOT_FOUND.value();
    }

    static Integer fullAccessCode() {
        return PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL;
    }

    static String commitBranch() {
        return DEFAULT_BRANCH_FOR_COMMIT;
    }

    static RepositoryFile committedFile(NmServiceConfiguration configuration) {
        RepositoryFile file = new RepositoryFile();
        file.setFilePath(configuration.getConfigFileName());
        file.setContent(configuration.getConfigFileContent());
        return file;
    }

    static String commitMessage(String fileName) {
        return "Initial commit of " + fileName;
    }

    static String updateCommitMessage(String fileName) {
        return "Update commit of " + fileName;
    }

}
