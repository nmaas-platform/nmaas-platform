package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Application deployment specification. Contains information about supported deployment options represented by
 * {@link AppDeploymentEnv} and all required templates ({@link DockerContainerTemplate}, {@link DockerComposeFileTemplate}
 * and/or {@link KubernetesTemplate}) according to the supported deployment environments.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class AppDeploymentSpec implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass = AppDeploymentEnv.class)
    @Enumerated(EnumType.STRING)
    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DockerContainerTemplate dockerContainerTemplate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DockerComposeFileTemplate dockerComposeFileTemplate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

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

    public DockerComposeFileTemplate getDockerComposeFileTemplate() {
        return dockerComposeFileTemplate;
    }

    public void setDockerComposeFileTemplate(DockerComposeFileTemplate dockerComposeFileTemplate) {
        this.dockerComposeFileTemplate = dockerComposeFileTemplate;
    }

    public KubernetesTemplate getKubernetesTemplate() {
        return kubernetesTemplate;
    }

    public void setKubernetesTemplate(KubernetesTemplate kubernetesTemplate) {
        this.kubernetesTemplate = kubernetesTemplate;
    }
}
