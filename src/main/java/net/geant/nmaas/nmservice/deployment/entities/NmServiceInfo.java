package net.geant.nmaas.nmservice.deployment.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.orchestration.Identifier;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Unique (within a domain) name of the service provided by the caller */
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
     *
     * Comment: This feature is not used
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private List<String> managedDevicesIpAddresses;

    /** Globally unique descriptive application deployment identifier */
    @Column(nullable = false)
    private Identifier descriptiveDeploymentId;

    /** GitLab project information created to store configuration files for this service (deployment) */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GitLabProject gitLabProject;

    /** Map of additional parameters provided by user during wizard completion */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private Map<String, String> additionalParameters;

    public NmServiceInfo(Identifier deploymentId, String deploymentName, String domain, Identifier descriptiveDeploymentId) {
        this.name = deploymentId.value();
        this.deploymentId = deploymentId;
        this.deploymentName = deploymentName;
        this.domain = domain;
        this.descriptiveDeploymentId = descriptiveDeploymentId;
    }

    public NmServiceInfo(Identifier deploymentId, String deploymentName, String domain, Identifier descriptiveDeploymentId, Map <String, String> additionalParameters) {
        this.name = deploymentId.value();
        this.deploymentId = deploymentId;
        this.deploymentName = deploymentName;
        this.domain = domain;
        this.descriptiveDeploymentId = descriptiveDeploymentId;
        this.additionalParameters = additionalParameters;
    }

    public void addAdditionalParameters(Map<String, String> newAdditionalParameters) {
        if(additionalParameters == null) {
            additionalParameters = new HashMap<>();
        }
        this.additionalParameters.putAll(newAdditionalParameters);
    }

}
