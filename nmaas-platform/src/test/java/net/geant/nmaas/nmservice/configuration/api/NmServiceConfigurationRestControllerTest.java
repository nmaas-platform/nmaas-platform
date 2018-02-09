package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class NmServiceConfigurationRestControllerTest {

    private static final String TEST_OXIDIZED_CONFIG_FIRST_ID = "oxidized-config-1";
    private static final String TEST_OXIDIZED_CONFIG_FIRST_FILENAME = "config-file-name-1";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private NmServiceConfigFileRepository configurations;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void shouldReturnSimpleFile() throws Exception {
        String configFileContent = "simple content";
        NmServiceConfiguration configuration
                = new NmServiceConfiguration(TEST_OXIDIZED_CONFIG_FIRST_ID, TEST_OXIDIZED_CONFIG_FIRST_FILENAME, configFileContent);
        configurations.save(configuration);
        mvc.perform(get("/platform/api/configs/{configId}", TEST_OXIDIZED_CONFIG_FIRST_ID)
                .with(httpBasic(context.getEnvironment().getProperty("app.config.download.client.username"), context.getEnvironment().getProperty("app.config.download.client.password"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment;filename=" + TEST_OXIDIZED_CONFIG_FIRST_FILENAME))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"))
                .andExpect(content().string(configFileContent));
    }

    @Test
    public void shouldForbidDownload() throws Exception {
        mvc.perform(get("/platform/api/configs/{configId}", TEST_OXIDIZED_CONFIG_FIRST_ID)
                .with(httpBasic("testClient", "testPassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNotFoundOnMissingConfigurationWithProvidedId() throws Exception {
        mvc.perform(get("/platform/api/configs/{configId}", TEST_OXIDIZED_CONFIG_FIRST_ID + "invalid-string")
                .with(httpBasic(context.getEnvironment().getProperty("app.config.download.client.username"), context.getEnvironment().getProperty("app.config.download.client.password"))))
                .andExpect(status().isNotFound());
    }

}
