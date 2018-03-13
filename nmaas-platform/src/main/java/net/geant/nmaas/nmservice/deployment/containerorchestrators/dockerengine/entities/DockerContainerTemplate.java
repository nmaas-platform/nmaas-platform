package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_container_template")
public class DockerContainerTemplate {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column(name="template_id")
    private Long id;

    /**
     * The name of the image to use for the container
     */
    @Column(nullable=false)
    private String image;

    /**
     * The command to be run in the image at startup
     */
    private String command;

    /**
     * Port exposed by the service/container for UI access.
     * During container configuration for this port a published port needs to be assigned.
     */
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    private DockerContainerPortForwarding exposedPort;

    /**
     * A list of environment variables in the form of ["VAR=value"]
     */
    @ElementCollection(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> envVariables = new ArrayList<>();

    /**
     * This field represents all the directories on the container for which remote containerVolumes need to be mounted.
     * During container configuration for each listed volume a directory on the Docker Host needs to be assigned.
     */
    @ElementCollection(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> containerVolumes = new ArrayList<>();

    public DockerContainerTemplate() { }

    public DockerContainerTemplate(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerContainerTemplate that = (DockerContainerTemplate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (command != null ? !command.equals(that.command) : that.command != null) return false;
        if (exposedPort != null ? !exposedPort.equals(that.exposedPort) : that.exposedPort != null) return false;
        if (envVariables != null ? !envVariables.equals(that.envVariables) : that.envVariables != null) return false;
        return containerVolumes != null ? containerVolumes.equals(that.containerVolumes) : that.containerVolumes == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DockerContainerTemplate{" +
                "id=" + id +
                ", image='" + image + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public DockerContainerPortForwarding getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(DockerContainerPortForwarding exposedPort) {
        this.exposedPort = exposedPort;
    }

    public List<String> getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(List<String> envVariables) {
        this.envVariables = envVariables;
    }

    public List<String> getContainerVolumes() {
        return containerVolumes;
    }

    public void setContainerVolumes(List<String> containerVolumes) {
        this.containerVolumes = containerVolumes;
    }

    public static DockerContainerTemplate copy(DockerContainerTemplate toCopy) {
        DockerContainerTemplate template = new DockerContainerTemplate();
        template.setImage(toCopy.getImage());
        template.setCommand(toCopy.getCommand());
        template.setContainerVolumes(new ArrayList<>(toCopy.getContainerVolumes()));
        template.setEnvVariables(new ArrayList<>(toCopy.getEnvVariables()));
        if(toCopy.getExposedPort() != null)
            template.setExposedPort(DockerContainerPortForwarding.copy(toCopy.getExposedPort()));
        return template;
    }
}