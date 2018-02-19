package net.geant.nmaas.dcn.deployment.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.DcnView;
import net.geant.nmaas.dcn.deployment.entities.DcnCloudEndpointDetails;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DcnAdminApiSecurityTest extends BaseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @MockBean
    private DcnRepositoryManager dcnRepositoryManager;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void shouldAuthAndCallSimpleGet() throws Exception {
        when(dcnRepositoryManager.loadAllNetworks())
                .thenReturn(Arrays.asList(dcnInfo()));
        String token = getValidUserTokenFor(Role.ROLE_SUPERADMIN);
        MvcResult result = mvc.perform(get("/platform/api/management/dcns")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        List<DcnView> content = ((List<DcnView>) new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<DcnView>>() {}));
        assertThat(dcnRepositoryManager.loadAllNetworks().size(), equalTo(content.size()));
        assertThat(dcnInfo().getClientId().getValue(), equalTo(content.get(0).getClientId()));
    }

    private DcnInfo dcnInfo() {
        DcnInfo dcnInfo = new DcnInfo();
        dcnInfo.setName("");
        dcnInfo.setClientId(Identifier.newInstance("1L"));
        DcnCloudEndpointDetails dcnCloudEndpointDetails =
                new DcnCloudEndpointDetails(550, "10.10.10.0/24", "10.10.10.1");
        dcnInfo.setCloudEndpointDetails(dcnCloudEndpointDetails);
        return dcnInfo;
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        mvc.perform(get("/platform/api/management/dcns")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

}
