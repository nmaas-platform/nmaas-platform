package net.geant.nmaas.kubernetes.remote.api;

import net.geant.nmaas.api.dto.kubernetes.KClusterStateDto;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterBaseDto;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterCompleteDto;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import net.geant.nmaas.kubernetes.remote.api.exceptions.RemoteClusterValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RemoteClusterManagerControllerTest {

    private static final String CLUSTER_BASE_URL = "/api/v1/management/cluster";

    private final RemoteClusterManagementService remoteClusterManager = mock(RemoteClusterManagementService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mvc;

    private final Principal principal = mock(Principal.class);

    @BeforeEach
    void setUp() {
        RemoteClusterManagerController controller = new RemoteClusterManagerController(remoteClusterManager, objectMapper);
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .addPlaceholderValue("nmaas.api.version", "v1")
                .setControllerAdvice(new RemoveClusterManagerAdvice())
                .build();
    }

    private static RemoteKClusterDto clusterDto(Long id, String name, String codename) {
        return RemoteKClusterDto.builder()
                .id(id)
                .name(name)
                .codename(codename)
                .state(KClusterStateDto.UP)
                .build();
    }

    @Test
    void shouldGetClusterById() throws Exception {
        when(remoteClusterManager.getCluster(eq(1L), any(Principal.class)))
                .thenReturn(clusterDto(1L, "Cluster", "cluster"));

        mvc.perform(get(CLUSTER_BASE_URL + "/1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cluster"))
                .andExpect(jsonPath("$.codename").value("cluster"))
                .andExpect(jsonPath("$.state").value("UP"));

        verify(remoteClusterManager, times(1)).getCluster(1L, principal);
    }

    @Test
    void shouldGetClusterForEdit() throws Exception {
        when(remoteClusterManager.getClusterForEdit(eq(1L), any(Principal.class)))
                .thenReturn(RemoteKClusterCompleteDto.builder()
                        .id(1L)
                        .name("Cluster")
                        .codename("cluster")
                        .configFileContent("kube-config-content")
                        .build());

        mvc.perform(get(CLUSTER_BASE_URL + "/1/complete").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cluster"));

        verify(remoteClusterManager, times(1)).getClusterForEdit(1L, principal);
    }

    @Test
    void shouldGetAllClusters() throws Exception {
        when(remoteClusterManager.getAllClusters()).thenReturn(List.of(
                clusterDto(1L, "Cluster", "cluster"),
                clusterDto(2L, "Other", "other")
        ));

        mvc.perform(get(CLUSTER_BASE_URL + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(remoteClusterManager, times(1)).getAllClusters();
    }

    @Test
    void shouldGetAllClustersBaseInfo() throws Exception {
        when(remoteClusterManager.getAllClustersBase()).thenReturn(List.of(
                RemoteKClusterBaseDto.builder()
                        .id(1L)
                        .name("Cluster")
                        .codename("cluster")
                        .state(KClusterStateDto.UP)
                        .build()
        ));

        mvc.perform(get(CLUSTER_BASE_URL + "/base"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cluster"));

        verify(remoteClusterManager, times(1)).getAllClustersBase();
    }

    @Test
    void shouldGetClustersInDomain() throws Exception {
        when(remoteClusterManager.getClustersInDomain(1L)).thenReturn(List.of(
                clusterDto(1L, "Cluster", "cluster")
        ));

        mvc.perform(get(CLUSTER_BASE_URL + "/domain/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cluster"));

        verify(remoteClusterManager, times(1)).getClustersInDomain(1L);
    }

    @Test
    void shouldCreateClusterFromKubeConfigFile() throws Exception {
        RemoteKClusterDto created = clusterDto(1L, "Cluster", "cluster");
        when(remoteClusterManager.processNewCluster(isA(RemoteKClusterDto.class), isA(MultipartFile.class), eq(false)))
                .thenReturn(created);

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile("file", "kubeconfig", "text/plain",
                "kube-config-content".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile createNamespace = new MockMultipartFile("createNamespace", "", "text/plain",
                "false".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL)
                        .file(data)
                        .file(file)
                        .file(createNamespace))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cluster"));

        verify(remoteClusterManager, times(1)).checkRequest(isA(RemoteKClusterDto.class));
        verify(remoteClusterManager, times(1)).processNewCluster(isA(RemoteKClusterDto.class), isA(MultipartFile.class), eq(false));
    }

    @Test
    void shouldCreateClusterFromSecret() throws Exception {
        RemoteKClusterDto created = clusterDto(1L, "Cluster", "cluster");
        when(remoteClusterManager.processNewCluster(isA(RemoteKClusterDto.class), eq(true), eq("kube-system"), eq("kubeconfig-secret")))
                .thenReturn(created);

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile secretNamespace = new MockMultipartFile("secretNamespace", "", "text/plain",
                "kube-system".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile secretName = new MockMultipartFile("secretName", "", "text/plain",
                "kubeconfig-secret".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile createNamespace = new MockMultipartFile("createNamespace", "", "text/plain",
                "true".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL)
                        .file(data)
                        .file(secretNamespace)
                        .file(secretName)
                        .file(createNamespace))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(remoteClusterManager, times(1)).processNewCluster(isA(RemoteKClusterDto.class), eq(true), eq("kube-system"), eq("kubeconfig-secret"));
    }

    @Test
    void shouldReturnBadRequestWhenNeitherFileNorSecretProvided() throws Exception {
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile createNamespace = new MockMultipartFile("createNamespace", "", "text/plain",
                "false".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL)
                        .file(data)
                        .file(createNamespace))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You need either to upload the kubeConfig file or name of secret object that holds the respective kubeConfig file in the local cluster"));
    }

    @Test
    void shouldReturnBadRequestWhenClusterPayloadIsInvalid() throws Exception {
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "not-a-json".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile("file", "kubeconfig", "text/plain",
                "kube-config-content".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile createNamespace = new MockMultipartFile("createNamespace", "", "text/plain",
                "false".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL)
                        .file(data)
                        .file(file)
                        .file(createNamespace))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid cluster request payload"));
    }

    @Test
    void shouldReturnConflictWhenRequestValidationFails() throws Exception {
        doThrowOnCheckRequest("Cluster codename is already in use");

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile("file", "kubeconfig", "text/plain",
                "kube-config-content".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile createNamespace = new MockMultipartFile("createNamespace", "", "text/plain",
                "false".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL)
                        .file(data)
                        .file(file)
                        .file(createNamespace))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Cluster codename is already in use"));
    }

    private void doThrowOnCheckRequest(String message) {
        doThrow(new RemoteClusterValidationException(message))
                .when(remoteClusterManager).checkRequest(isA(RemoteKClusterDto.class));
    }

    @Test
    void shouldUpdateCluster() throws Exception {
        when(remoteClusterManager.updateCluster(isA(RemoteKClusterDto.class), eq(1L)))
                .thenReturn(clusterDto(1L, "Cluster", "cluster"));

        mvc.perform(put(CLUSTER_BASE_URL + "/1")
                        .contentType("application/json")
                        .content("{\"name\":\"Cluster\",\"codename\":\"cluster\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cluster"));

        verify(remoteClusterManager, times(1)).updateCluster(isA(RemoteKClusterDto.class), eq(1L));
    }

    @Test
    void shouldDeleteCluster() throws Exception {
        mvc.perform(delete(CLUSTER_BASE_URL + "/1"))
                .andExpect(status().isOk());

        verify(remoteClusterManager, times(1)).removeCluster(1L);
    }

    @Test
    void shouldReadClusterFromKubeConfigFile() throws Exception {
        when(remoteClusterManager.mapFile(isA(RemoteKClusterDto.class), isA(MultipartFile.class)))
                .thenReturn(clusterDto(null, "Cluster", "cluster"));

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile("file", "kubeconfig", "text/plain",
                "kube-config-content".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL + "/read")
                        .file(data)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cluster"));

        verify(remoteClusterManager, times(1)).mapFile(isA(RemoteKClusterDto.class), isA(MultipartFile.class));
    }

    @Test
    void shouldReadClusterFromSecret() throws Exception {
        when(remoteClusterManager.mapFile(isA(RemoteKClusterDto.class), eq("kube-system"), eq("kubeconfig-secret")))
                .thenReturn(clusterDto(null, "Cluster", "cluster"));

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile secretNamespace = new MockMultipartFile("secretNamespace", "", "text/plain",
                "kube-system".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile secretName = new MockMultipartFile("secretName", "", "text/plain",
                "kubeconfig-secret".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL + "/read")
                        .file(data)
                        .file(secretNamespace)
                        .file(secretName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cluster"));

        verify(remoteClusterManager, times(1)).mapFile(isA(RemoteKClusterDto.class), eq("kube-system"), eq("kubeconfig-secret"));
    }

    @Test
    void shouldReturnBadRequestWhenReadHasNeitherFileNorSecret() throws Exception {
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "{\"name\":\"Cluster\",\"codename\":\"cluster\"}".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL + "/read")
                        .file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You need either to upload the kubeConfig file or name of secret object that holds the respective kubeConfig file in the local cluster"));
    }

    @Test
    void shouldReturnBadRequestWhenReadPayloadIsInvalid() throws Exception {
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                "not-a-json".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile("file", "kubeconfig", "text/plain",
                "kube-config-content".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart(CLUSTER_BASE_URL + "/read")
                        .file(data)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid cluster request payload"));
    }

    @Test
    void shouldUpdateClusterStatus() throws Exception {
        mvc.perform(post(CLUSTER_BASE_URL + "/1/status"))
                .andExpect(status().isOk());

        verify(remoteClusterManager, times(1)).updateClusterStatus(1L);
    }
}
