package net.geant.nmaas.nmservice.deployment.entities;

import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Abstract Network Management Service deployment information.
 * Extended by each {@link net.geant.nmaas.nmservice.deployment.ContainerOrchestrator}.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class NmServiceInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    /**
     * Unique name of the service provided by the caller
     */
    @Column(nullable = false)
    private String name;

    /**
     * State in which service should be at this point
     */
    @Column(nullable=false)
    private NmServiceDeploymentState state = NmServiceDeploymentState.INIT;

    /**
     * Identifier of the application deployment assigned by application lifecycle manager
     */
    @Column(nullable=false, unique=true)
    private Identifier deploymentId;

    /**
     * Identifier of the application being deployed
     */
    @Column(nullable=false)
    private Identifier applicationId;

    /**
     * Identifier of the client requesting application deployment
     */
    @Column(nullable=false)
    private Identifier clientId;

    /**
     * The list of IP addresses of devices to be managed/monitored by the deployed service.
     * These addresses are provided by the user during wizard completion.
     * For these addresses specific routing entries needs to be by applied on the container once run.
     */
    @ElementCollection(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> managedDevicesIpAddresses;

    /**
     * GitLab project information created to store configuration files for this service (deployment)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GitLabProject gitLabProject;

    public NmServiceInfo() { }

    public NmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId) {
        this.name = deploymentId.value();
        this.deploymentId = deploymentId;
        this.applicationId = applicationId;
        this.clientId = clientId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NmServiceDeploymentState getState() {
        return state;
    }

    public void setState(NmServiceDeploymentState state) {
        this.state = state;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(Identifier deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Identifier applicationId) {
        this.applicationId = applicationId;
    }

    public Identifier getClientId() {
        return clientId;
    }

    public void setClientId(Identifier clientId) {
        this.clientId = clientId;
    }

    public List<String> getManagedDevicesIpAddresses() {
        return managedDevicesIpAddresses;
    }

    public void setManagedDevicesIpAddresses(List<String> managedDevicesIpAddresses) {
        this.managedDevicesIpAddresses = managedDevicesIpAddresses;
    }

    public GitLabProject getGitLabProject() {
        return gitLabProject;
    }

    public void setGitLabProject(GitLabProject gitLabProject) {
        this.gitLabProject = gitLabProject;
    }
}
