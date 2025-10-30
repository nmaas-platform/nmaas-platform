package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.kubernetes.remote.api.model.RemoteClusterView;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;

import java.security.Principal;
import java.util.List;

public interface RemoteClusterInfoService {

    RemoteClusterView getCluster(Long id, Principal principal);

    KCluster getClusterEntity(Long id);

    List<RemoteClusterView> getAllClusters();

    List<RemoteClusterView> getClustersInDomain(Long domainId);

}
