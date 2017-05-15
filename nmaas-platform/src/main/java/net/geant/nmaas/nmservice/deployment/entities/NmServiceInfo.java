package net.geant.nmaas.nmservice.deployment.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="nm_service_info")
public class NmServiceInfo {

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
     * Container template for this service
     */
    @OneToOne(cascade=CascadeType.ALL, optional=false, orphanRemoval=true, fetch=FetchType.EAGER)
    private DockerContainerTemplate template;

    /**
     * Target deployment Docker Host on which this service will be or was deployed.
     */
    @ManyToOne(fetch=FetchType.EAGER)
    private DockerHost host;

    /**
     * Docker container deployed for this service.
     */
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private DockerContainer dockerContainer;

    /**
     * The list of IP addresses of devices to be managed/monitored by the deployed service.
     * These addresses are provided by the user during wizard completion.
     * For these addresses specific routing entries needs to be by applied on the container once run.
     */
    @ElementCollection(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> managedDevicesIpAddresses;

    public NmServiceInfo() { }

    public NmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId, DockerContainerTemplate template) {
        this.name = deploymentId.value();
        this.deploymentId = deploymentId;
        this.applicationId = applicationId;
        this.clientId = clientId;
        this.template = template;
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

    public DockerContainerTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DockerContainerTemplate template) {
        this.template = template;
    }

    public DockerHost getHost() {
        return host;
    }

    public void setHost(DockerHost host) {
        this.host = host;
    }

    public DockerContainer getDockerContainer() {
        return dockerContainer;
    }

    public void setDockerContainer(DockerContainer dockerContainer) {
        this.dockerContainer = dockerContainer;
    }

    public List<String> getManagedDevicesIpAddresses() {
        return managedDevicesIpAddresses;
    }

    public void setManagedDevicesIpAddresses(List<String> managedDevicesIpAddresses) {
        this.managedDevicesIpAddresses = managedDevicesIpAddresses;
    }
}
