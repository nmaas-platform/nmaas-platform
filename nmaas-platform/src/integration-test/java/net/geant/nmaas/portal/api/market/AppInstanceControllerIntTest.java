package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.AppInstanceRequest;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AppInstanceControllerIntTest extends BaseControllerTestSetup {

    @SpyBean
    private DomainService domainService;

    @MockBean
    private ApplicationService applicationService;

    @SpyBean
    private ModelMapper modelMapper;

    @MockBean
    private ApplicationInstanceService applicationInstanceService;

    @MockBean
    private AppLifecycleManager appLifecycleManager;

    @BeforeEach
    void setup(){
        this.mvc = this.createMVC();
    }

    @Test
    void shouldDeployApplicationInstance() throws Exception {
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        Application application = new Application("name", "version", "owner");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        AppInstanceRequest appInstanceRequest = appInstanceRequest();
        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));
        when(applicationInstanceService.create(domain, application, appInstanceRequest.getName()))
                .thenReturn(new AppInstance(10L, application, domain, appInstanceRequest.getName()));
        when(modelMapper.map(application.getAppDeploymentSpec(), AppDeploymentSpec.class)).thenReturn(new AppDeploymentSpec());
        mvc.perform(post("/api/apps/instances/domain/{domainId}", domain.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(appInstanceRequest))
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        ArgumentCaptor<AppDeployment> appDeployment = ArgumentCaptor.forClass(AppDeployment.class);
        verify(appLifecycleManager, times(1)).deployApplication(appDeployment.capture());
        assertThat(appDeployment.getValue().getInstanceId(), equalTo(10L));
    }

    private AppInstanceRequest appInstanceRequest() {
        AppInstanceRequest appInstanceRequest = new AppInstanceRequest();
        appInstanceRequest.setApplicationId(1L);
        appInstanceRequest.setName("appInstanceName");
        return appInstanceRequest;
    }

    @Test
    void shouldRestartApplication() throws Exception{
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        AppInstance appInstance = new AppInstance(new Application("test","testVersion","admin"),"test", domain, user);
        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        mvc.perform(post("/api/apps/instances/{appInstanceId}/restart", 1L)
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).restartApplication(appInstance.getInternalId());
    }

    @Test
    void shouldThrowAnExceptionWhenInputIsIncorrect() throws Exception{
        when(applicationInstanceService.find(0L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/apps/instances/{appInstanceId}/restart",0L)
                .header("Authorization","Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                .andExpect(status().is(404));
    }
}
