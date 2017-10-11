package net.geant.nmaas.nmservice.deployment.entities;

import javax.persistence.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents Docker Host which runs Docker Engine daemon, exposes an Docker Remote API and is available for container deployment.
 * Eventually data contained within this object should be retrieved from remote OSS system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_host")
public class DockerHost {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    /**
     * Unique name identifying this Docker host.
     */
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

    public DockerHost() {}

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

    public InetAddress getApiIpAddress() {
        return apiIpAddress;
    }

    public void setApiIpAddress(InetAddress apiIpAddress) {
        this.apiIpAddress = apiIpAddress;
    }

    public Integer getApiPort() {
        return apiPort;
    }

    public void setApiPort(Integer apiPort) {
        this.apiPort = apiPort;
    }

    public InetAddress getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(InetAddress publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getAccessInterfaceName() {
        return accessInterfaceName;
    }

    public void setAccessInterfaceName(String accessInterfaceName) {
        this.accessInterfaceName = accessInterfaceName;
    }

    public String getDataInterfaceName() {
        return dataInterfaceName;
    }

    public void setDataInterfaceName(String dataInterfaceName) {
        this.dataInterfaceName = dataInterfaceName;
    }

    public InetAddress getBaseDataNetworkAddress() {
        return baseDataNetworkAddress;
    }

    public void setBaseDataNetworkAddress(InetAddress baseDataNetworkAddress) {
        this.baseDataNetworkAddress = baseDataNetworkAddress;
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }

    public String getVolumesPath() {
        return volumesPath;
    }

    public void setVolumesPath(String volumesPath) {
        this.volumesPath = volumesPath;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public List<DockerHostNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<DockerHostNetwork> networks) {
        this.networks = networks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerHost)) return false;

        DockerHost that = (DockerHost) o;

        if (isPreferred() != that.isPreferred()) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getApiIpAddress() != null ? !getApiIpAddress().getHostAddress().equals(that.getApiIpAddress().getHostAddress()) : that.getApiIpAddress() != null)
            return false;
        if (getApiPort() != null ? !getApiPort().equals(that.getApiPort()) : that.getApiPort() != null) return false;
        if (getPublicIpAddress() != null ? !getPublicIpAddress().getHostAddress().equals(that.getPublicIpAddress().getHostAddress()) : that.getPublicIpAddress() != null)
            return false;
        if (getAccessInterfaceName() != null ? !getAccessInterfaceName().equals(that.getAccessInterfaceName()) : that.getAccessInterfaceName() != null)
            return false;
        if (getDataInterfaceName() != null ? !getDataInterfaceName().equals(that.getDataInterfaceName()) : that.getDataInterfaceName() != null)
            return false;
        if (getBaseDataNetworkAddress() != null ? !getBaseDataNetworkAddress().equals(that.getBaseDataNetworkAddress()) : that.getBaseDataNetworkAddress() != null)
            return false;
        return getVolumesPath() != null ? getVolumesPath().equals(that.getVolumesPath()) : that.getVolumesPath() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getApiIpAddress() != null ? getApiIpAddress().hashCode() : 0);
        result = 31 * result + (getApiPort() != null ? getApiPort().hashCode() : 0);
        result = 31 * result + (getPublicIpAddress() != null ? getPublicIpAddress().hashCode() : 0);
        result = 31 * result + (getAccessInterfaceName() != null ? getAccessInterfaceName().hashCode() : 0);
        result = 31 * result + (getDataInterfaceName() != null ? getDataInterfaceName().hashCode() : 0);
        result = 31 * result + (getBaseDataNetworkAddress() != null ? getBaseDataNetworkAddress().hashCode() : 0);
        result = 31 * result + (getVolumesPath() != null ? getVolumesPath().hashCode() : 0);
        result = 31 * result + (isPreferred() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DockerHost{" +
                "name='" + name + '\'' +
                ", apiIpAddress=" + apiIpAddress +
                '}';
    }
}
