package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import org.springframework.web.multipart.MultipartFile;

public interface RemoteClusterManagementService extends RemoteClusterInfoService {

    void checkRequest(RemoteKClusterDto view);

    RemoteKClusterDto processNewCluster(RemoteKClusterDto remoteClusterSpec, MultipartFile kubeConfigFile, boolean createNamespace);

    RemoteKClusterDto processNewCluster(RemoteKClusterDto remoteClusterSpec, boolean createNamespace, String namespace, String secretName);

    RemoteKClusterDto updateCluster(RemoteKClusterDto cluster, Long id);

    void removeCluster(Long id);

    boolean clusterExists(Long id);

    RemoteKClusterDto mapFile(RemoteKClusterDto view, MultipartFile file);

    RemoteKClusterDto mapFile(RemoteKClusterDto view, String secretNamespace, String secretName);

    void updateClusterStatus(Long id);
}
