package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.AppInstanceRequest;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    private ApplicationBaseRepository applicationBaseRepository;

    @MockBean
    private AppDeploymentMonitor appDeploymentMonitor;

    @BeforeEach
    void setup(){
        this.mvc = this.createMVC();
    }

    @Test
    void shouldDeployApplicationInstance() throws Exception {
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        Application application = new Application("name with spaces", "version", "owner");
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
        assertThat(appDeployment.getValue().getDescriptiveDeploymentId().getValue(),
                equalTo(UsersHelper.DOMAIN1.getCodename().toLowerCase() + "-namewithspaces-" + 10));
    }

    @Test
    void shouldNotDeployApplicationInstanceWhenNameIsUsedInDeployedInstance() throws Exception {
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        Application application = new Application("test", "version", "owner");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        AppInstanceRequest appInstanceDeployed = appInstanceRequest();
        appInstanceDeployed.setName("deployedAppName");

        AppInstance appInstance = new AppInstance(application, domain, "deployedAppName");
        appInstance.setInternalId(new Identifier("1001"));
        List<AppInstance> deployedInstances = new ArrayList<>();
        deployedInstances.add(appInstance);

        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));
        when(applicationInstanceService.create(domain, application, appInstanceDeployed.getName()))
                .thenReturn(new AppInstance(10L, application, domain, appInstanceDeployed.getName()));
        when(modelMapper.map(application.getAppDeploymentSpec(), AppDeploymentSpec.class)).thenReturn(new AppDeploymentSpec());
        when(applicationInstanceService.findAllByDomain(domain)).thenReturn(deployedInstances);
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYED);

        mvc.perform(post("/api/apps/instances/domain/{domainId}", domain.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(appInstanceDeployed))
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isInternalServerError());
        verify(appLifecycleManager, times(0)).deployApplication(ArgumentMatchers.any(AppDeployment.class));

    }

    @Test
    void shouldDeployApplicationInstanceWhenNameIsUsedInDoneInstance() throws Exception {
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        Application application = new Application("test", "version", "owner");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        AppInstanceRequest appInstanceDone = appInstanceRequest();
        appInstanceDone.setName("doneAppName");

        AppInstance appInstance = new AppInstance(application, domain, "doneAppName");
        appInstance.setInternalId(new Identifier("1002"));
        List<AppInstance> deployedInstances = new ArrayList<>();
        deployedInstances.add(appInstance);

        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));
        when(applicationInstanceService.create(domain, application, appInstanceDone.getName()))
                .thenReturn(new AppInstance(10L, application, domain, appInstanceDone.getName()));
        when(modelMapper.map(application.getAppDeploymentSpec(), AppDeploymentSpec.class)).thenReturn(new AppDeploymentSpec());
        when(applicationInstanceService.findAllByDomain(domain)).thenReturn(deployedInstances);
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVED);

        mvc.perform(post("/api/apps/instances/domain/{domainId}", domain.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(appInstanceDone))
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).deployApplication(ArgumentMatchers.any(AppDeployment.class));

    }

    @Test
    void shouldDeployApplicationInstanceWhenNameIsUsedInRemovedInstance() throws Exception {
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        Application application = new Application("test", "version", "owner");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        AppInstanceRequest appInstanceRemoved = appInstanceRequest();
        appInstanceRemoved.setName("removedAppName");

        AppInstance appInstance = new AppInstance(application, domain, "removedAppName");
        appInstance.setInternalId(new Identifier("1003"));
        List<AppInstance> deployedInstances = new ArrayList<>();
        deployedInstances.add(appInstance);

        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));
        when(applicationInstanceService.create(domain, application, appInstanceRemoved.getName()))
                .thenReturn(new AppInstance(10L, application, domain, appInstanceRemoved.getName()));
        when(modelMapper.map(application.getAppDeploymentSpec(), AppDeploymentSpec.class)).thenReturn(new AppDeploymentSpec());
        when(applicationInstanceService.findAllByDomain(domain)).thenReturn(deployedInstances);
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.FAILED_APPLICATION_REMOVED);

        mvc.perform(post("/api/apps/instances/domain/{domainId}", domain.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(appInstanceRemoved))
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).deployApplication(ArgumentMatchers.any(AppDeployment.class));

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

    @Test
    void shouldGetRequestedAppInstance() throws Exception {
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        ApplicationBase applicationBase = new ApplicationBase(1L, "name");
        Application application = new Application("name", "version", "owner");
        application.setId(1L);
        AppInstance appInstance = new AppInstance(10L, application, domain, "test");
        appInstance.setInternalId(new Identifier("1014"));
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        when(applicationBaseRepository.findByName("name")).thenReturn(Optional.of(applicationBase));
        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));
        when(applicationInstanceService.find(10L)).thenReturn(Optional.of(appInstance));
        when(appDeploymentMonitor.userAccessDetails(appInstance.getInternalId())).thenThrow(new InvalidDeploymentIdException());
        mvc.perform(get("/api/apps/instances/{appInstanceId}", 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
    }
}
