package net.geant.nmaas.externalservices.api.model;

import java.net.InetAddress;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerHostView {

    private String name;
    private InetAddress apiIpAddress;
    private Integer apiPort;
    private InetAddress publicIpAddress;
    private boolean preferred;

    public DockerHostView() {
    }

    public DockerHostView(String name, InetAddress apiIpAddress, Integer apiPort, InetAddress publicIpAddress, boolean preferred) {
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

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }
}
