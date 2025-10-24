package net.geant.nmaas.portal.api.logs;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
//@AutoConfigureMockMvc
public class ApplicationLogsControllerSecTest extends BaseControllerTestSetup {

    @MockitoBean
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

    @Test
    void shouldAccessAppInstancePodLogsEndpointAsAdmin() {
        when(applicationLogsService.isLogAccessEnabled(1L)).thenReturn(true);
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/apps/logs/1/pods/pod1/container/container1")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        });
        verify(applicationLogsService, times(1)).getPodLogs(1L, "pod1", "container1", 0);
    }

    @Test
    void shouldAccessAppInstancePodLogsEndpointAsAdminWithLimit() {
        when(applicationLogsService.isLogAccessEnabled(1L)).thenReturn(true);
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/apps/logs/1/pods/pod1/container/container1?limit=10")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        });
        verify(applicationLogsService, times(1)).getPodLogs(1L, "pod1", "container1", 10);
    }

}
