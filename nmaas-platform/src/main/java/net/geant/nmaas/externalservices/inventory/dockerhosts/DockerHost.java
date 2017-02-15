package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentHost;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents Docker Host which runs Docker Engine daemon, exposes an Docker Remote API and is available for container deployment.
 * Eventually data contained within this object should be retrieved from remote OSS system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHost implements NmServiceDeploymentHost {

    /**
     * Unique name identifying this Docker host.
     */
    private final String name;

    /**
     * Ip address on which Docker Remote API is exposed.
     */
    private final InetAddress apiIpAddress;

    /**
     * Port on which Docker Remote API is exposed.
     */
    private final Integer apiPort;

    /**
     * Public Ip address of the Docker host which will be used by the clients to access services deployed on containers.
     */
    private final InetAddress publicIpAddress;

    /**
     * Name of the interface to which Docker default bridge is attached.
     */
    private final String accessInterfaceName;

    /**
     * Name of the Docker host interface on which network monitoring/management traffic will be exchanged.
     * On this interface dedicated VLANs will be configured during container deployment.
     */
    private final String dataInterfaceName;

    /**
     * Address of the base /9 network from which address pools for particular container deployment will be assigned.
     */
    private final InetAddress baseDataNetworkAddress;

    /**
     * Default root directory on the Docker host on which volumes will be created and mounted on deployed containers.
     */
    private final String volumesPath;

    /**
     * Helper flag indicating whether this Docker host should be always preferred in selection for new container deployment.
     */
    private final boolean preferred;

    public DockerHost(String name,
                      InetAddress apiIpAddress,
                      Integer apiPort,
                      InetAddress publicIpAddress,
                      String accessInterfaceName,
                      String dataInterfaceName,
                      InetAddress baseDataNetworkAddress,
                      String volumesPath,
                      boolean preferred) {
        this.name = name;
        this.apiIpAddress = apiIpAddress;
        this.apiPort = apiPort;
        this.publicIpAddress = publicIpAddress;
        this.accessInterfaceName = accessInterfaceName;
        this.dataInterfaceName = dataInterfaceName;
        this.baseDataNetworkAddress = baseDataNetworkAddress;
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

    public String getName() {
        return name;
    }

    public InetAddress getPublicIpAddress() {
        return publicIpAddress;
    }

    public String getAccessInterfaceName() {
        return accessInterfaceName;
    }

    public String getDataInterfaceName() {
        return dataInterfaceName;
    }

    public InetAddress getBaseDataNetworkAddress() {
        return baseDataNetworkAddress;
    }

    public String getVolumesPath() {
        return volumesPath;
    }

    public boolean isPreferred() {
        return preferred;
    }

}
