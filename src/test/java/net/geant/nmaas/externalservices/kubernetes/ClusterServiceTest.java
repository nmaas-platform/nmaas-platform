package net.geant.nmaas.externalservices.kubernetes;

import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.kubernetes.repositories.KClusterRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClusterServiceTest {

    private final KClusterRepository kClusterRepository = mock(KClusterRepository.class);
    private final KubernetesClusterIngressManager kClusterIngressManager = mock(KubernetesClusterIngressManager.class);
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager = mock(KubernetesClusterDeploymentManager.class);

    private final ModelMapper modelMapper = new ModelMapper();

    private final RemoteClusterManager remoteClusterManager = new RemoteClusterManager(
            kClusterRepository, kClusterIngressManager, kClusterDeploymentManager, null, null, modelMapper);

    @Test
    void getClusterView_validId_returnsRemoteClusterView() {
        Long id = 1L;
        KCluster remoteCluster = KCluster.builder().id(id).name("Cluster").description("Description").build();
        RemoteClusterView remoteClusterView = modelMapper.map(remoteCluster, RemoteClusterView.class);
        when(kClusterRepository.findById(id)).thenReturn(Optional.of(remoteCluster));

        RemoteClusterView result = remoteClusterManager.getClusterView(id);

        assertEquals(remoteClusterView.getName(), result.getName());
        verify(kClusterRepository, times(1)).findById(id);
    }

    @Test
    void getClusterView_invalidId_throwsException() {
        Long id = 100L;
        when(kClusterRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> remoteClusterManager.getClusterView(id));
        verify(kClusterRepository, times(1)).findById(id);
    }

    @Test
    void getAllClusterView_returnsClusterManagerViews() {
        KCluster cluster1 = KCluster.builder().id(1L).name("Cluster1").build();
        KCluster cluster2 = KCluster.builder().id(2L).name("Cluster2").build();

        when(kClusterRepository.findAll()).thenReturn(List.of(cluster1, cluster2));

        List<RemoteClusterView> result = remoteClusterManager.getAllClusterView();

        assertEquals(2, result.size());
        verify(kClusterRepository, times(1)).findAll();
    }

//    @Test
//    void saveCluster_validInput_savesCluster() throws IOException, NoSuchAlgorithmException {
//        MultipartFile multipartFile = mock(MultipartFile.class);
//        ClusterManager clusterManager = ClusterManager.builder().name("Cluster").description("Description").build();
//
//        when(clusterManagerRepository.save(any(ClusterManager.class))).thenReturn(clusterManager);
//
//        // Load config.yaml from test/resources directory
//        ClassLoader classLoader = getClass().getClassLoader();
//        try (var inputStream = classLoader.getResourceAsStream("test/resources/config.yaml")) {
//            when(multipartFile.getInputStream()).thenReturn(inputStream);
//
//            ClusterManagerView result = clusterService.saveCluster(clusterManager, multipartFile);
//            assertEquals(clusterManager.getName(), result.getName());
//            verify(clusterManagerRepository, times(1)).save(clusterManager);
//        }
//    }
//
//    @Test
//    void updateCluster_validInput_updatesCluster() {
//        Long id = 1L;
//        ClusterManager existingCluster = ClusterManager.builder().id(id).name("OldName").build();
//        ClusterManagerView updatedView = ClusterManagerView.builder().id(id).name("NewName").build();
//
//        when(clusterManagerRepository.findById(id)).thenReturn(Optional.of(existingCluster));
//        when(clusterManagerRepository.save(any(ClusterManager.class))).thenReturn(existingCluster);
//
//        ClusterManagerView result = clusterService.updateCluster(updatedView, id);
//
//        assertEquals("NewName", result.getName());
//        verify(clusterManagerRepository, times(1)).save(any(ClusterManager.class));
//    }
}