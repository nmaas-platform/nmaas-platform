package net.geant.nmaas.dcn.deployment.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.DcnView;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DcnAdminControllerSecTest extends BaseControllerTestSetup {

    @MockBean
    private DcnRepositoryManager dcnRepositoryManager;

    @BeforeEach
    public void setup() {
        createMVC();
    }

    @Test
    public void shouldAuthAndCallSimpleGet() throws Exception {
        when(dcnRepositoryManager.loadAllNetworks())
                .thenReturn(Arrays.asList(dcnInfo()));
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        MvcResult result = mvc.perform(get("/api/management/dcns")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        List<DcnView> content = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<DcnView>>() {});
        assertThat(dcnRepositoryManager.loadAllNetworks().size(), equalTo(content.size()));
        assertThat(dcnInfo().getDomain(), equalTo(content.get(0).getDomain()));
    }

    private DcnInfo dcnInfo() {
        DcnInfo dcnInfo = new DcnInfo();
        dcnInfo.setName("");
        dcnInfo.setDomain("domain");
        return dcnInfo;
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        mvc.perform(get("/api/management/dcns")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

}
