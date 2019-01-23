package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GitLabControllerSecTest extends BaseControllerTestSetup {

    @MockBean
    private GitLabRepository repository;

    @Before
    public void setup(){
        createMVC();
    }

    @Test
    public void shouldAuthorizeAdminProperUser() throws Exception{
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        mvc.perform(get("/api/management/gitlab")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldRejectNonAdminProperUser() throws Exception{
        String token = getValidUserTokenFor(Role.ROLE_DOMAIN_ADMIN);
        mvc.perform(get("/api/management/gitlab")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
