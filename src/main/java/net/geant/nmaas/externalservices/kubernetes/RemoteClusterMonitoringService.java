package net.geant.nmaas.externalservices.kubernetes;

public interface RemoteClusterMonitoringService {

    boolean clusterAvailable(Long id);
    void updateAllClusterState();

}
