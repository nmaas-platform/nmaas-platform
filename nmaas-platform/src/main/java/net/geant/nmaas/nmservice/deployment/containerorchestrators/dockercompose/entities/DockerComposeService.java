package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_compose_service")
public class DockerComposeService {

    /**
     * It is assumed that such network is pre-configured manually on all Docker Hosts supporting Docker Compose -based
     * container deployment. Exemplary command to be issued: "docker network create -d bridge nmaas-access".
     */
    public static final String DEFAULT_EXTERNAL_ACCESS_NETWORK_NAME = "nmaas-access";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Name of the Docker network deployed on the host. */
    private String dcnNetworkName;

    @Column(nullable = false)
    private String externalAccessNetworkName = DEFAULT_EXTERNAL_ACCESS_NETWORK_NAME;

    /** Public port number assigned for accessing services running on container. */
    @Column(nullable = false)
    private int publicPort;

    /** Directory on the Docker Host to be used for storing configuration files and data. */
    @Column(nullable = false)
    private String attachedVolumeName;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DockerComposeServiceComponent> serviceComponents = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDcnNetworkName() {
        return dcnNetworkName;
    }

    public void setDcnNetworkName(String dcnNetworkName) {
        this.dcnNetworkName = dcnNetworkName;
    }

    public String getExternalAccessNetworkName() {
        return externalAccessNetworkName;
    }

    public void setExternalAccessNetworkName(String externalAccessNetworkName) {
        this.externalAccessNetworkName = externalAccessNetworkName;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }

    public String getAttachedVolumeName() {
        return attachedVolumeName;
    }

    public void setAttachedVolumeName(String attachedVolumeName) {
        this.attachedVolumeName = attachedVolumeName;
    }

    public List<DockerComposeServiceComponent> getServiceComponents() {
        return serviceComponents;
    }

    public void setServiceComponents(List<DockerComposeServiceComponent> serviceComponents) {
        this.serviceComponents = serviceComponents;
    }
}
