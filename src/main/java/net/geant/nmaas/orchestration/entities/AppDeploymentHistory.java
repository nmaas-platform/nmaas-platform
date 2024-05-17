package net.geant.nmaas.orchestration.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "app_deployment_history")
public class AppDeploymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "app_id")
    private AppDeployment app;

    @Column(nullable = false)
    private Date timestamp;

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
