package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;

import java.security.Principal;
import java.util.List;

public interface RemoteClusterInfoService {

    RemoteKClusterDto getCluster(Long id, Principal principal);

    KCluster getClusterEntity(Long id);

    List<RemoteKClusterDto> getAllClusters();

    List<RemoteKClusterDto> getClustersInDomain(Long domainId);

}
