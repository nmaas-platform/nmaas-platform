package net.geant.nmaas.externalservices.api.model;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHostDetails {

    private String name;
    private String apiIpAddress;
    private Integer apiPort;
    private String publicIpAddress;
    private String accessInterfaceName;
    private String dataInterfaceName;
    private String baseDataNetworkAddress;
    private String workingPath;
    private String volumesPath;
    private boolean preferred;

    public DockerHostDetails() {
    }

    public DockerHostDetails(String name, String apiIpAddress, Integer apiPort, String publicIpAddress, String accessInterfaceName, String dataInterfaceName, String baseDataNetworkAddress, String workingPath, String volumesPath, boolean preferred) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiIpAddress() {
        return apiIpAddress;
    }

    public void setApiIpAddress(String apiIpAddress) {
        this.apiIpAddress = apiIpAddress;
    }

    public Integer getApiPort() {
        return apiPort;
    }

    public void setApiPort(Integer apiPort) {
        this.apiPort = apiPort;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
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

    public String getBaseDataNetworkAddress() {
        return baseDataNetworkAddress;
    }

    public void setBaseDataNetworkAddress(String baseDataNetworkAddress) {
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
}
