package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.kubernetes.remote.api.model.RemoteClusterView;
import org.springframework.web.multipart.MultipartFile;

public interface RemoteClusterManagementService extends RemoteClusterInfoService {

    RemoteClusterView processNewCluster(RemoteClusterView remoteClusterSpec, MultipartFile kubeConfigFile, boolean createNamespace);

    RemoteClusterView updateCluster(RemoteClusterView cluster, Long id);

    void removeCluster(Long id);

    boolean clusterExists(Long id);

    RemoteClusterView mapFile(RemoteClusterView view, MultipartFile file);
}
