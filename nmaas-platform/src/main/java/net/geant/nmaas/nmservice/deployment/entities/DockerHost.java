package net.geant.nmaas.nmservice.deployment.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents Docker Host which runs Docker Engine daemon, exposes an Docker Remote API and is available for container deployment.
 * Eventually data contained within this object should be retrieved from remote OSS system.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="docker_host")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DockerHost {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    /**
     * Unique name identifying this Docker host.
     */
    @EqualsAndHashCode.Include
    private String name;

    /**
     * Ip address on which Docker Remote API is exposed.
     */
    private InetAddress apiIpAddress;

    /**
     * Port on which Docker Remote API is exposed.
     */
    private Integer apiPort;

    /**
     * Public Ip address of the Docker host which will be used by the clients to access services deployed on containers.
     */
    private InetAddress publicIpAddress;

    /**
     * Name of the interface to which Docker default bridge is attached.
     */
    private String accessInterfaceName;

    /**
     * Name of the Docker host interface on which network monitoring/management traffic will be exchanged.
     * On this interface dedicated VLANs will be configured during container deployment.
     */
    private String dataInterfaceName;

    /**
     * Address of the base /9 network from which address pools for particular container deployment will be assigned.
     */
    private InetAddress baseDataNetworkAddress;

    /**
     * Default root directory on the Docker host for scripts execution
     */
    private String workingPath;

    /**
     * Default root directory on the Docker host on which volumes will be created and mounted on deployed containers.
     */
    private String volumesPath;

    /**
     * Helper flag indicating whether this Docker host should be always preferred in selection for new container deployment.
     */
    private boolean preferred;

    @OneToMany(mappedBy = "host", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DockerHostNetwork> networks = new ArrayList<>();

    public DockerHost(String name) {
        this.name = name;
    }

    public DockerHost(String name,
                      InetAddress apiIpAddress,
                      Integer apiPort,
                      InetAddress publicIpAddress,
                      String accessInterfaceName,
                      String dataInterfaceName,
                      InetAddress baseDataNetworkAddress,
                      String workingPath,
                      String volumesPath,
                      boolean preferred) {
        this.name = name;
        this.apiIpAddress = apiIpAddress;
        this.apiPort = apiPort;
        this.publicIpAddress = publicIpAddress;
        this.accessInterfaceName = accessInterfaceName;
        this.dataInterfaceName = dataInterfaceName;
        this.baseDataNetworkAddress = baseDataNetworkAddress;
        this.workingPath = workingPath;
        this.volumesPath = volumesPath;
        this.preferred = preferred;
    }

    /**
     * Constructs the complete URL of Docker Remote API exposed on the Docker host.
     *
     * @return API URL
     */
    public String apiUrl() {
        try {
            return new URL("http", apiIpAddress.getHostAddress(), apiPort, "").toString();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "DockerHost{" +
                "name='" + name + '\'' +
                ", apiIpAddress=" + apiIpAddress +
                '}';
    }
}
