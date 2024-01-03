package net.geant.nmaas.portal.api.logs;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
//@AutoConfigureMockMvc
public class ApplicationLogsControllerSecTest extends BaseControllerTestSetup {

    @MockBean
    private ApplicationLogsService applicationLogsService;

    @BeforeEach
    void setup() {
        createMVC();
    }

    @Test
    void shouldAccessAppInstancePodNamesEndpointAsAdmin() {
        when(applicationLogsService.isLogAccessEnabled(1L)).thenReturn(true);
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/apps/logs/1/pods")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        });
    }

}
