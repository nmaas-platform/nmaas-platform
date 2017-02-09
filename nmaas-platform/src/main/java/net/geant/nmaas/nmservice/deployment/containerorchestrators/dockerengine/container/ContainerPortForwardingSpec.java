package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerPortForwardingSpec {

    private String name;

    private Protocol protocol;

    private Integer targetPort;

    private Integer publishedPort;

    public ContainerPortForwardingSpec(String name, Protocol protocol, Integer targetPort) {
        this.name = name;
        this.protocol = protocol;
        this.targetPort = targetPort;
    }

    public String getName() {
        return name;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Integer getTargetPort() {
        return targetPort;
    }

    public void setPublishedPort(Integer publishedPort) {
        this.publishedPort = publishedPort;
    }

    public Integer getPublishedPort() {
        return publishedPort;
    }

    public enum Protocol {
        TCP("tcp"),
        UDP("udp");

        private String value;

        Protocol(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
