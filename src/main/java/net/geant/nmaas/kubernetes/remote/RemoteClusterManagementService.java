package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterCompleteDto;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import org.springframework.web.multipart.MultipartFile;

public interface RemoteClusterManagementService extends RemoteClusterInfoService {

    void checkRequest(RemoteKClusterDto dto);

    RemoteKClusterDto processNewCluster(RemoteKClusterDto remoteClusterSpec, MultipartFile kubeConfigFile, boolean createNamespace);

    RemoteKClusterDto processNewCluster(RemoteKClusterDto remoteClusterSpec, boolean createNamespace, String namespace, String secretName);

    RemoteKClusterDto updateCluster(RemoteKClusterCompleteDto cluster, Long id);

    void removeCluster(Long id);

    boolean clusterExists(Long id);

    RemoteKClusterDto mapFile(RemoteKClusterDto dto, MultipartFile file);

    RemoteKClusterDto mapFile(RemoteKClusterDto dto, String secretNamespace, String secretName);

    void updateClusterStatus(Long id);
}
