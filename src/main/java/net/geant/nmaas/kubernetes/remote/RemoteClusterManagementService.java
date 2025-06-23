package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;

public interface RemoteClusterManagementService {

    RemoteClusterView getCluster(Long id, Principal principal);

    KCluster getClusterEntity(Long id);

    List<RemoteClusterView> getAllClusters();

    List<RemoteClusterView> getClustersInDomain(Long domainId);

    RemoteClusterView saveCluster(KCluster entity, MultipartFile file) throws IOException, NoSuchAlgorithmException;

    RemoteClusterView updateCluster(RemoteClusterView cluster, Long id);

    void removeCluster(Long id);

    boolean clusterExists(Long id);

    RemoteClusterView saveClusterFile(RemoteClusterView view, MultipartFile file);

    RemoteClusterView mapFile(RemoteClusterView view, MultipartFile file);
}
