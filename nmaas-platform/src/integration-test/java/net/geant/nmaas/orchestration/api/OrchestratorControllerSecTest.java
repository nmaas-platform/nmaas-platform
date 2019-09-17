package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class OrchestratorControllerSecTest extends BaseControllerTestSetup {

    @MockBean
    private DefaultAppDeploymentRepositoryManager repository;

    @BeforeEach
    public void setup() {
        createMVC();
    }

    @Test
    public void shouldAuthorizeAdminProperUser() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        mvc.perform(get("/api/orchestration/deployments")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        when(repository.loadState(any())).thenThrow(new InvalidDeploymentIdException(""));
        mvc.perform(get("/api/orchestration/deployments/{deploymentId}/state", "id")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRejectNonAdminProperUser() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        mvc.perform(get("/api/orchestration/deployments")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/orchestration/deployments/{deploymentId}/state", "id")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
