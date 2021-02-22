package net.geant.nmaas.nmservice.configuration.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.orchestration.Identifier;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stores information tha allow to connect to git repository created on GitLab.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gitlab_project")
public class GitLabProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Identifier of the application deployment assigned by application lifecycle manager
     */
    @Column(nullable = false, unique = true)
    private Identifier deploymentId;

    /**
     * User to be used for git clone authorization
     */
    @Column(nullable = false)
    private String accessUser;

    /**
     * Password to be used for git clone authorization
     */
    @Column(nullable = false)
    private String accessPassword;

    /**
     * Http URL of the git repository
     */
    @Column(nullable = false)
    private String accessUrl;

    /**
     * Internal GitLab project id
     */
    @Column(nullable = false)
    private Integer projectId;

    /**
     * URL that can be directly used in "git clone" command with SSH
     */
    @Column(nullable = false)
    private String cloneUrl;

    @Column
    private String webhookId;

    @Column
    private String webhookToken;

    public GitLabProject(Identifier deploymentId, String accessUser, String accessPassword, String accessUrl, String cloneUrl, Integer projectId) {
        this.deploymentId = deploymentId;
        this.accessUser = accessUser;
        this.accessPassword = accessPassword;
        this.accessUrl = accessUrl;
        this.cloneUrl = cloneUrl;
        this.projectId = projectId;
    }

}
