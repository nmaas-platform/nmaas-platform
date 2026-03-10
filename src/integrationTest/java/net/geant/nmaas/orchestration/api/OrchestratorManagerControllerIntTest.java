package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class OrchestratorManagerControllerIntTest {

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId1");
    private static final Identifier APPLICATION_ID = Identifier.newInstance(15L);

    @Autowired
    private JsonMapper jsonMapper;

    private final AppLifecycleManager lifecycleManager = mock(AppLifecycleManager.class);
    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    private final Principal principal = mock(Principal.class);

    private MockMvc mvc;

    private AppConfiguration appConfiguration;

    private static final String CONFIGURATION_JSON = "{" +
            "\"jsonInput\":{\"id\":\"testvalue\"}," +
            "\"storageSpace\":null" +
            "}";

    @BeforeEach
    void setup() {
        String jsonInput = "{\"id\":\"testvalue\"}";
        appConfiguration = new AppConfiguration(jsonInput);
        mvc = MockMvcBuilders.standaloneSetup(new AppLifecycleManagerRestController(lifecycleManager, applicationRepository)).build();

        Application application = new Application("testapp", "testversion");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.getAppConfigurationSpec().setConfigFileRepositoryRequired(true);
        when(applicationRepository.findById(any())).thenReturn(Optional.of(application));
        when(principal.getName()).thenReturn("user");
    }

    @Test
    void shouldDeserializeAppConfigurationJson() {
        AppConfigurationView result = jsonMapper.readValue(CONFIGURATION_JSON, AppConfigurationView.class);
        assertEquals("{\"id\":\"testvalue\"}", jsonMapper.writeValueAsString(result.getJsonInput()));
    }

    @Test
    void shouldRequestNewDeploymentAndReceiveNewDeploymentId() {
        when(lifecycleManager.deployApplication(any())).thenReturn(DEPLOYMENT_ID);
        ObjectMapper mapper = new ObjectMapper();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("domain", DOMAIN);
        params.set("applicationid", APPLICATION_ID.getValue());
        params.set("deploymentname", DEPLOYMENT_NAME);
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/orchestration/deployments")
                            .params(params)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(mapper.writeValueAsString(DEPLOYMENT_ID)));
        });
    }

    @Test
    void shouldApplyConfigurationForDeploymentWithGivenDeploymentId() throws Exception {
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}", DEPLOYMENT_ID.toString())
                        .principal(this.principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONFIGURATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfigurationView> appConfigurationCaptor = ArgumentCaptor.forClass(AppConfigurationView.class);

        verify(lifecycleManager, times(1)).applyConfiguration(deploymentIdCaptor.capture(), appConfigurationCaptor.capture(), eq("user"));
        assertThat(deploymentIdCaptor.getValue(), equalTo(DEPLOYMENT_ID));
        assertThat(jsonMapper.writeValueAsString(appConfigurationCaptor.getValue().getJsonInput()), equalTo(appConfiguration.getJsonInput()));
    }

    @Test
    void shouldUpdateConfigurationForDeploymentWithGivenDeploymentId() throws Exception {
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}", DEPLOYMENT_ID.toString())
                        .principal(this.principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONFIGURATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mvc.perform(post("/api/orchestration/deployments/{deploymentId}/update", DEPLOYMENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonInput\":{\"id\":\"newtestvalue\"}," + "\"storageSpace\":null" + "}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfigurationView> appDeploymentCaptor = ArgumentCaptor.forClass(AppConfigurationView.class);
        verify(lifecycleManager, times(1)).updateConfiguration(deploymentIdCaptor.capture(), appDeploymentCaptor.capture());
        assertEquals(DEPLOYMENT_ID, deploymentIdCaptor.getValue());
        assertTrue(jsonMapper.writeValueAsString(appDeploymentCaptor.getValue().getJsonInput()).contains("newtestvalue"));
    }

    @Test
    void shouldReturnNotFoundOnMissingDeploymentWithGivenDeploymentId() {
        doThrow(InvalidDeploymentIdException.class)
                .when(lifecycleManager).applyConfiguration(any(), any(), anyString());
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
