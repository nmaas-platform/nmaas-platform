package net.geant.nmaas.nmservice.deployment.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Contains all data required by {@link net.geant.nmaas.nmservice.deployment.ContainerOrchestrator} to carry out
 * NM service deployment.
 *
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
     * Docker container template for this service (set in case of deployment using plain docker-engine api)
     */
    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    private DockerContainerTemplate dockerContainerTemplate;

    /**
     * Docker compose file template for this service (set in case of deployment using docker-compose api)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DockerComposeFileTemplate dockerComposeFileTemplate;

    /**
     * Kubernetes template for this service (set in case of deployment on kubernetes cluster using helm)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

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

    public DockerContainerTemplate getDockerContainerTemplate() {
        return dockerContainerTemplate;
    }

    public void setDockerContainerTemplate(DockerContainerTemplate dockerContainerTemplate) {
        this.dockerContainerTemplate = dockerContainerTemplate;
    }

    public DockerComposeFileTemplate getDockerComposeFileTemplate() {
        return dockerComposeFileTemplate;
    }

    public void setDockerComposeFileTemplate(DockerComposeFileTemplate dockerComposeFileTemplate) {
        this.dockerComposeFileTemplate = dockerComposeFileTemplate;
    }

    public KubernetesTemplate getKubernetesTemplate() {
        return kubernetesTemplate;
    }

    public void setKubernetesTemplate(KubernetesTemplate kubernetesTemplate) {
        this.kubernetesTemplate = kubernetesTemplate;
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
