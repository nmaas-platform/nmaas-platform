package net.geant.nmaas.externalservices.inventory.gitlab.model;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHostView {

    private String name;
    private String apiIpAddress;
    private Integer apiPort;
    private String publicIpAddress;
    private boolean preferred;

    public DockerHostView() {
    }

    public DockerHostView(String name, String apiIpAddress, Integer apiPort, String publicIpAddress, boolean preferred) {
        this.name = name;
        this.apiIpAddress = apiIpAddress;
        this.apiPort = apiPort;
        this.publicIpAddress = publicIpAddress;
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

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }
}
