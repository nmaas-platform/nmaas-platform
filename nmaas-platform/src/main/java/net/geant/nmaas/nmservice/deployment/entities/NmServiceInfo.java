package net.geant.nmaas.nmservice.deployment.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

/**
 * Abstract Network Management Service deployment information.
 * Extended by each {@link net.geant.nmaas.nmservice.deployment.ContainerOrchestrator}.
 */
@NoArgsConstructor
@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class NmServiceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Unique name of the service provided by the caller */
    @Column(nullable = false)
    private String name;

    /** State in which service should be at this point */
    @Column(nullable = false)
    private NmServiceDeploymentState state = NmServiceDeploymentState.INIT;

    /** Identifier of the application deployment assigned by application lifecycle manager */
    @Column(nullable = false, unique = true)
    private Identifier deploymentId;

    /** Name of the deployment provided by the user. */
    @Column(nullable = false)
    private String deploymentName;

    /** Name of the client domain for this deployment */
    @Column(nullable = false)
    private String domain;

    /**
     * The list of IP addresses of devices to be managed/monitored by the deployed service.
     * These addresses are provided by the user during wizard completion.
     * For these addresses specific routing entries needs to be by applied on the container once run.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> managedDevicesIpAddresses;

    /** GitLab project information created to store configuration files for this service (deployment) */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GitLabProject gitLabProject;

    public NmServiceInfo(Identifier deploymentId, String deploymentName, String domain) {
        this.name = deploymentId.value();
        this.deploymentId = deploymentId;
        this.deploymentName = deploymentName;
        this.domain = domain;
    }
}
