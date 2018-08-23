package net.geant.nmaas.externalservices.api.model;

import java.net.InetAddress;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class KubernetesClusterView {

    private Long id;
    private String name;
    private InetAddress helmHostAddress;
    private InetAddress restApiHostAddress;
    private int restApiPort;

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

    public InetAddress getHelmHostAddress() {
        return helmHostAddress;
    }

    public void setHelmHostAddress(InetAddress helmHostAddress) {
        this.helmHostAddress = helmHostAddress;
    }

    public InetAddress getRestApiHostAddress() {
        return restApiHostAddress;
    }

    public void setRestApiHostAddress(InetAddress restApiHostAddress) {
        this.restApiHostAddress = restApiHostAddress;
    }

    public int getRestApiPort() {
        return restApiPort;
    }

    public void setRestApiPort(int restApiPort) {
        this.restApiPort = restApiPort;
    }
}
