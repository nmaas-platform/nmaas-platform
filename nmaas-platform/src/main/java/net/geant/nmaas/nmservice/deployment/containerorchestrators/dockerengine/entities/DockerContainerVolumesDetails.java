package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_container_volumes_details")
public class DockerContainerVolumesDetails {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable=false)
    private String attachedVolumeName;

    public DockerContainerVolumesDetails() { }

    public DockerContainerVolumesDetails(String attachedVolumeName) {
        this.attachedVolumeName = attachedVolumeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttachedVolumeName() {
        return attachedVolumeName;
    }

    public void setAttachedVolumeName(String attachedVolumeName) {
        this.attachedVolumeName = attachedVolumeName;
    }

    @Override
    public String toString() {
        return "DockerContainerVolumesDetails{" +
                "attachedVolumeName='" + attachedVolumeName + '\'' +
                '}';
    }
}
