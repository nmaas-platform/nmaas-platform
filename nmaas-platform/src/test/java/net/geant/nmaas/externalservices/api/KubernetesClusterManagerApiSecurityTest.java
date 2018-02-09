package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-k8s.properties")
public class KubernetesClusterManagerApiSecurityTest extends BaseControllerTest {

    @Before
    public void setup() {
        mvc = createMVC();
    }

    @MockBean
    private KubernetesClusterRepository repository;

    @Test
    public void shouldAuthorizeAdminProperUser() throws Exception {
        String token = getValidUserTokenFor(Role.ADMIN);
        mvc.perform(get("/platform/api/management/kubernetes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldRejectNonAdminProperUser() throws Exception {
        String token = getValidUserTokenFor(Role.USER);
        mvc.perform(get("/platform/api/management/kubernetes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
