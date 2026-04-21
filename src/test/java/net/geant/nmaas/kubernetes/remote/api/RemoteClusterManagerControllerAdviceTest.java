package net.geant.nmaas.kubernetes.remote.api;

import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RemoteClusterManagerControllerAdviceTest {

    private final RemoteClusterManagementService remoteClusterManager = mock(RemoteClusterManagementService.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        RemoteClusterManagerController controller = new RemoteClusterManagerController(remoteClusterManager, objectMapper);
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new RemoveClusterManagerAdvice())
                .build();
    }

    @Test
    void shouldReturnBadRequestWhenServiceThrowsIllegalArgumentException() throws Exception {
        when(remoteClusterManager.getCluster(eq(1L), nullable(Principal.class)))
                .thenThrow(new IllegalArgumentException("No access to cluster 1"));

        mvc.perform(get("/api/management/cluster/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No access to cluster 1"));
    }

    @Test
    void shouldReturnNotFoundWhenServiceThrowsNoSuchElementException() throws Exception {
        when(remoteClusterManager.getCluster(eq(7L), nullable(Principal.class)))
                .thenThrow(new NoSuchElementException("Cluster not found"));

        mvc.perform(get("/api/management/cluster/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cluster not found"));
    }
}
