package net.geant.nmaas.nmservice.deployment.api;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NmServiceDeploymentAdminControllerSecTest extends BaseControllerTestSetup {

    @Before
    public void setup() {
        createMVC();
    }

    @Test
    public void shouldAuthAndCallSimpleGet() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        mvc.perform(get("/api/management/services")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        mvc.perform(get("/api/management/services")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

}
