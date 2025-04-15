package net.geant.nmaas.externalservices.kubernetes;

import net.geant.nmaas.externalservices.kubernetes.model.ClusterManager;
import net.geant.nmaas.externalservices.kubernetes.model.ClusterManagerView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClusterServiceTest {

    @Mock
    private ClusterManagerRepository clusterManagerRepository;

    @Mock
    private KubernetesClusterIngressManager kClusterIngressManager;

    @Mock
    private KubernetesClusterDeploymentManager kClusterDeploymentManager;

    @InjectMocks
    private ClusterService clusterService;

    private ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getClusterView_validId_returnsClusterManagerView() {
        Long id = 1L;
        ClusterManager clusterManager = ClusterManager.builder().id(id).name("Cluster").description("Description").build();
        ClusterManagerView clusterManagerView = modelMapper.map(clusterManager, ClusterManagerView.class);

        when(clusterManagerRepository.findById(id)).thenReturn(Optional.of(clusterManager));

        ClusterManagerView result = clusterService.getClusterView(id);

        assertEquals(clusterManagerView.getName(), result.getName());
        verify(clusterManagerRepository, times(1)).findById(id);
    }

    @Test
    void getClusterView_invalidId_throwsException() {
        Long id = 100L;
        when(clusterManagerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clusterService.getClusterView(id));
        verify(clusterManagerRepository, times(1)).findById(id);
    }

    @Test
    void getAllClusterView_returnsClusterManagerViews() {
        ClusterManager cluster1 = ClusterManager.builder().id(1L).name("Cluster1").build();
        ClusterManager cluster2 = ClusterManager.builder().id(2L).name("Cluster2").build();

        when(clusterManagerRepository.findAll()).thenReturn(List.of(cluster1, cluster2));

        List<ClusterManagerView> result = clusterService.getAllClusterView();

        assertEquals(2, result.size());
        verify(clusterManagerRepository, times(1)).findAll();
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