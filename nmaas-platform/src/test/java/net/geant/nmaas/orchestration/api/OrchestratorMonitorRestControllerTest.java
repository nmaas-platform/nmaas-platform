package net.geant.nmaas.orchestration.api;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.api.model.AppDeploymentView;
import net.geant.nmaas.orchestration.entities.*;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
    private AppDeploymentMonitor deploymentMonitor;
    @Autowired
    private ModelMapper modelMapper;

    private MockMvc mvc;

    private Identifier deploymentId;
    private List<AppDeployment> deployments;
    private AppUiAccessDetails accessDetails;

    @Before
    public void setup() {
        deploymentId = Identifier.newInstance("deploymentId1");
        AppDeployment deployment1 = new AppDeployment(
                deploymentId,
                "domain1",
                Identifier.newInstance("applicationId1"),
                "deploymentName1");
        AppDeployment deployment2 = new AppDeployment(
                Identifier.newInstance("deploymentId2"),
                "domain2",
                Identifier.newInstance("applicationId2"),
                "deploymentName2");
        deployment2.setState(AppDeploymentState.APPLICATION_DEPLOYED);
        AppDeployment deployment3 = new AppDeployment(
                Identifier.newInstance("deploymentId3"),
                "domain3",
                Identifier.newInstance("applicationId3"),
                "deploymentName3");
        deployment3.setState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        deployments = Arrays.asList(deployment1, deployment2, deployment3);
        accessDetails = new AppUiAccessDetails("http://testurl:8080");
        mvc = MockMvcBuilders.standaloneSetup(new AppDeploymentMonitorRestController(deploymentMonitor, modelMapper)).build();
    }

    @Test
    public void shouldRetrieveAllDeployments() throws Exception {
        when(deploymentMonitor.allDeployments()).thenReturn(deployments).thenReturn(deployments);
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, AppDeploymentView.class);
        MvcResult result = mvc.perform(get("/api/orchestration/deployments"))
                .andExpect(status().isOk())
                .andReturn();
        List<AppDeploymentView> retrievedDeployments = mapper.readValue(result.getResponse().getContentAsString(), type);
        assertThat(retrievedDeployments.size(), equalTo(deployments.size()));
        assertThat(
                retrievedDeployments.stream().map(d -> d.getDeploymentId()).collect(Collectors.toList()),
                contains("deploymentId1", "deploymentId2", "deploymentId3"));
    }

    @Test
    public void shouldRetrieveCurrentDeploymentLifecycleStatus() throws Exception {
        when(deploymentMonitor.state(deploymentId)).thenReturn(AppLifecycleState.APPLICATION_CONFIGURED);
        MvcResult result = mvc.perform(get("/api/orchestration/deployments/{deploymentId}/state", deploymentId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(
                new ObjectMapper().readValue(result.getResponse().getContentAsString(), AppLifecycleState.class),
                equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

    @Test
    public void shouldTryToRetrieveNotExistingDeployment() throws Exception {
        Identifier invalidDeploymentID = Identifier.newInstance("invalidValue");
        when(deploymentMonitor.state(invalidDeploymentID)).thenThrow(InvalidDeploymentIdException.class);
        mvc.perform(get("/api/orchestration/deployments/{deploymentId}/state", invalidDeploymentID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRetrieveDeploymentAccessDetails() throws Exception {
        when(deploymentMonitor.userAccessDetails(deploymentId)).thenReturn(accessDetails);
        MvcResult result = mvc.perform(get("/api/orchestration/deployments/{deploymentId}/access", deploymentId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        AppUiAccessDetails resultAccessDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), AppUiAccessDetails.class);
        assertThat(resultAccessDetails.getUrl(), equalTo(accessDetails.getUrl()));
    }

    @Test
    public void shouldTryToRetrieveDeploymentAccessDetailsInWrongState() throws Exception {
        when(deploymentMonitor.userAccessDetails(deploymentId)).thenThrow(new InvalidAppStateException(""));
        mvc.perform(get("/api/orchestration/deployments/{deploymentId}/access", deploymentId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldMapAppDeploymentToAppDeploymentView() {
        AppDeployment source = new AppDeployment(
                Identifier.newInstance("deploymentId"),
                "domain1",
                Identifier.newInstance("2"),
                "deploymentName");
        AppDeploymentView output = modelMapper.map(source, AppDeploymentView.class);
        assertThat(output.getDeploymentId(), equalTo(source.getDeploymentId().value()));
        assertThat(output.getDomain(), equalTo(source.getDomain()));
        assertThat(output.getDeploymentName(), equalTo(source.getDeploymentName()));
        assertThat(output.getState(), equalTo(source.getState().name()));
    }
}
