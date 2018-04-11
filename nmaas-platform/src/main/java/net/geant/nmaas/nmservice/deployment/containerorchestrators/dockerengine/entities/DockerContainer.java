package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_container")
public class DockerContainer {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /**
     * Network details for deployed container obtained from remote OSS system.
     */
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    private DockerContainerNetDetails networkDetails;

    /**
     * Directories on the host in which configuration files for this container should be placed.
     */
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true, optional=false)
    private DockerContainerVolumesDetails volumesDetails;

    /**
     * Identifier assigned by Docker Engine during container deployment.
     */
    private String deploymentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DockerContainerNetDetails getNetworkDetails() {
        return networkDetails;
    }

    public void setNetworkDetails(DockerContainerNetDetails networkDetails) {
        this.networkDetails = networkDetails;
    }

    public DockerContainerVolumesDetails getVolumesDetails() {
        return volumesDetails;
    }

    public void setVolumesDetails(DockerContainerVolumesDetails volumesDetails) {
        this.volumesDetails = volumesDetails;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerContainer that = (DockerContainer) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
