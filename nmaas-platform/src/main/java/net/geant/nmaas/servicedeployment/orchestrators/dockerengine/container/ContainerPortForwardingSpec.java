package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerPortForwardingSpec {

    private String name;

    private Protocol protocol;

    private Integer targetPort;

    private Integer publishedPort;

    public ContainerPortForwardingSpec(String name, Protocol protocol, Integer targetPort, Integer publishedPort) {
        this.name = name;
        this.protocol = protocol;
        this.targetPort = targetPort;
        this.publishedPort = publishedPort;
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

    public Integer getPublishedPort() {
        return publishedPort;
    }

    private enum Protocol {
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
