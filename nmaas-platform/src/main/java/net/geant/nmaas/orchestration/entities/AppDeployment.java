package net.geant.nmaas.orchestration.entities;

import javax.persistence.*;

/**
 * Details of single application deployment in the system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="app_deployment")
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

    public AppDeployment() { }

    public AppDeployment(Identifier deploymentId, String domain, Identifier applicationId, String deploymentName) {
        this.deploymentId = deploymentId;
        this.domain = domain;
        this.applicationId = applicationId;
        this.deploymentName = deploymentName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(Identifier deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Identifier applicationId) {
        this.applicationId = applicationId;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public AppDeploymentState getState() {
        return state;
    }

    public void setState(AppDeploymentState state) {
        this.state = state;
    }

    public AppConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AppConfiguration configuration) {
        this.configuration = configuration;
    }

}
