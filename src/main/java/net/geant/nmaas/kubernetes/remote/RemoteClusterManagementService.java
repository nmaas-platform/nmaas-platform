package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface RemoteClusterManagementService {

    RemoteClusterView getCluster(Long id, Principal principal);

    KCluster getClusterEntity(Long id);

    List<RemoteClusterView> getAllClusters();

    List<RemoteClusterView> getClustersInDomain(Long domainId);

    RemoteClusterView processNewCluster(RemoteClusterView remoteClusterSpec, MultipartFile kubeConfigFile, boolean createNamespace);

    RemoteClusterView updateCluster(RemoteClusterView cluster, Long id);

    void removeCluster(Long id);

    boolean clusterExists(Long id);

    RemoteClusterView mapFile(RemoteClusterView view, MultipartFile file);
}
