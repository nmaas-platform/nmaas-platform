package net.geant.nmaas.nmservice.configuration.entities;

import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;

/**
 * Stores information tha allow to connect to git repository created on GitLab.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="gitlab_project")
public class GitLabProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    /**
     * Identifier of the application deployment assigned by application lifecycle manager
     */
    @Column(nullable=false, unique=true)
    private Identifier deploymentId;

    /**
     * User to be used for git clone authorization
     */
    @Column(nullable=false)
    private String accessUser;

    /**
     * Password to be used for git clone authorization
     */
    @Column(nullable=false)
    private String accessPassword;

    /**
     * Http URL of the git repository to be cloned
     */
    @Column(nullable=false)
    private String accessUrl;

    /**
     * Http URL that can be directly used in "git clone" command (includes user access credentials)
     */
    @Column(nullable=false)
    private String cloneUrl;

    public GitLabProject() {
    }

    public GitLabProject(Identifier deploymentId, String accessUser, String accessPassword, String accessUrl, String cloneUrl) {
        this.deploymentId = deploymentId;
        this.accessUser = accessUser;
        this.accessPassword = accessPassword;
        this.accessUrl = accessUrl;
        this.cloneUrl = cloneUrl;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public String getAccessUser() {
        return accessUser;
    }

    public String getAccessPassword() {
        return accessPassword;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

}
