package net.geant.nmaas.orchestration.projections;

public interface AppDeploymentCount {
    String getApplicationName();
    Long getCount();
}
