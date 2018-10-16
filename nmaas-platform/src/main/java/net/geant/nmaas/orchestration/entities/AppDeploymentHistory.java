package net.geant.nmaas.orchestration.entities;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
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

    @Column(nullable = true)
    private AppDeploymentState previousState;

    @Column(nullable = false)
    private AppDeploymentState currentState;

    public AppDeploymentHistory(AppDeployment app, Date timestamp, AppDeploymentState previousState, AppDeploymentState currentState) {
        this.app = app;
        this.timestamp = timestamp;
        this.previousState = previousState;
        this.currentState = currentState;
    }

    public String getPreviousStateString() {
        if(this.previousState == null){
            return null;
        }
        return this.previousState.lifecycleState().getUserFriendlyState();
    }

    public String getCurrentStateString() {
        return currentState.lifecycleState().getUserFriendlyState();
    }
}
