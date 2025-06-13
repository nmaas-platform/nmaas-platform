package net.geant.nmaas.externalservices.kubernetes;

import net.geant.nmaas.externalservices.kubernetes.api.model.RemoteClusterView;
import net.geant.nmaas.externalservices.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.kubernetes.repositories.KClusterRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClusterServiceTest {

    private final KClusterRepository kClusterRepository = mock(KClusterRepository.class);
    private final KubernetesClusterIngressManager kClusterIngressManager = mock(KubernetesClusterIngressManager.class);
    private final KubernetesClusterDeploymentManager kClusterDeploymentManager = mock(KubernetesClusterDeploymentManager.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final DomainService domainService = mock(DomainService.class);
    private final UserService userService = mock(UserService.class);

    private final RemoteClusterManager remoteClusterManager = new RemoteClusterManager(
            kClusterRepository, kClusterIngressManager, kClusterDeploymentManager, domainService, null, userService, modelMapper);


    private Domain globalDomain;
    private Domain specificDomain;
    private KCluster cluster1;
    private KCluster cluster2;
    private KCluster cluster3;
    private Principal mockPrincipal;


    @BeforeEach
    void setUp() {
        globalDomain = new Domain("global", "global.example.com");
        globalDomain.setId(1L);
        specificDomain = new Domain("domain", "specific.example.com");
        specificDomain.setId(2L);

        cluster1 = KCluster.builder().id(100L).name("ClusterA").build();
        cluster2 = KCluster.builder().id(101L).name("ClusterB").build();
        cluster3 = KCluster.builder().id(102L).name("ClusterC").build();


        cluster1.setDomains(Arrays.asList(specificDomain));
        cluster2.setDomains(Arrays.asList(specificDomain, globalDomain));
        cluster3.setDomains(Arrays.asList(globalDomain));

        mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testUser");
    }

    @Test
    void getClusterView_validId_returnsRemoteClusterView() throws AccessDeniedException {
        Long id = 1L;
        KCluster remoteCluster = KCluster.builder().id(id).name("Cluster").description("Description").build();
        remoteCluster.setDomains(List.of(specificDomain));

        when(kClusterRepository.findById(id)).thenReturn(Optional.of(remoteCluster));
        when(userService.isAdmin(anyString())).thenReturn(true);

        RemoteClusterView result = remoteClusterManager.getClusterView(id, mockPrincipal);

        assertEquals(remoteCluster.getName(), result.getName());
        verify(kClusterRepository, times(1)).findById(id);
        verify(userService, times(1)).isAdmin(mockPrincipal.getName());
        verify(userService, never()).isUserAdminInAnyDomain(anyList(), anyString());
    }


    @Test
    void getClusterView_validId_domainAdminUser_returnsRemoteClusterView() throws AccessDeniedException {
        Long id = 1L;
        KCluster remoteCluster = KCluster.builder().id(id).name("Cluster").description("Description").build();
        remoteCluster.setDomains(List.of(specificDomain));

        when(kClusterRepository.findById(id)).thenReturn(Optional.of(remoteCluster));
        when(userService.isAdmin(anyString())).thenReturn(false);
        when(userService.isUserAdminInAnyDomain(anyList(), anyString())).thenReturn(true);

        RemoteClusterView result = remoteClusterManager.getClusterView(id, mockPrincipal);

        assertEquals(remoteCluster.getName(), result.getName());
        verify(kClusterRepository, times(1)).findById(id);
        verify(userService, times(1)).isAdmin(mockPrincipal.getName());
        verify(userService, times(1)).isUserAdminInAnyDomain(remoteCluster.getDomains(), mockPrincipal.getName());
    }

    @Test
    void getClusterView_validId_unauthorizedUser_throwsAccessDeniedException() {
        Long id = 1L;
        KCluster remoteCluster = KCluster.builder().id(id).name("Cluster").description("Description").build();
        remoteCluster.setDomains(List.of(specificDomain));

        when(kClusterRepository.findById(id)).thenReturn(Optional.of(remoteCluster));
        when(userService.isAdmin(anyString())).thenReturn(false);
        when(userService.isUserAdminInAnyDomain(anyList(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> remoteClusterManager.getClusterView(id, mockPrincipal));

        verify(kClusterRepository, times(1)).findById(id);
        verify(userService, times(1)).isAdmin(mockPrincipal.getName());
        verify(userService, times(1)).isUserAdminInAnyDomain(remoteCluster.getDomains(), mockPrincipal.getName());
    }


    @Test
    void getClusterView_invalidId_throwsException() {
        Long id = 100L;
        when(kClusterRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> remoteClusterManager.getClusterView(id, mockPrincipal));
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

    @Test
    void shouldReturnAllClustersWhenDomainIdIsGlobalDomainId() {
        // Given
        Long globalDomainId = globalDomain.getId();
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(globalDomain));
        when(kClusterRepository.findAll()).thenReturn(Arrays.asList(cluster1, cluster2, cluster3));

        // When
        List<RemoteClusterView> result = remoteClusterManager.getClustersInDomain(globalDomainId);

        // Then
        verify(kClusterRepository, times(1)).findAll();
        verify(kClusterRepository, never()).findByDomains_Id(anyLong());
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getId().equals(cluster1.getId())));
        assertTrue(result.stream().anyMatch(v -> v.getId().equals(cluster2.getId())));
        assertTrue(result.stream().anyMatch(v -> v.getId().equals(cluster3.getId())));

    }

    @Test
    void shouldReturnClustersAssociatedWithSpecificDomainWhenDomainIsNotGlobal() {
        // Given
        Long specificDomainId = specificDomain.getId();
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(globalDomain));
        when(kClusterRepository.findByDomains_Id(specificDomainId)).thenReturn(Arrays.asList(cluster1, cluster2));

        // When
        List<RemoteClusterView> result = remoteClusterManager.getClustersInDomain(specificDomainId);

        // Then
        verify(kClusterRepository, times(1)).findByDomains_Id(specificDomainId);
        verify(kClusterRepository, never()).findAll();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getId().equals(cluster1.getId())));
        assertTrue(result.stream().anyMatch(v -> v.getId().equals(cluster2.getId())));

    }

    @Test
    void shouldReturnClustersAssociatedWithSpecificDomainWhenGlobalDomainDoesNotExist() {
        // Given
        Long specificDomainId = specificDomain.getId();
        when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
        when(kClusterRepository.findByDomains_Id(specificDomainId)).thenReturn(Arrays.asList(cluster1));

        // When
        List<RemoteClusterView> result = remoteClusterManager.getClustersInDomain(specificDomainId);

        // Then
        verify(kClusterRepository, times(1)).findByDomains_Id(specificDomainId);
        verify(kClusterRepository, never()).findAll();
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getId().equals(cluster1.getId())));
    }
}