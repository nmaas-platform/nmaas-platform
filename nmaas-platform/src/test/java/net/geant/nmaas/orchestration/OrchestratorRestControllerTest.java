package net.geant.nmaas.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.configuration.SecurityConfig;
import net.geant.nmaas.orchestration.api.AppLifecycleManagerRestController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrchestratorRestControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Mock
    private AppLifecycleManager lifecycleManager;

    private MockMvc mvc;

    private Identifier clientId;

    private Identifier applicationId;

    private Identifier deploymentId;

    private AppConfiguration appConfiguration;

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
    public void shouldRequestNewDeployment() throws Exception {
        when(lifecycleManager.deployApplication(any(),any())).thenReturn(deploymentId);
        ObjectMapper mapper = new ObjectMapper();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("clientid", clientId.toString());
        params.set("applicationid", applicationId.toString());
        mvc.perform(post("/platform/api/orchestration/deployments")
                .params(params)
                .with(user("test").roles(SecurityConfig.AUTH_ROLE_NMAAS_TEST_CLIENT))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(deploymentId)));
    }

    @Test
    public void shouldApplyConfigurationForDeployment() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mvc.perform(post("/platform/api/orchestration/deployments/{deploymentId}", deploymentId.toString())
                .with(user("test").roles(SecurityConfig.AUTH_ROLE_NMAAS_TEST_CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(appConfiguration))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Identifier> deploymentIdCaptor = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<AppConfiguration> appConfigurationCaptor = ArgumentCaptor.forClass(AppConfiguration.class);

        verify(lifecycleManager, times(1)).applyConfiguration(deploymentIdCaptor.capture(), appConfigurationCaptor.capture());
        assertThat(deploymentIdCaptor.getValue(), equalTo(deploymentId));
        assertThat(appConfigurationCaptor.getValue(), equalTo(appConfiguration));
    }

}
