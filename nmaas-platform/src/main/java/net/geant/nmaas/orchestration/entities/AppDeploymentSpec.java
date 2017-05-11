package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class AppDeploymentSpec implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ElementCollection(targetClass = AppDeploymentEnv.class)
    @Enumerated(EnumType.STRING)
    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.LAZY)
    private DockerContainerTemplate dockerContainerTemplate;

    public AppDeploymentSpec() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AppDeploymentEnv> getSupportedDeploymentEnvironments() {
        return supportedDeploymentEnvironments;
    }

    public void setSupportedDeploymentEnvironments(List<AppDeploymentEnv> supportedDeploymentEnvironments) {
        this.supportedDeploymentEnvironments = supportedDeploymentEnvironments;
    }

    public DockerContainerTemplate getDockerContainerTemplate() {
        return dockerContainerTemplate;
    }

    public void setDockerContainerTemplate(DockerContainerTemplate dockerContainerTemplate) {
        this.dockerContainerTemplate = dockerContainerTemplate;
    }
}
