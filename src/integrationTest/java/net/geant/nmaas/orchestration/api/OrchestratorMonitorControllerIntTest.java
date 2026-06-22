package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.api.dto.applications.ServiceAccessMethodDto;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppDeploymentDto;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class OrchestratorMonitorControllerIntTest {

    private final AppDeploymentMonitor deploymentMonitor;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public OrchestratorMonitorControllerIntTest(@Autowired ModelMapper modelMapper,
                                                @Autowired ObjectMapper objectMapper) {
        this.deploymentMonitor = mock(AppDeploymentMonitor.class);
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    private MockMvc mvc;

    private Identifier deploymentId;
    private List<AppDeployment> deployments;
    private AppUiAccessDetails accessDetails;

    @BeforeEach
    void setup() {
        deploymentId = Identifier.newInstance("deploymentId1");

        AppDeployment deployment1 = AppDeployment.builder()
                .deploymentId(deploymentId)
                .domain("domain1")
                .applicationId(Identifier.newInstance("applicationId1"))
                .deploymentName("deploymentName1")
                .configFileRepositoryRequired(true)
                .build();

        AppDeployment deployment2 = AppDeployment.builder()
                .deploymentId(Identifier.newInstance("deploymentId2"))
                .domain("domain2")
                .applicationId(Identifier.newInstance("applicationId2"))
                .deploymentName("deploymentName2")
                .configFileRepositoryRequired(true)
                .build();
        deployment2.setState(AppDeploymentState.APPLICATION_DEPLOYED);

        AppDeployment deployment3 = AppDeployment.builder()
                .deploymentId(Identifier.newInstance("deploymentId3"))
                .domain("domain3")
                .applicationId(Identifier.newInstance("applicationId3"))
                .deploymentName("deploymentName3")
                .configFileRepositoryRequired(true)
                .build();
        deployment3.setState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);

        deployments = Arrays.asList(deployment1, deployment2, deployment3);
        accessDetails = new AppUiAccessDetails(new HashSet<>() {{
            ServiceAccessMethodDto.builder().type(ServiceAccessMethodTypeDto.DEFAULT).name("Default").protocol("Web").url("http://testurl:8080").build();
        }});
        mvc = MockMvcBuilders.standaloneSetup(new AppDeploymentMonitorRestController(deploymentMonitor, modelMapper))
                .addPlaceholderValue("nmaas.api.version", "v1")
                .build();
    }

    @Test
    void shouldRetrieveAllDeployments() throws Exception {
        when(deploymentMonitor.allDeployments()).thenReturn(deployments).thenReturn(deployments);
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, AppDeploymentDto.class);
        MvcResult result = mvc.perform(get("/api/v1/orchestration/deployments"))
                .andExpect(status().isOk())
                .andReturn();
        List<AppDeploymentDto> retrievedDeployments = objectMapper.readValue(result.getResponse().getContentAsString(), type);
        assertThat(retrievedDeployments.size(), equalTo(deployments.size()));
        assertThat(
                retrievedDeployments.stream().map(AppDeploymentDto::getDeploymentId).collect(Collectors.toList()),
                contains("deploymentId1", "deploymentId2", "deploymentId3"));
    }

    @Test
    void shouldRetrieveCurrentDeploymentLifecycleStatus() throws Exception {
        when(deploymentMonitor.state(deploymentId)).thenReturn(AppLifecycleState.APPLICATION_CONFIGURED);
        MvcResult result = mvc.perform(get("/api/v1/orchestration/deployments/{deploymentId}/state", deploymentId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(
                new ObjectMapper().readValue(result.getResponse().getContentAsString(), AppLifecycleState.class),
                equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

    @Test
    void shouldTryToRetrieveNotExistingDeployment() {
        Identifier invalidDeploymentID = Identifier.newInstance("invalidValue");
        when(deploymentMonitor.state(invalidDeploymentID)).thenThrow(InvalidDeploymentIdException.class);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/v1/orchestration/deployments/{deploymentId}/state", invalidDeploymentID.toString()))
                    .andExpect(status().isNotFound());
        });
    }

    @Test
    void shouldRetrieveDeploymentAccessDetails() throws Exception {
        when(deploymentMonitor.userAccessDetails(deploymentId)).thenReturn(accessDetails);
        MvcResult result = mvc.perform(get("/api/v1/orchestration/deployments/{deploymentId}/access", deploymentId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        AppUiAccessDetails resultAccessDetails = new ObjectMapper().readValue(result.getResponse().getContentAsString(), AppUiAccessDetails.class);
        assertThat(resultAccessDetails.getServiceAccessMethods(), equalTo(accessDetails.getServiceAccessMethods()));
    }

    @Test
    void shouldTryToRetrieveDeploymentAccessDetailsInWrongState() {
        when(deploymentMonitor.userAccessDetails(deploymentId)).thenThrow(new InvalidAppStateException(""));
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/v1/orchestration/deployments/{deploymentId}/access", deploymentId.toString()))
                    .andExpect(status().isConflict());
        });
    }

    @Test
    void shouldMapAppDeploymentToAppDeploymentView() {
        AppDeployment source = AppDeployment.builder()
                .deploymentId(Identifier.newInstance("deploymentId"))
                .domain("domain1")
                .applicationId(Identifier.newInstance("2"))
                .deploymentName("deploymentName")
                .configFileRepositoryRequired(true)
                .build();

        AppDeploymentDto output = modelMapper.map(source, AppDeploymentDto.class);
        assertThat(output.getDeploymentId(), equalTo(source.getDeploymentId().value()));
        assertThat(output.getDomain(), equalTo(source.getDomain()));
        assertThat(output.getDeploymentName(), equalTo(source.getDeploymentName()));
        assertThat(output.getState(), equalTo(source.getState().name()));
    }

}
