package net.geant.nmaas.kubernetes.remote;

public interface RemoteClusterMonitoringService {

    boolean clusterAvailable(Long id);

    void updateAllClusterState();

}
