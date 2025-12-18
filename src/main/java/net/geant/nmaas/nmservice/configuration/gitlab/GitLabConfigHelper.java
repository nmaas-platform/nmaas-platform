package net.geant.nmaas.nmservice.configuration.gitlab;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.orchestration.Identifier;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.User;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class GitLabConfigHelper {

    static final String GROUPS_PATH_PREFIX = "groups";
    static final String DEFAULT_BRANCH_FOR_COMMIT = "master";
    static final String DEFAULT_REPLACEMENT_FOR_INCORRECT_CHARACTER = "_";
    static final String FORBIDDEN_USERNAME_SUFFIX1 = ".git";
    static final String FORBIDDEN_USERNAME_SUFFIX2 = ".atom";
    private static final String GITLAB_ALLOWED_CHARACTERS = "[A-Za-z0-9._-]+";

    static final int PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL = 40;
    private static final int DEFAULT_WEBHOOK_TOKEN_LENGTH = 30;

    /**
     * Applies specific rules for the GitLab user username
     *
     * @param username Username from the user account
     * @return correct username to be used in GitLab
     */
    static String prepareGitLabUsername(String username) {
        if (username.endsWith(FORBIDDEN_USERNAME_SUFFIX1)) {
            username = username.substring(0, username.length() - FORBIDDEN_USERNAME_SUFFIX1.length());
        }
        if (username.endsWith(FORBIDDEN_USERNAME_SUFFIX2)) {
            username = username.substring(0, username.length() - FORBIDDEN_USERNAME_SUFFIX2.length());
        }
        return username
                .replace("@", DEFAULT_REPLACEMENT_FOR_INCORRECT_CHARACTER)
                .replace("#", DEFAULT_REPLACEMENT_FOR_INCORRECT_CHARACTER)
                .stripTrailing()
                .toLowerCase();
    }

    static User createStandardUser(String username, String email, String name) {
        if (name == null || name.isEmpty() || name.trim().isEmpty()) {
            name = username;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setName(name);
        user.setCanCreateGroup(false);
        user.setCanCreateProject(false);
        user.setSkipConfirmation(true);
        log.info("Creating GitLab user (username: [{}], email: [{}], name: [{}])", username, email, name);
        return user;
    }

    static String generateRandomPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    static String generateWebhookId() {
        return String.valueOf(System.nanoTime());
    }

    static String generateRandomToken() {
        return RandomStringUtils.random(DEFAULT_WEBHOOK_TOKEN_LENGTH, true, true);
    }

    static String projectName(Identifier deploymentId) {
        return deploymentId.value();
    }

    static String groupName(String domain) {
        return domain;
    }

    static String groupPath(String domain) {
        return GROUPS_PATH_PREFIX + "-" + groupName(domain);
    }

    static String fullGroupPath(String domain, Optional<String> parentPath) {
        return parentPath.map(parent -> parent + "/" + groupPath(domain)).orElse(groupPath(domain));
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
        if (StringUtils.isNotEmpty(configuration.getConfigFileDirectory())) {
            file.setFilePath(configuration.getConfigFileDirectory() + "/" + configuration.getConfigFileName());
        } else {
            file.setFilePath(configuration.getConfigFileName());
        }
        file.setContent(configuration.getConfigFileContent());
        return file;
    }

    static String commitMessage(String fileName) {
        return "Initial commit of " + fileName;
    }

    static String updateCommitMessage(String fileName) {
        return "Update commit of " + fileName;
    }

    public static String sanitizeGroupPathSegment(String value) {
        if (value == null) {
            throw new GitLabInvalidConfigurationException(
                    "GitLab top-level group name must be defined"
            );
        }
        // Remove leading or trailing dots/hyphens (not allowed in paths)
        String sanitized = value.replaceAll("^[-.]+|[-.]+$", "").trim();
        if (!sanitized.matches(GITLAB_ALLOWED_CHARACTERS)) {
            throw new GitLabInvalidConfigurationException(
                    "Invalid GitLab top-level group name: " + value
            );
        }
        if (sanitized.isEmpty()) {
            throw new GitLabInvalidConfigurationException(
                    "Invalid GitLab top-level group name: " + value
            );
        }
        return sanitized.toLowerCase(Locale.ROOT);
    }

}