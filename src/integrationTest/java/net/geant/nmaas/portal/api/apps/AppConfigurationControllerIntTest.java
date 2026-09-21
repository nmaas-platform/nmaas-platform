package net.geant.nmaas.portal.api.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.applications.AppConfigurationDto;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.service.AclService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AppConfigurationControllerIntTest extends BaseControllerTestSetup {

    private static final Domain DOMAIN = UsersHelper.DOMAIN1;

    private static final Long APP_INSTANCE_ID = 10L;

    private static final Identifier INTERNAL_DEPLOYMENT_ID = Identifier.newInstance("internalDeploymentId");

    @MockitoBean
    private ApplicationInstanceService applicationInstanceService;

    @MockitoBean
    private AppLifecycleManager appLifecycleManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AclService aclService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = createMVC();
        when(userService.findByUsername(UsersHelper.DOMAIN1_ADMIN.getUsername()))
                .thenReturn(Optional.of(UsersHelper.DOMAIN1_ADMIN));
        when(aclService.isAuthorized(eq(UsersHelper.DOMAIN1_ADMIN.getId()), eq(APP_INSTANCE_ID), eq("appInstance"), any()))
                .thenReturn(true);
    }

    @Test
    void shouldApplyConfigurationForAppInstance() throws Exception {
        AppInstance appInstance = appInstance();
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.validateAgainstAppConfiguration(eq(appInstance), any(AppConfigurationDto.class)))
                .thenReturn(true);

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/configure", APP_INSTANCE_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson()))
                .andExpect(status().isOk());

        ArgumentCaptor<AppConfigurationDto> configurationCaptor = ArgumentCaptor.forClass(AppConfigurationDto.class);
        verify(appLifecycleManager, times(1)).applyConfiguration(
                eq(INTERNAL_DEPLOYMENT_ID), configurationCaptor.capture(), eq(UsersHelper.DOMAIN1_ADMIN.getUsername()));
        assertThat(configurationCaptor.getValue().getAdditionalParameters().get("keyadd1").asString(), equalTo("valadd1"));
        verify(applicationInstanceService, times(1)).update(appInstance);
    }

    @Test
    void shouldRejectConfigurationUpdateWithModifiedConfigurationFileContent() throws Exception {
        AppInstance appInstance = appInstance();
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.validateAgainstAppConfiguration(eq(appInstance), any(AppConfigurationDto.class)))
                .thenReturn(true);

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/configure/update", APP_INSTANCE_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson()))
                .andExpect(status().isNotAcceptable());

        verify(appLifecycleManager, times(0)).updateConfiguration(any(), any(), any());
    }

    @Test
    void shouldUpdateConfigurationForAppInstance() throws Exception {
        AppInstance appInstance = appInstance();
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.validateAgainstAppConfiguration(eq(appInstance), any(AppConfigurationDto.class)))
                .thenReturn(true);

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/configure/update", APP_INSTANCE_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateConfigurationJson()))
                .andExpect(status().isOk());

        ArgumentCaptor<AppConfigurationDto> configurationCaptor = ArgumentCaptor.forClass(AppConfigurationDto.class);
        verify(appLifecycleManager, times(1)).updateConfiguration(
                eq(INTERNAL_DEPLOYMENT_ID), configurationCaptor.capture(), eq(UsersHelper.DOMAIN1_ADMIN.getUsername()));
        assertThat(configurationCaptor.getValue().getAdditionalParameters().get("keyadd1").asString(), equalTo("valupdated"));
    }

    @Test
    void shouldRejectConfigurationUpdateWhenValidationFails() throws Exception {
        AppInstance appInstance = appInstance();
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.validateAgainstAppConfiguration(eq(appInstance), any(AppConfigurationDto.class)))
                .thenReturn(false);

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/configure/update", APP_INSTANCE_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateConfigurationJson()))
                .andExpect(status().isNotAcceptable());

        verify(appLifecycleManager, times(0)).updateConfiguration(any(), any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenAppInstanceMissing() throws Exception {
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.empty());

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/configure", APP_INSTANCE_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConfigurationForAppInstance() throws Exception {
        AppInstance appInstance = appInstance();
        appInstance.setConfiguration("{\"id\":\"testvalue\"}");
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));

        mvc.perform(get("/api/v1/apps/instances/{appInstanceId}/configuration", APP_INSTANCE_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"testvalue\"}"));
    }

    private AppInstance appInstance() {
        Application application = new Application("testapp", "testversion");
        AppInstance appInstance = new AppInstance(APP_INSTANCE_ID, application, DOMAIN, "instance-name", true);
        appInstance.setInternalId(INTERNAL_DEPLOYMENT_ID);
        return appInstance;
    }

    private String configurationJson() {
        return "{" +
                "\"jsonInput\":{\"id\":\"testvalue\"}," +
                "\"storageSpace\":null," +
                "\"additionalParameters\":{\"keyadd1\":\"valadd1\"}," +
                "\"mandatoryParameters\":{}" +
                "}";
    }

    private String updateConfigurationJson() {
        return "{" +
                "\"jsonInput\":null," +
                "\"storageSpace\":null," +
                "\"additionalParameters\":{\"keyadd1\":\"valupdated\"}," +
                "\"mandatoryParameters\":{}" +
                "}";
    }

}
