package net.geant.nmaas.orchestration.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.api.AppDeploymentMonitorRestController;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrchestratorMonitorRestControllerTest {

    @Mock
    AppDeploymentMonitor deploymentMonitor;

    private MockMvc mvc;

    private Identifier deploymentId;

    private Map<Identifier, AppLifecycleState> deployments;

    private AppUiAccessDetails accessDetails;

    @Before
    public void setup() {
        deploymentId = Identifier.newInstance("deploymentId1");
        deployments = new HashMap<>();
        deployments.put(Identifier.newInstance("deploymentId2"), AppLifecycleState.APPLICATION_CONFIGURED);
        deployments.put(deploymentId, AppLifecycleState.APPLICATION_CONFIGURATION_FAILED);
        deployments.put(Identifier.newInstance("deploymentId3"), AppLifecycleState.APPLICATION_DEPLOYED);
        accessDetails = new AppUiAccessDetails("http://testurl:8080");
        mvc = MockMvcBuilders.standaloneSetup(new AppDeploymentMonitorRestController(deploymentMonitor)).build();
    }

    @Test
    public void shouldRetrieveAllDeploymentsWithTheirStatus() throws Exception {
        when(deploymentMonitor.allDeployments()).thenReturn(deployments).thenReturn(deployments);
        MvcResult result = mvc.perform(get("/platform/api/orchestration/deployments"))
                .andExpect(status().isOk())
                .andReturn();
        Map<Identifier, AppLifecycleState> retrievedDeployments =
                new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<Map<Identifier,AppLifecycleState>>() {});
        assertThat(retrievedDeployments.size(), equalTo(deployments.size()));
        assertThat(retrievedDeployments.get(deploymentId), equalTo(deployments.get(deploymentId)));
    }

    @Test
    public void shouldRetrieveCurrentDeploymentLifecycleStatus() throws Exception {
        when(deploymentMonitor.state(deploymentId)).thenReturn(deployments.get(deploymentId));
        MvcResult result = mvc.perform(get("/platform/api/orchestration/deployments/{deploymentId}/state", deploymentId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(new ObjectMapper().readValue(result.getResponse().getContentAsString(), AppLifecycleState.class), equalTo(deployments.get(deploymentId)));
    }

    @Test
    public void shouldTryToRetrieveNotExistingDeployment() throws Exception {
        Identifier invalidDeploymentID = Identifier.newInstance("invalidValue");
        when(deploymentMonitor.state(invalidDeploymentID)).thenThrow(InvalidDeploymentIdException.class);
        mvc.perform(get("/platform/api/orchestration/deployments/{deploymentId}/state", invalidDeploymentID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRetrieveDeploymentAccessDetails() throws Exception {
        when(deploymentMonitor.userAccessDetails(deploymentId)).thenReturn(accessDetails);
        MvcResult result = mvc.perform(get("/platform/api/orchestration/deployments/{deploymentId}/access", deploymentId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        AppUiAccessDetails resultAccessDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), AppUiAccessDetails.class);
        assertThat(resultAccessDetails.getUrl(), equalTo(accessDetails.getUrl()));
    }

    @Test
    public void shouldTryToRetrieveDeploymentAccessDetailsInWrongState() throws Exception {
        when(deploymentMonitor.userAccessDetails(deploymentId)).thenThrow(new InvalidAppStateException(""));
        mvc.perform(get("/platform/api/orchestration/deployments/{deploymentId}/access", deploymentId.toString()))
                .andExpect(status().isConflict());
    }
}
