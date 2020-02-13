package net.geant.nmaas.orchestration.entities;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import java.util.Map;

/**
 * Application deployment specification. Contains information about supported deployment options represented by
 * {@link AppDeploymentEnv} and all required templates like {@link KubernetesTemplate} and additional deployment
 * parameters.
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AppDeploymentSpec implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass = AppDeploymentEnv.class)
    @Enumerated(EnumType.STRING)
    private List<AppDeploymentEnv> supportedDeploymentEnvironments;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

    @Column(nullable = false)
    private Integer defaultStorageSpace;

    @Column(nullable = false)
    private boolean exposesWebUI;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<ParameterType, String> deployParameters;

    public void validate(){
        checkArgument(kubernetesTemplate != null, "Kubernetes template cannot be null");
        checkArgument(defaultStorageSpace != null && defaultStorageSpace > 0, "Default storage space cannot be lower than 0GB");
    }
}
