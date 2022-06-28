package net.geant.nmaas.externalservices.kubernetes;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class KubernetesClusterControllerSecTest extends BaseControllerTestSetup {

    @BeforeEach
    public void setup() {
        createMVC();
    }

    @Test
    public void shouldAuthorizeAdminProperUser() {
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/management/kubernetes")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        });
    }

    @Test
    public void shouldRejectNonAdminProperUser() {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/management/kubernetes")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        });
    }
}
