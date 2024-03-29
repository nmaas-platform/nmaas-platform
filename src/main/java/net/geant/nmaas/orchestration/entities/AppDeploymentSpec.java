package net.geant.nmaas.orchestration.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Application deployment specification. Contains information about supported deployment options represented by
 * {@link AppDeploymentEnv} and all required templates like {@link KubernetesTemplate} and additional deployment
 * parameters.
 */
@Getter
@Setter
@Builder
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
    private boolean exposesWebUI;

    @Column(nullable = false)
    private boolean allowSshAccess;

    @Column(nullable = false)
    private boolean allowLogAccess;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<String, String> deployParameters;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<String, String> globalDeployParameters;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AppStorageVolume> storageVolumes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AppAccessMethod> accessMethods;

    public void validate(){
        checkArgument(kubernetesTemplate != null, "Kubernetes template cannot be null");
        checkArgument(accessMethods != null && accessMethods.size() > 0, "At least one access method has to be specified");
        //checkArgument(storageVolumes != null && storageVolumes.size() > 0, "At least one storage volume has to be specified");
    }
}
