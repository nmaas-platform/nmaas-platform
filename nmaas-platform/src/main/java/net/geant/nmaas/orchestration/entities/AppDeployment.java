package net.geant.nmaas.orchestration.entities;

import org.springframework.stereotype.Component;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="app_deployment")
public class AppDeployment {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="id")
    private Long id;

    @Column(nullable = false)
    private Identifier deploymentId;

    @Column(nullable = false)
    private Identifier clientId;

    @Column(nullable = false)
    private Identifier applicationId;

    @Column(nullable = false)
    private AppDeploymentState state = AppDeploymentState.REQUESTED;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AppConfiguration configuration;

    public AppDeployment() {
    }

    public AppDeployment(Identifier deploymentId, Identifier clientId, Identifier applicationId) {
        this.deploymentId = deploymentId;
        this.clientId = clientId;
        this.applicationId = applicationId;
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

    public Identifier getClientId() {
        return clientId;
    }

    public void setClientId(Identifier clientId) {
        this.clientId = clientId;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Identifier applicationId) {
        this.applicationId = applicationId;
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
