package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.NmaasPlatformConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationRestControllerTest {

    private static final String TEST_OXIDIZED_CONFIG_FIRST_ID = "oxidized-config-1";
    private static final String TEST_OXIDIZED_CONFIG_FIRST_FILENAME = "config-file-name-1";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

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
    public void shouldReturnSimpleFile() throws Exception {
        byte[] configFileBytes = new byte[]{1,2,3,4,5};
        NmServiceConfiguration configuration = new NmServiceConfiguration(TEST_OXIDIZED_CONFIG_FIRST_ID, TEST_OXIDIZED_CONFIG_FIRST_FILENAME, configFileBytes);
        configurationRepository.storeConfig(TEST_OXIDIZED_CONFIG_FIRST_ID, configuration);
        mvc.perform(get("/api/configs/{configId}", TEST_OXIDIZED_CONFIG_FIRST_ID)
                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment;filename=" + TEST_OXIDIZED_CONFIG_FIRST_FILENAME))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"))
                .andExpect(content().bytes(configFileBytes));
    }

    @Test
    public void shouldReturnNotFoundOnMissingConfigurationWithProvidedId() throws Exception {
        mvc.perform(get("/api/configs/{configId}", TEST_OXIDIZED_CONFIG_FIRST_ID + "invalid-string")
                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_CONFIG_DOWNLOAD_CLIENT)))
                .andExpect(status().isNotFound());
    }

}
