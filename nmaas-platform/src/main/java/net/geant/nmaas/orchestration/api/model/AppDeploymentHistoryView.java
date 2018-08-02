package net.geant.nmaas.orchestration.api.model;

import java.util.Date;

public class AppDeploymentHistoryView {
    private Date timestamp;
    private String previousState;
    private String currentState;

    public AppDeploymentHistoryView(Date timestamp, String previousState, String currentState) {
        this.timestamp = timestamp;
        this.previousState = previousState;
        this.currentState = currentState;
    }

    public AppDeploymentHistoryView(){}

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
