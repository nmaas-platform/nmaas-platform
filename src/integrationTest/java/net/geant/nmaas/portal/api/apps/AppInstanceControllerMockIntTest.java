package net.geant.nmaas.portal.api.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.applications.AppInstanceRequest;
import net.geant.nmaas.api.dto.applications.AppInstanceViewExtendedDto;
import net.geant.nmaas.api.dto.kubernetes.RemoteKClusterDto;
import net.geant.nmaas.api.dto.users.UserBase;
import net.geant.nmaas.api.dto.users.UserViewMinimal;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManager;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AppInstanceControllerMockIntTest extends BaseControllerTestSetup {

    private static final Domain DOMAIN = UsersHelper.DOMAIN1;

    @MockitoSpyBean
    private DomainService domainService;

    @MockitoSpyBean
    private UserService userService;

    @MockitoBean
    private ApplicationService applicationService;

    @Autowired
    private ModelMapper modelMapper;

    @MockitoBean
    private ApplicationInstanceService applicationInstanceService;

    @MockitoBean
    private AppInstanceRepository applicationInstanceRepository;

    @MockitoBean
    private AppLifecycleManager appLifecycleManager;

    @MockitoBean
    private ApplicationBaseRepository applicationBaseRepository;

    @MockitoBean
    private AppDeploymentMonitor appDeploymentMonitor;

    @MockitoBean
    private RemoteClusterManager clusterManager;

    @BeforeEach
    void setup() {
        this.mvc = this.createMVC();
    }

    @Test
    void shouldDeployApplicationInstance() throws Exception {
        User user = UsersHelper.ADMIN;
        Application application = new Application("name with spaces", "version");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        AppInstanceRequest appInstanceRequest = appInstanceRequest(null);
        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(applicationInstanceService.create(DOMAIN, application, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled()))
                .thenReturn(new AppInstance(10L, application, DOMAIN, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled()));
        mvc.perform(post("/api/v1/apps/instances/domain/{domainId}", DOMAIN.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceRequest))
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        ArgumentCaptor<AppDeployment> appDeployment = ArgumentCaptor.forClass(AppDeployment.class);
        verify(appLifecycleManager, times(1)).deployApplication(appDeployment.capture(), ArgumentMatchers.eq(user.getUsername()));
        assertThat(appDeployment.getValue().getInstanceId(), equalTo(10L));
        assertThat(appDeployment.getValue().getDescriptiveDeploymentId().getValue(),
                equalTo(UsersHelper.DOMAIN1.getCodename().toLowerCase() + "-namewithspaces-" + 10));
    }

    @Test
    void shouldDeployApplicationInstanceWithRemoteCluster() throws Exception {
        User user = UsersHelper.ADMIN;
        Application application = new Application("name with spaces", "version");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        AppInstanceRequest appInstanceRequest = appInstanceRequest(null);
        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(clusterManager.getClustersInDomain(DOMAIN.getId())).thenReturn(List.of(remoteCluster(100L)));
        when(applicationInstanceService.create(DOMAIN, application, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled()))
                .thenReturn(new AppInstance(10L, application, DOMAIN, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled()));
        mvc.perform(post("/api/v1/apps/instances/domain/{domainId}", DOMAIN.getId())
                        .param("clusterId", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceRequest))
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        ArgumentCaptor<AppDeployment> appDeployment = ArgumentCaptor.forClass(AppDeployment.class);
        verify(appLifecycleManager, times(1)).deployApplication(appDeployment.capture(), ArgumentMatchers.eq(user.getUsername()));
        assertThat(appDeployment.getValue().getInstanceId(), equalTo(10L));
        assertThat(appDeployment.getValue().getDescriptiveDeploymentId().getValue(),
                equalTo(UsersHelper.DOMAIN1.getCodename().toLowerCase() + "-namewithspaces-" + 10));
        assertNotNull(appDeployment.getValue().getRemoteClusterId());
    }

    private RemoteKClusterDto remoteCluster(Long id) {
        RemoteKClusterDto cluster = new RemoteKClusterDto();
        cluster.setId(id);
        return cluster;
    }

    @Test
    void shouldDeployApplicationInstanceAsAdminInDomain() {
        User user = UsersHelper.DOMAIN1_ADMIN;
        Application application = new Application("name", "version");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        AppInstanceRequest appInstanceRequest = appInstanceRequest(null);
        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(applicationInstanceService.create(DOMAIN, application, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled()))
                .thenReturn(new AppInstance(10L, application, DOMAIN, appInstanceRequest.name(), appInstanceRequest.autoUpgradesEnabled()));
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/v1/apps/instances/domain/{domainId}", DOMAIN.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(appInstanceRequest))
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldNotDeployApplicationInstanceWhenNameIsUsedInDeployedInstance() throws Exception {
        User user = UsersHelper.ADMIN;
        Application application = new Application("test", "version");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        AppInstanceRequest appInstanceDeployed = appInstanceRequest("deployedAppName");

        AppInstance appInstance = new AppInstance(application, DOMAIN, "deployedAppName", true);
        appInstance.setInternalId(new Identifier("1001"));
        List<AppInstance> deployedInstances = new ArrayList<>();
        deployedInstances.add(appInstance);

        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(applicationInstanceService.create(DOMAIN, application, appInstanceDeployed.name(), appInstanceDeployed.autoUpgradesEnabled()))
                .thenReturn(new AppInstance(10L, application, DOMAIN, appInstanceDeployed.name(), appInstanceDeployed.autoUpgradesEnabled()));
        when(applicationInstanceService.findAllByDomain(DOMAIN)).thenReturn(deployedInstances);
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYED);

        mvc.perform(post("/api/v1/apps/instances/domain/{domainId}", DOMAIN.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceDeployed))
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isBadRequest());
        verify(appLifecycleManager, times(0)).deployApplication(ArgumentMatchers.any(AppDeployment.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void shouldDeployApplicationInstanceWhenNameIsUsedInDoneInstance() throws Exception {
        User user = UsersHelper.ADMIN;
        Application application = new Application("test", "version");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        AppInstanceRequest appInstanceDone = appInstanceRequest("doneAppName");

        AppInstance appInstance = new AppInstance(application, DOMAIN, "doneAppName", true);
        appInstance.setInternalId(new Identifier("1002"));
        List<AppInstance> deployedInstances = new ArrayList<>();
        deployedInstances.add(appInstance);

        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(applicationInstanceService.create(DOMAIN, application, appInstanceDone.name(), appInstance.isAutoUpgradesEnabled()))
                .thenReturn(new AppInstance(10L, application, DOMAIN, appInstanceDone.name(), appInstance.isAutoUpgradesEnabled()));

        when(applicationInstanceService.findAllByDomain(DOMAIN)).thenReturn(deployedInstances);
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVED);

        mvc.perform(post("/api/v1/apps/instances/domain/{domainId}", DOMAIN.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceDone))
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).deployApplication(ArgumentMatchers.any(AppDeployment.class), ArgumentMatchers.eq(user.getUsername()));
    }

    @Test
    void shouldDeployApplicationInstanceWhenNameIsUsedInRemovedInstance() throws Exception {
        User user = UsersHelper.ADMIN;
        Application application = new Application("test", "version");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());

        AppInstanceRequest appInstanceRemoved = appInstanceRequest("removedAppName");

        AppInstance appInstance = new AppInstance(application, DOMAIN, "removedAppName", true);
        appInstance.setInternalId(new Identifier("1003"));
        List<AppInstance> deployedInstances = new ArrayList<>();
        deployedInstances.add(appInstance);

        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
        when(applicationInstanceService.create(DOMAIN, application, appInstanceRemoved.name(), appInstanceRemoved.autoUpgradesEnabled()))
                .thenReturn(new AppInstance(10L, application, DOMAIN, appInstanceRemoved.name(), appInstanceRemoved.autoUpgradesEnabled()));
        when(applicationInstanceService.findAllByDomain(DOMAIN)).thenReturn(deployedInstances);
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.FAILED_APPLICATION_REMOVED);

        mvc.perform(post("/api/v1/apps/instances/domain/{domainId}", DOMAIN.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appInstanceRemoved))
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).deployApplication(ArgumentMatchers.any(AppDeployment.class), ArgumentMatchers.eq(user.getUsername()));
    }

    private AppInstanceRequest appInstanceRequest(String name) {
        return new AppInstanceRequest(1L, StringUtils.isNoneBlank(name) ? name : "appInstanceName", true);
    }

    @Test
    void shouldRestartApplication() throws Exception {
        User user = UsersHelper.ADMIN;
        AppInstance appInstance = new AppInstance(new Application("test", "testVersion"), "test", DOMAIN, user, true);
        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceRepository.findById(10L)).thenReturn(Optional.of(appInstance));
        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/restart", 1L)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).restartApplication(appInstance.getInternalId(), user.getUsername());
    }

    @Test
    void shouldRestartRedeployAndUpgradeApplicationAsAdminInDomain() throws Exception {
        User user = UsersHelper.DOMAIN1_ADMIN;
        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        AppInstance appInstance = new AppInstance(new Application("test", "testVersion"), "test", DOMAIN, user, true);
        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceRepository.findById(1L)).thenReturn(Optional.of(appInstance));
        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/restart", 1L)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).restartApplication(appInstance.getInternalId(), user.getUsername());
        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/redeploy", 1L)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).redeployApplication(appInstance.getInternalId(), user.getUsername());
        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/upgrade/{targetAppInstanceId}", 1L, 2L)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).upgradeApplication(appInstance.getInternalId(), Identifier.newInstance(2L), user.getUsername());
    }

    @Test
    void shouldNotRestartNorRedeployNotUpgradeApplicationAsUserInDomain() {
        User user = UsersHelper.DOMAIN1_USER1;
        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        AppInstance appInstance = new AppInstance(new Application("test", "testVersion"), "test", DOMAIN, user, true);
        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceRepository.findById(1L)).thenReturn(Optional.of(appInstance));
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/restart", 1L)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/redeploy", 1L)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/upgrade/{targetAppInstanceId}", 1L, 2L)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isUnauthorized());
        });
    }

    @Test
    void shouldThrowAnExceptionWhenInputIsIncorrect() {
        when(applicationInstanceService.find(0L)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/restart", 0L)
                            .header("Authorization", "Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                    .andExpect(status().is(404));
        });
    }

    @Test
    void shouldGetRequestedAppInstanceAndCheckStatusSinceAdmin() {
        User user = UsersHelper.ADMIN;

        ApplicationBase applicationBase = testApplicationBase(user);
        Application application = testApplication();
        AppInstance appInstance = testAppInstance(DOMAIN, application, user);

        mockAppInstanceGetProcess(DOMAIN, user, applicationBase, application, appInstance);

        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/v1/apps/instances/{appInstanceId}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isOk());
        });

        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/check", 10L)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldNotGetRequestedAppInstanceNorCheckStatusSinceGuestInDomain() {
        User user = UsersHelper.DOMAIN1_GUEST;

        ApplicationBase applicationBase = testApplicationBase(user);
        Application application = testApplication();
        AppInstance appInstance = testAppInstance(DOMAIN, application, user);
        mockAppInstanceGetProcess(DOMAIN, user, applicationBase, application, appInstance);

        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/v1/apps/instances/{appInstanceId}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/check", 10L)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isUnauthorized());
        });
    }

    @Test
    void shouldGetRequestedAppInstanceButNotCheckStatusSinceUserInDomain() {
        User user = UsersHelper.DOMAIN1_USER1;

        ApplicationBase applicationBase = testApplicationBase(user);
        Application application = testApplication();
        AppInstance appInstance = testAppInstance(DOMAIN, application, user);
        mockAppInstanceGetProcess(DOMAIN, user, applicationBase, application, appInstance);

        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/v1/apps/instances/{appInstanceId}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isOk());
        });

        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/check", 10L)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isUnauthorized());
        });
    }

    @Test
    void shouldDeleteAppInstanceSinceAdminInDomain() {
        User user = UsersHelper.DOMAIN1_ADMIN;

        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        AppInstance appInstance = new AppInstance(new Application("test", "testVersion"), "test", DOMAIN, UsersHelper.ADMIN, true);
        when(applicationInstanceService.find(10L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceRepository.findById(10L)).thenReturn(Optional.of(appInstance));

        assertDoesNotThrow(() -> {
            mvc.perform(delete("/api/v1/apps/instances/{appInstanceId}", 10L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldSetApplicationInstanceMembersWhenAppInstanceOwner() throws Exception {
        User user = UsersHelper.ADMIN;

        ApplicationBase applicationBase = testApplicationBase(user);
        Application application = testApplication();
        AppInstance appInstance = testAppInstance(DOMAIN, application, user);
        mockAppInstanceGetProcess(DOMAIN, user, applicationBase, application, appInstance);

        mvc.perform(get("/api/v1/apps/instances/{appInstanceId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());

        User u1 = UsersHelper.DOMAIN1_ADMIN;
        u1.getSshKeys().add(new SSHKeyEntity(u1, "test", "longlong"));
        User u2 = UsersHelper.DOMAIN1_USER1;
        User u3 = UsersHelper.DOMAIN2_USER1;

        List<UserBase> members = new ArrayList<>();
        members.add(modelMapper.map(u1, UserBase.class));
        members.add(modelMapper.map(u2, UserBase.class));
        members.add(modelMapper.map(u3, UserBase.class));

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(userService.findById(u1.getId())).thenReturn(Optional.of(u1));
        when(userService.findById(u2.getId())).thenReturn(Optional.of(u2));
        when(userService.findById(u3.getId())).thenReturn(Optional.of(u3));

        when(userService.findByUsername(u1.getUsername())).thenReturn(Optional.of(u1));
        when(userService.findByUsername(u2.getUsername())).thenReturn(Optional.of(u2));
        when(userService.findByUsername(u3.getUsername())).thenReturn(Optional.of(u3));

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/members", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(members))
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().is2xxSuccessful());

        String data = mvc.perform(get("/api/v1/apps/instances/{appInstanceId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Set<UserViewMinimal> retrieved = objectMapper.readValue(data, AppInstanceViewExtendedDto.class).getMembers();
        assertEquals(1, retrieved.size());
    }

    @Test
    void shouldUpgradeApplication() throws Exception {
        User user = UsersHelper.ADMIN;
        AppInstance appInstance = new AppInstance(new Application("test", "testVersion"), "test", DOMAIN, user, true);
        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceRepository.findById(10L)).thenReturn(Optional.of(appInstance));

        mvc.perform(post("/api/v1/apps/instances/{appInstanceId}/upgrade/{targetAppInstanceId}", 1L, 2L)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).upgradeApplication(appInstance.getInternalId(), Identifier.newInstance(2L), user.getUsername());
    }

    private void mockAppInstanceGetProcess(Domain domain, User user, ApplicationBase applicationBase, Application application, AppInstance appInstance) {
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));
        when(userService.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationBaseRepository.findByVersionId(1L)).thenReturn(Optional.of(applicationBase));
        when(applicationService.findApplication(1L)).thenReturn(Optional.of(application));
        when(applicationInstanceService.find(10L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceRepository.findById(10L)).thenReturn(Optional.of(appInstance));
        when(appDeploymentMonitor.userAccessDetails(appInstance.getInternalId())).thenThrow(new InvalidDeploymentIdException());
    }

    private ApplicationBase testApplicationBase(User user) {
        ApplicationBase applicationBase = new ApplicationBase(1L, "name");
        applicationBase.setOwner(user.getUsername());
        return applicationBase;
    }

    private Application testApplication() {
        Application application = new Application("name", "version");
        application.setId(1L);
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        return application;
    }

    private AppInstance testAppInstance(Domain domain, Application application, User user) {
        AppInstance appInstance = new AppInstance(10L, application, domain, "test", true);
        appInstance.setInternalId(new Identifier("1014"));
        appInstance.setOwner(user);
        return appInstance;
    }

}
