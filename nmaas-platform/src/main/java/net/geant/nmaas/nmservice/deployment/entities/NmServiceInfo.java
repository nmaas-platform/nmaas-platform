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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Unique name of the service provided by the caller */
    @Column(nullable = false)
    private String name;

    /** State in which service should be at this point */
    @Column(nullable = false)
    private NmServiceDeploymentState state = NmServiceDeploymentState.INIT;

    /** Identifier of the application deployment assigned by application lifecycle manager */
    @Column(nullable = false, unique = true)
    private Identifier deploymentId;

    /** Name of the deployment provided by the user. */
    @Column(nullable = false)
    private String deploymentName;

    /** Name of the client domain for this deployment */
    @Column(nullable = false)
    private String domain;

    /**
     * The list of IP addresses of devices to be managed/monitored by the deployed service.
     * These addresses are provided by the user during wizard completion.
     * For these addresses specific routing entries needs to be by applied on the container once run.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> managedDevicesIpAddresses;

    /** GitLab project information created to store configuration files for this service (deployment) */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GitLabProject gitLabProject;

    public NmServiceInfo() { }

    public NmServiceInfo(Identifier deploymentId, String deploymentName, String domain) {
        this.name = deploymentId.value();
        this.deploymentId = deploymentId;
        this.deploymentName = deploymentName;
        this.domain = domain;
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

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
