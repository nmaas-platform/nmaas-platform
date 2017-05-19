package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeFileInput {

    /**
     * It is assumed that such network is pre-configured manually on all Docker Hosts supporting Docker Compose -based
     * container deployment. Exemplary command to be issued: "docker network create -d bridge nmaas-ext-access".
     */
    public static final String DEFAULT_EXTERNAL_ACCESS_NETWORK_NAME = "nmaas-ext-access";

    private int port;

    private String volume;

    private String containerIpAddress;

    private String externalAccessNetworkName = DEFAULT_EXTERNAL_ACCESS_NETWORK_NAME;

    private String dcnNetworkName;

    public DockerComposeFileInput(int port, String volume) {
        this.port = port;
        this.volume = volume;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getContainerIpAddress() {
        return containerIpAddress;
    }

    public void setContainerIpAddress(String containerIpAddress) {
        this.containerIpAddress = containerIpAddress;
    }

    public String getExternalAccessNetworkName() {
        return externalAccessNetworkName;
    }

    public void setExternalAccessNetworkName(String externalAccessNetworkName) {
        this.externalAccessNetworkName = externalAccessNetworkName;
    }

    public String getDcnNetworkName() {
        return dcnNetworkName;
    }

    public void setDcnNetworkName(String dcnNetworkName) {
        this.dcnNetworkName = dcnNetworkName;
    }
}
