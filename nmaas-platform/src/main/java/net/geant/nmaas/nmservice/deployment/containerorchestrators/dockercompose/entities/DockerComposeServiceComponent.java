package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_compose_service_component")
public class DockerComposeServiceComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String deploymentName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String ipAddressOfContainer;

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

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddressOfContainer() {
        return ipAddressOfContainer;
    }

    public void setIpAddressOfContainer(String ipAddressOfContainer) {
        this.ipAddressOfContainer = ipAddressOfContainer;
    }
}
