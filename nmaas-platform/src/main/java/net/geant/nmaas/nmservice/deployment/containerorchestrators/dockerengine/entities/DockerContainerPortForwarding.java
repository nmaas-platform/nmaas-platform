package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_container_port_forwarding")
public class DockerContainerPortForwarding implements Serializable {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="port_forwarding_id")
    private Long id;

    private Protocol protocol;

    private Integer targetPort;

    public DockerContainerPortForwarding() { }

    public DockerContainerPortForwarding(Protocol protocol, Integer targetPort) {
        this.protocol = protocol;
        this.targetPort = targetPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerContainerPortForwarding that = (DockerContainerPortForwarding) o;

        if (protocol != that.protocol) return false;
        return targetPort != null ? targetPort.equals(that.targetPort) : that.targetPort == null;
    }

    @Override
    public int hashCode() {
        int result = protocol != null ? protocol.hashCode() : 0;
        result = 31 * result + (targetPort != null ? targetPort.hashCode() : 0);
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Integer getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(Integer targetPort) {
        this.targetPort = targetPort;
    }

    public static DockerContainerPortForwarding copy(DockerContainerPortForwarding toCopy) {
        DockerContainerPortForwarding copy = new DockerContainerPortForwarding();
        copy.setProtocol(toCopy.getProtocol());
        copy.setTargetPort(toCopy.getTargetPort());
        return copy;
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
