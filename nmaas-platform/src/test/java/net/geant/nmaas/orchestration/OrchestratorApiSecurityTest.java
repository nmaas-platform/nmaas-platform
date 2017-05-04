package net.geant.nmaas.orchestration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrchestratorApiSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @MockBean
    private AppDeploymentRepositoryManager repository;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void shouldAuthorizeProperUser() throws Exception {
        mvc.perform(get("/platform/api/orchestration/deployments")
                .with(httpBasic(context.getEnvironment().getProperty("api.client.nmaas.test.username"), context.getEnvironment().getProperty("api.client.nmaas.test.password"))))
                .andExpect(status().isOk());
    }
}
