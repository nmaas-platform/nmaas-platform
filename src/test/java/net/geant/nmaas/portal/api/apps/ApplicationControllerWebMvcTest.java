package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies role-based access to {@link ApplicationController#getApplicationByNameAndVersion}
 * and its retrieval behavior.
 *
 * The WebMvcTest slice does not load the application's SecurityConfig, so no servlet
 * filter chain is present and requests run directly against the dispatcher servlet.
 * Method security is therefore enabled explicitly on the test configuration, and
 * @WithMockUser is used (rather than the request post-processor) so the
 * authentication is visible to the method interceptors.
 */
@WebMvcTest(controllers = ApplicationController.class)
@Import(ApplicationControllerWebMvcTest.SecurityTestConfig.class)
class ApplicationControllerWebMvcTest {

    private static final String URL = "/api/v1/apps/my-app/version/1.0.0";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ModelMapper modelMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private ApplicationBaseService applicationBaseService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private ApplicationInstanceService applicationInstanceService;

    @Test
    @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
    void shouldAllowSystemAdminToGetApplicationByNameAndVersion() throws Exception {
        mockApplicationFound();

        mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationBase.name").value("my-app"));

        verify(applicationService).findApplication("my-app", "1.0.0");
    }

    @Test
    @WithMockUser(username = "toolmanager", roles = {"TOOL_MANAGER"})
    void shouldAllowToolManagerToGetApplicationByNameAndVersion() throws Exception {
        mockApplicationFound();

        mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(applicationService).findApplication("my-app", "1.0.0");
    }

    @Test
    @WithMockUser(username = "groupmanager", roles = {"GROUP_MANAGER"})
    void shouldAllowGroupManagerToGetApplicationByNameAndVersion() throws Exception {
        mockApplicationFound();

        mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationBase.name").value("my-app"));

        verify(applicationService).findApplication("my-app", "1.0.0");
    }

    @Test
    @WithMockUser(username = "regular", roles = {"USER"})
    void shouldRejectRegularUserFromGettingApplicationByNameAndVersion() throws Exception {
        mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(applicationService);
    }

    @Test
    @WithAnonymousUser
    void shouldRejectAnonymousFromGettingApplicationByNameAndVersion() throws Exception {
        mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(applicationService);
    }

    @Test
    @WithMockUser(username = "groupmanager", roles = {"GROUP_MANAGER"})
    void shouldReturnNotFoundWhenApplicationVersionDoesNotExist() throws Exception {
        when(applicationBaseService.findByName("my-app")).thenReturn(new ApplicationBase(10L, "my-app"));
        when(applicationService.findApplication("my-app", "9.9.9")).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/apps/my-app/version/9.9.9").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(applicationService).findApplication("my-app", "9.9.9");
    }

    private void mockApplicationFound() {
        when(applicationBaseService.findByName("my-app")).thenReturn(new ApplicationBase(10L, "my-app"));
        when(applicationService.findApplication("my-app", "1.0.0"))
                .thenReturn(Optional.of(new Application(10L, "my-app", "1.0.0")));
        when(modelMapper.map(any(ApplicationBase.class), any()))
                .thenReturn(net.geant.nmaas.api.dto.applications.ApplicationBaseDto.builder()
                        .id(10L).name("my-app").build());
        when(modelMapper.map(any(Application.class), any()))
                .thenReturn(new net.geant.nmaas.api.dto.applications.ApplicationDto());
    }

    /**
     * The WebMvcTest slice does not load the application's SecurityConfig, where
     * method security is enabled, so @EnableMethodSecurity has to be applied here
     * for @PreAuthorize to be enforced.
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

    }

}
