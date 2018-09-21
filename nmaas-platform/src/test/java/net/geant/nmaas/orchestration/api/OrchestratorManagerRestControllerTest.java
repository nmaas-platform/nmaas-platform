package net.geant.nmaas.orchestration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrchestratorManagerRestControllerTest {

    @Mock
    private AppLifecycleManager lifecycleManager;

    private ApplicationRepository appRepo = mock(ApplicationRepository.class);

    private MockMvc mvc;

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private Identifier applicationId;
    private Identifier deploymentId;
    private AppConfiguration appConfiguration;
    private ModelMapper modelMapper;

    private static final String CONFIGURATION_JSON = "" +
            "{" +
            "\"jsonInput\":{\"id\":\"testvalue" + "\"}," +
            "\"storageSpace\":null" +
            "}";

    @Before
    public void setup() {
        modelMapper = new ModelMapper();
        applicationId = Identifier.newInstance(15L);
        deploymentId = Identifier.newInstance("deploymentId1");
        String jsonInput = "{\"id\":\"testvalue\"}";
        appConfiguration = new AppConfiguration(jsonInput);
        mvc = MockMvcBuilders.standaloneSetup(new AppLifecycleManagerRestController(lifecycleManager, appRepo, modelMapper)).build();
        Application application = new Application("testapp");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.getAppDeploymentSpec().setDefaultStorageSpace(20);
        application.getAppDeploymentSpec().setConfigFileRepositoryRequired(true);
        when(appRepo.findById(any())).thenReturn(Optional.of(application));
    }

    @Test
    public void shouldRequestNewDeploymentAndReceiveNewDeploymentId() throws Exception {
        when(lifecycleManager.deployApplication(any(), any(), any(), any())).thenReturn(deploymentId);
        ObjectMapper mapper = new ObjectMapper();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("domain", DOMAIN);
        params.set("applicationid", applicationId.getValue());
        params.set("deploymentname", DEPLOYMENT_NAME);
        mvc.perform(post("/api/orchestration/deployments")
                .params(params)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(deploymentId)));
    }

    @Test
    public void shouldApplyConfigurationForDeploymentWithGivenDeploymentId() throws Exception {
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}", deploymentId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONFIGURATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfigurationView> appConfigurationCaptor = ArgumentCaptor.forClass(AppConfigurationView.class);

        verify(lifecycleManager, times(1)).applyConfiguration(deploymentIdCaptor.capture(), appConfigurationCaptor.capture());
        assertThat(deploymentIdCaptor.getValue(), equalTo(deploymentId));
        assertThat(appConfigurationCaptor.getValue().getJsonInput(), equalTo(appConfiguration.getJsonInput()));
    }

    @Test
    public void shouldReturnNotFoundOnMissingDeploymentWithGivenDeploymentId() throws Exception {
        doThrow(InvalidDeploymentIdException.class).when(lifecycleManager).applyConfiguration(any(),any());
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}", "anydeploymentid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONFIGURATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
