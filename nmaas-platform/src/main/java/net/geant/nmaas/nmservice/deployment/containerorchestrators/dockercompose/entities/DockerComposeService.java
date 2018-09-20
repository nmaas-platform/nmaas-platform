package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
