package net.geant.nmaas.nmservice.deployment.api;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NmServiceDeploymentAdminControllerSecTest extends BaseControllerTestSetup {

    @BeforeEach
    public void setup() {
        createMVC();
    }

    @Test
    void shouldAuthAndCallSimpleGet() {
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/management/services")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        });
    }

}
