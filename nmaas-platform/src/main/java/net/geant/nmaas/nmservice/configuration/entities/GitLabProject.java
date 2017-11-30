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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    /**
     * Identifier of the application deployment assigned by application lifecycle manager
     */
    @Column(nullable=false, unique=true)
    private Identifier deploymentId;

    /**
     * Token string to be used for git clone authorization
     */
    @Column(nullable=false)
    private String accessToken;

    /**
     * Http URL of the git repository to be cloned
     */
    @Column(nullable=false)
    private String accessUrl;

    public GitLabProject() {
    }

    public GitLabProject(Identifier deploymentId, String accessToken, String accessUrl) {
        this.deploymentId = deploymentId;
        this.accessToken = accessToken;
        this.accessUrl = accessUrl;
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

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessUrl() {
        return accessUrl;
    }
}
