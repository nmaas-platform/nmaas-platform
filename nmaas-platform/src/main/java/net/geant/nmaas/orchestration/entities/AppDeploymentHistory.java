package net.geant.nmaas.orchestration.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="app_deployment_history")
public class AppDeploymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "app_id")
    private AppDeployment app;

    @Column(nullable = false)
    private Date timestamp;

    @Column(nullable = false)
    private AppDeploymentState previousState;

    @Column(nullable = false)
    private AppDeploymentState currentState;

    public AppDeploymentHistory(){}

    public AppDeploymentHistory(AppDeployment app, Date timestamp, AppDeploymentState previousState, AppDeploymentState currentState) {
        this.app = app;
        this.timestamp = timestamp;
        this.previousState = previousState;
        this.currentState = currentState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppDeployment getApp() {
        return app;
    }

    public void setApp(AppDeployment app) {
        this.app = app;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public AppDeploymentState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(AppDeploymentState previousState) {
        this.previousState = previousState;
    }

    public AppDeploymentState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AppDeploymentState currentState) {
        this.currentState = currentState;
    }
}
