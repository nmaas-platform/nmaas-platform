package net.geant.nmaas.orchestration.entities;

import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.List;

/**
 * Application deployment specification. Contains information about supported deployment options represented by
 * {@link AppDeploymentEnv} and all required templates {@link DockerComposeFileTemplate}
 * and/or {@link KubernetesTemplate}) according to the supported deployment environments.
 */
@Getter
@Setter
@Entity
public class AppDeploymentSpec implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass = AppDeploymentEnv.class)
    @Enumerated(EnumType.STRING)
    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private DockerComposeFileTemplate dockerComposeFileTemplate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

    @Column(nullable = false)
    private Integer defaultStorageSpace;

    private boolean configFileRepositoryRequired;
}
