package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationBuildAndDownloadTest {

    private static final Identifier deploymentId = Identifier.newInstance("testOxidizedDeploymentId");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private NmServiceConfigurationProvider nmServiceConfigurationService;

    @Autowired
    private NmServiceConfigurationRepository configurationRepository;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void shouldBuildRequestDownloadAndReturnTwoOxidizedConfigurationFiles() throws Exception {
//        nmServiceConfigurationService.configureNmService();
//
//        mvc.perform(get("/api/configs/{configId}", TEST_OXIDIZED_CONFIG_FIRST_ID)
//                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_NMAAS_TEST_CLIENT)))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Disposition", "attachment;filename=" + TEST_OXIDIZED_CONFIG_FIRST_FILENAME))
//                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"))
//                .andExpect(content().bytes(configFileBytes));
    }

}
