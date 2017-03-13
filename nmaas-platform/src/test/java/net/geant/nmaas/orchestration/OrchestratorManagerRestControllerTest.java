package net.geant.nmaas.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.api.AppLifecycleManagerRestController;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrchestratorManagerRestControllerTest {

    @Mock
    private AppLifecycleManager lifecycleManager;

    private MockMvc mvc;

    private Identifier clientId;

    private Identifier applicationId;

    private Identifier deploymentId;

    private AppConfiguration appConfiguration;

    @Autowired
    private Environment env;

    @Before
    public void setup() {
        clientId = Identifier.newInstance("clientId1");
        applicationId = Identifier.newInstance("applicationId1");
        deploymentId = Identifier.newInstance("deploymentId1");
        String jsonInput = "{\"id\":\"testvalue\"}";
        appConfiguration = new AppConfiguration(applicationId, jsonInput);
        mvc = MockMvcBuilders.standaloneSetup(new AppLifecycleManagerRestController(lifecycleManager)).build();
    }

    @Test
    public void shouldRequestNewDeploymentAndReceiveNewDeploymentId() throws Exception {
        when(lifecycleManager.deployApplication(any(),any())).thenReturn(deploymentId);
        ObjectMapper mapper = new ObjectMapper();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("clientid", clientId.toString());
        params.set("applicationid", applicationId.toString());
        mvc.perform(post("/platform/api/orchestration/deployments")
                .params(params)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(deploymentId)));
    }

    @Test
    public void shouldApplyConfigurationForDeploymentWithGivenDeploymentId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mvc.perform(post("/platform/api/orchestration/deployments/{deploymentId}", deploymentId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(appConfiguration.getJsonInput())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfiguration> appConfigurationCaptor = ArgumentCaptor.forClass(AppConfiguration.class);

        verify(lifecycleManager, times(1)).applyConfiguration(deploymentIdCaptor.capture(), appConfigurationCaptor.capture());
        assertThat(deploymentIdCaptor.getValue(), equalTo(deploymentId));
        assertThat(appConfigurationCaptor.getValue().getJsonInput(), equalTo(appConfiguration.getJsonInput()));
    }

    @Test
    public void shouldReturnNotFoundOnMissingDeploymentWithGivenDeploymentId() throws Exception {
        doThrow(InvalidDeploymentIdException.class).when(lifecycleManager).applyConfiguration(any(),any());
        mvc.perform(post("/platform/api/orchestration/deployments/{deploymentId}", "anydeploymentid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appConfiguration.getJsonInput())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
