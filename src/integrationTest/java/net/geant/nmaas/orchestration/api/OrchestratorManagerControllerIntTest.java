package net.geant.nmaas.orchestration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.security.Principal;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class OrchestratorManagerControllerIntTest {

    @MockBean
    private AppLifecycleManager lifecycleManager;

    @MockBean
    private ApplicationRepository appRepo;

    private MockMvc mvc;

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private Identifier applicationId;
    private Identifier deploymentId;
    private AppConfiguration appConfiguration;

    private Principal principal;

    private static final String CONFIGURATION_JSON = "" +
            "{" +
            "\"jsonInput\":{\"id\":\"testvalue" + "\"}," +
            "\"storageSpace\":null" +
            "}";

    @BeforeEach
    public void setup() {
        applicationId = Identifier.newInstance(15L);
        deploymentId = Identifier.newInstance("deploymentId1");
        String jsonInput = "{\"id\":\"testvalue\"}";
        appConfiguration = new AppConfiguration(jsonInput);
        mvc = MockMvcBuilders.standaloneSetup(new AppLifecycleManagerRestController(lifecycleManager, appRepo)).build();

        Application application = new Application("testapp", "testversion");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.getAppConfigurationSpec().setConfigFileRepositoryRequired(true);
        when(appRepo.findById(any())).thenReturn(Optional.of(application));

        this.principal = mock(Principal.class);
        when(this.principal.getName()).thenReturn("user");

    }

    @Test
    public void shouldRequestNewDeploymentAndReceiveNewDeploymentId() {
        when(lifecycleManager.deployApplication(any())).thenReturn(deploymentId);
        ObjectMapper mapper = new ObjectMapper();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("domain", DOMAIN);
        params.set("applicationid", applicationId.getValue());
        params.set("deploymentname", DEPLOYMENT_NAME);
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/orchestration/deployments")
                    .params(params)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(deploymentId)));
        });
    }

    @Test
    public void shouldApplyConfigurationForDeploymentWithGivenDeploymentId() throws Throwable {
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}", deploymentId.toString())
                .principal(this.principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONFIGURATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfigurationView> appConfigurationCaptor = ArgumentCaptor.forClass(AppConfigurationView.class);

        verify(lifecycleManager, times(1)).applyConfiguration(deploymentIdCaptor.capture(), appConfigurationCaptor.capture(), eq("user"));
        assertThat(deploymentIdCaptor.getValue(), equalTo(deploymentId));
        assertThat(appConfigurationCaptor.getValue().getJsonInput(), equalTo(appConfiguration.getJsonInput()));
    }

    @Test
    public void shouldUpdateConfigurationForDeploymentWithGivenDeploymentId() throws Throwable {
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}", deploymentId.toString())
                .principal(this.principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONFIGURATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}/update", deploymentId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jsonInput\":{\"id\":\"newtestvalue" + "\"}," + "\"storageSpace\":null" + "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfigurationView> appDeploymentCaptor = ArgumentCaptor.forClass(AppConfigurationView.class);
        verify(lifecycleManager, times(1)).updateConfiguration(deploymentIdCaptor.capture(), appDeploymentCaptor.capture());
        assertEquals(deploymentId, deploymentIdCaptor.getValue());
        assertTrue(appDeploymentCaptor.getValue().getJsonInput().contains("newtestvalue"));
    }

    @Test
    public void shouldReturnNotFoundOnMissingDeploymentWithGivenDeploymentId() throws Throwable {
        doThrow(InvalidDeploymentIdException.class).when(lifecycleManager).applyConfiguration(any(), any(), anyString());
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/orchestration/deployments/{deploymentId}", "anydeploymentid")
                    .principal(this.principal)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CONFIGURATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        });
    }

}
