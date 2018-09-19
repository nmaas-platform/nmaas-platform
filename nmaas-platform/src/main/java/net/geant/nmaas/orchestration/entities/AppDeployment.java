package net.geant.nmaas.orchestration.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Details of single application deployment in the system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="app_deployment")
@Getter
@Setter
public class AppDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Unique identifier of this deployment. */
    @Column(nullable = false, unique = true)
    private Identifier deploymentId;

    /** Name of the client domain for this deployment. */
    @Column(nullable = false)
    private String domain;

    /** Identifier of the application being deployed. */
    @Column(nullable = false)
    private Identifier applicationId;

    /** Name of the deployment provided by the user. */
    @Column(nullable = false)
    private String deploymentName;

    /** Current deployment state. */
    @Column(nullable = false)
    private AppDeploymentState state = AppDeploymentState.REQUESTED;

    /** Initial application configuration provided by the user. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AppConfiguration configuration;

    /** Store all of deployment state changes */
    @OneToMany(mappedBy = "app", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AppDeploymentHistory> history = new ArrayList<>();

    /** Indicates if GitLab instance is required during deployment */
    @Column(nullable = false)
    private boolean configFileRepositoryRequired;

    /** Required storage space to be allocated for this particular instance in GB */
    private Double storageSpace;

    public AppDeployment() { }

    public AppDeployment(Identifier deploymentId, String domain, Identifier applicationId, String deploymentName, boolean configFileRepositoryRequired, Double storageSpace) {
        this.deploymentId = deploymentId;
        this.domain = domain;
        this.applicationId = applicationId;
        this.deploymentName = deploymentName;
        this.configFileRepositoryRequired = configFileRepositoryRequired;
        this.storageSpace = storageSpace;
    }
}
