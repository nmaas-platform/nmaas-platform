package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.api.dto.applications.AppInstanceBase;
import net.geant.nmaas.api.dto.applications.AppInstanceCompleteDto;
import net.geant.nmaas.api.dto.applications.AppInstanceDto;
import net.geant.nmaas.api.dto.applications.AppInstanceExtendedDto;
import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.api.dto.applications.AppInstanceStatus;
import net.geant.nmaas.api.dto.applications.ApplicationCompleteDto;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodDto;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.users.UserBaseDto;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppInstanceReadControllerTest {

    private static final String NAME = "app1";
    private static final String TEMPLATE_STRING = "{\"template\":\"xD\"}";
    private static final String IDENTIFIER_VALUE = "id12";

    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    private final UserService userService = mock(UserService.class);
    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final ApplicationInstanceBaseService instanceBaseService = mock(ApplicationInstanceBaseService.class);

    private AppInstanceReadController appInstanceReadController;

    private Application application;
    private Domain domain1;
    private Domain domain2;
    private Domain global;
    private User owner;
    private User admin;

    private final Pageable pageable = mock(Pageable.class);

    @BeforeEach
    void setup() {
        ApplicationBase appBase = new ApplicationBase();
        appBase.setId(1L);
        owner = new User("owner");
        owner.setId(2L);
        admin = new User("admin");
        admin.setId(1L);
        domain1 = new Domain(2L, "domain one", "dom-1");
        domain2 = new Domain(3L, "domain two", "dom-2");
        global = new Domain(1L, "GLOBAL", "GLOBAL");
        application = new Application(NAME, "1.0");
        application.setId(1L);
        application.setState(ApplicationState.ACTIVE);
        Set<UserRole> roleSet = new HashSet<>();
        roleSet.add(new UserRole(admin, global, Role.ROLE_SYSTEM_ADMIN));
        admin.setNewRoles(roleSet);

        when(userService.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        when(userService.findByUsername(owner.getUsername())).thenReturn(Optional.of(owner));
        when(userService.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userService.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(applicationBaseService.findByVersionId(1L)).thenReturn(appBase);
        when(domainService.findDomain(global.getId())).thenReturn(Optional.of(global));
        when(domainService.findDomain(domain1.getId())).thenReturn(Optional.of(domain1));

        ConfigWizardTemplate configWizardTemplate = mock(ConfigWizardTemplate.class);
        when(configWizardTemplate.getTemplate()).thenReturn(TEMPLATE_STRING);
        application.setConfigWizardTemplate(configWizardTemplate);

        appInstanceReadController = new AppInstanceReadController(
                new ModelMapper(),
                applicationService,
                applicationBaseService,
                userService,
                appDeploymentMonitor,
                applicationInstanceService,
                domainService,
                appDeploymentRepositoryManager,
                instanceBaseService
        );

        when(pageable.getOffset()).thenReturn(0L);
        when(pageable.getPageNumber()).thenReturn(0);
        when(pageable.getPageSize()).thenReturn(20);
        Sort sort = mock(Sort.class);
        Sort.Order order = mock(Sort.Order.class);
        when(sort.get()).thenReturn(Stream.of(order));
        when(order.getProperty()).thenReturn("createdAt");
        when(pageable.getSort()).thenReturn(sort);

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(appDeploymentMonitor.userAccessDetails(any())).thenThrow(new InvalidDeploymentIdException());
        AppDeployment appDeployment = mock(AppDeployment.class);
        when(appDeployment.getDescriptiveDeploymentId()).thenReturn(new Identifier(IDENTIFIER_VALUE));
        when(appDeploymentRepositoryManager.load(any())).thenReturn(appDeployment);
    }

    @Test
    void shouldGetAllInstancesWithPageable() {
        AppInstanceBase appInstance = new AppInstanceBase();
        appInstance.setApplicationBaseId(application.getId());
        appInstance.setApplicationName(application.getName());
        appInstance.setName(NAME);
        appInstance.setAutoUpgradesEnabled(true);
        appInstance.setDomainId(domain1.getId());
        appInstance.setOwner(new UserBaseDto(owner.getId(), owner.getUsername(), true));
        Page<AppInstanceBase> appInstancePage = new PageImpl<>(List.of(appInstance));

        when(instanceBaseService.findAll(pageable)).thenReturn(appInstancePage);

        Page<AppInstanceBase> result = appInstanceReadController.getAllInstances(pageable);

        assertEquals(1, result.getTotalElements());
        AppInstanceBase appInstanceView = result.getContent().getFirst();
        assertEquals(NAME, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertTrue(appInstanceView.getAutoUpgradesEnabled());
    }

    @Test
    void shouldGetOnlyDeployedInstancesByDefault() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        AppInstance done = appInstance("done", 2L, domain1, admin, "done-id");
        AppInstance removed = appInstance("removed", 3L, domain1, admin, "removed-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running, done, removed));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(appDeploymentMonitor.state(done.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVED);
        when(appDeploymentMonitor.state(removed.getInternalId())).thenReturn(AppLifecycleState.FAILED_APPLICATION_REMOVED);

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances((String) null);

        assertEquals(1, result.size());
        assertEquals("running", result.getFirst().getName());
        assertEquals(AppInstanceState.RUNNING, result.getFirst().getState());
    }

    @Test
    void shouldFilterAllInstancesByStatus() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        AppInstance done = appInstance("done", 2L, domain1, admin, "done-id");
        AppInstance removed = appInstance("removed", 3L, domain1, admin, "removed-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running, done, removed));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(appDeploymentMonitor.state(done.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVED);
        when(appDeploymentMonitor.state(removed.getInternalId())).thenReturn(AppLifecycleState.FAILED_APPLICATION_REMOVED);

        List<AppInstanceBase> deployed = appInstanceReadController.getAllInstances("deployed");
        List<AppInstanceBase> undeployed = appInstanceReadController.getAllInstances("undeployed");
        List<AppInstanceBase> unknownStatus = appInstanceReadController.getAllInstances("unknown");

        assertEquals(List.of("running"), deployed.stream().map(AppInstanceBase::getName).toList());
        assertEquals(List.of("done", "removed"), undeployed.stream().map(AppInstanceBase::getName).toList());
        assertEquals(List.of("running", "done", "removed"), unknownStatus.stream().map(AppInstanceBase::getName).toList());
    }

    @Test
    void shouldGetAllInstancesWithParamsWhenIsSystemAdminAndDomainIsGlobal() {
        AppInstance appInstance = new AppInstance(application, NAME, domain1, admin, false);
        when(applicationInstanceService.findAll()).thenReturn(List.of(appInstance));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances(global.getId(), principal, "deployed");

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.getFirst();
        assertEquals(NAME, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
        assertFalse(appInstanceView.getAutoUpgradesEnabled());
    }

    @Test
    void shouldGetOnlyDeployedDomainInstancesByDefault() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        AppInstance done = appInstance("done", 2L, domain1, admin, "done-id");
        AppInstance removed = appInstance("removed", 3L, domain1, admin, "removed-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running, done, removed));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        when(appDeploymentMonitor.state(done.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_REMOVED);
        when(appDeploymentMonitor.state(removed.getInternalId())).thenReturn(AppLifecycleState.FAILED_APPLICATION_REMOVED);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances(global.getId(), principal, null);

        assertEquals(1, result.size());
        assertEquals("running", result.getFirst().getName());
        assertEquals(AppInstanceState.RUNNING, result.getFirst().getState());
    }

    @Test
    void shouldGetAllMyInstancesInAllDomainWhenIsSystemAdminAndDomainIsGlobal() {
        AppInstance appInstance = new AppInstance(application, NAME, domain1, admin, false);
        when(applicationInstanceService.findAllByOwner(admin)).thenReturn(List.of(appInstance));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceReadController.getMyAllInstances(principal);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.getFirst();
        assertEquals(NAME, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
    }

    @Test
    void shouldGetAllUserInstancesInDomain() {
        AppInstance appInstance = new AppInstance(application, NAME, domain1, admin, false);
        when(applicationInstanceService.findAllByOwner(admin.getId(), domain1.getId())).thenReturn(List.of(appInstance));

        List<AppInstanceBase> result = appInstanceReadController.getUserAllInstances(domain1.getId(), admin.getUsername());

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.getFirst();
        assertEquals(NAME, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
    }

    @Test
    void shouldGetAllMyInstancesInDomain() {
        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);
        when(applicationInstanceService.findAllByOwner(owner)).thenReturn(List.of(appInstance));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        List<AppInstanceBase> result = appInstanceReadController.getMyAllInstances(principal);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.getFirst();
        assertEquals(NAME, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
    }

    @Test
    void shouldGetAllRunningInstancesOfUserInDomain() {
        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);
        when(applicationInstanceService.findAllByDomain(domain1)).thenReturn(List.of(appInstance));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());
        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);

        List<AppInstanceDto> result = appInstanceReadController.getRunningAppInstances(domain1.getId(), principal);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.getFirst();
        assertEquals(NAME, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
    }

    @Disabled
    @Test
    void shouldGetAppInstance() {
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.getAppDeploymentSpec().setKubernetesTemplate(new KubernetesTemplate());
        application.getAppDeploymentSpec().getKubernetesTemplate().setChart(new KubernetesChart());
        application.getAppDeploymentSpec().getKubernetesTemplate().getChart().setVersion("chart_version");

        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.getAppConfigurationSpec().setConfigUpdateEnabled(true);

        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);

        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());
        when(applicationBaseService.findByVersionId(1L)).thenReturn(new ApplicationBase(NAME));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        AppInstanceCompleteDto appInstanceView = appInstanceReadController.getAppInstance(1L, principal);

        assertEquals(NAME, appInstanceView.appBaseName());
        assertEquals(IDENTIFIER_VALUE, appInstanceView.descriptiveDeploymentId());
        assertEquals(domain1.getId(), appInstanceView.domainId());

        MissingElementException me = assertThrows(MissingElementException.class,
                () -> appInstanceReadController.getAppInstance(-1L, principal)
        );

        assertEquals("App instance not found.", me.getMessage());
    }

    @Test
    void shouldConvertAppInstanceToAppInstanceDtoWithApplicationIdAndDomainId() {
        ModelMapper modelMapper = new ModelMapper();
        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);
        AppInstanceDto appInstanceView = modelMapper.map(appInstance, AppInstanceDto.class);
        assertEquals(application.getId(), appInstanceView.getApplicationId());
        assertEquals(domain1.getId(), appInstanceView.getDomainId());
    }

    @Test
    void shouldConvertAppInstanceToAppInstanceExtendedDtoWithApplicationViewAndDomainView() {
        ModelMapper modelMapper = new ModelMapper();
        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);
        AppInstanceExtendedDto appInstanceView = modelMapper.map(appInstance, AppInstanceExtendedDto.class);

        assertEquals(application.getId(), appInstanceView.getApplicationId());
        assertEquals(domain1.getId(), appInstanceView.getDomainId());

        ApplicationCompleteDto av = appInstanceView.getApplication();
        assertEquals(application.getId(), av.getApplication().getId());
        assertEquals(application.getName(), av.getApplication().getName());

        DomainBaseDto dv = appInstanceView.getDomain();
        assertEquals(domain1.getId(), dv.getId());
        assertEquals(domain1.getName(), dv.getName());
        assertEquals(domain1.getCodename(), dv.getCodename());
        assertNull(av.getApplicationBase());
    }

    @Test
    void shouldGetAppInstanceState() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);
        appInstance.setId(1L);
        appInstance.setInternalId(new Identifier(IDENTIFIER_VALUE));

        when(appDeploymentMonitor.state(any(Identifier.class))).thenReturn(AppLifecycleState.APPLICATION_DEPLOYED);
        when(appDeploymentMonitor.previousState(any(Identifier.class))).thenReturn(AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS);
        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());

        AppInstanceStatus ais = appInstanceReadController.getState(1L, principal);

        assertEquals(appInstance.getId(), ais.appInstanceId());
    }

    @Test
    void shouldSetExternalAccessEnabledTrueWhenInstanceHasExternalAccessMethod() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        doReturn(new AppUiAccessDetails(new HashSet<>(Set.of(
                accessMethod(ServiceAccessMethodTypeDto.EXTERNAL, "ui", "https", "https://app.example.com"),
                accessMethod(ServiceAccessMethodTypeDto.INTERNAL, "ssh", "ssh", "ssh://app.example.com")
        )))).when(appDeploymentMonitor).userAccessDetails(running.getInternalId());

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances((String) null);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getExternalAccessEnabled());
    }

    @Test
    void shouldSetExternalAccessEnabledTrueWhenInstanceHasDefaultAccessMethod() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        doReturn(new AppUiAccessDetails(new HashSet<>(Set.of(
                accessMethod(ServiceAccessMethodTypeDto.DEFAULT, "ui", "https", "https://app.example.com")
        )))).when(appDeploymentMonitor).userAccessDetails(running.getInternalId());

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances((String) null);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getExternalAccessEnabled());
    }

    @Test
    void shouldSetExternalAccessEnabledFalseWhenInstanceHasNoExternalOrDefaultAccessMethod() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        doReturn(new AppUiAccessDetails(new HashSet<>(Set.of(
                accessMethod(ServiceAccessMethodTypeDto.INTERNAL, "ssh", "ssh", "ssh://app.example.com"),
                accessMethod(ServiceAccessMethodTypeDto.LOCAL, "local", "http", "http://app.local")
        )))).when(appDeploymentMonitor).userAccessDetails(running.getInternalId());

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances((String) null);

        assertEquals(1, result.size());
        assertFalse(result.getFirst().getExternalAccessEnabled());
    }

    @Test
    void shouldSetExternalAccessEnabledFalseWhenAccessDetailsNotAvailable() {
        AppInstance running = appInstance("running", 1L, domain1, admin, "running-id");
        when(applicationInstanceService.findAll()).thenReturn(List.of(running));
        when(appDeploymentMonitor.state(running.getInternalId())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);
        // userAccessDetails(any()) already stubbed to throw in setup()

        List<AppInstanceBase> result = appInstanceReadController.getAllInstances((String) null);

        assertEquals(1, result.size());
        assertFalse(result.getFirst().getExternalAccessEnabled());
    }

    private ServiceAccessMethodDto accessMethod(ServiceAccessMethodTypeDto type, String name, String protocol, String url) {
        return new ServiceAccessMethodDto(type, name, protocol, url);
    }

    private AppInstance appInstance(String name, Long id, Domain domain, User owner, String internalId) {
        AppInstance appInstance = new AppInstance(application, name, domain, owner, false);
        appInstance.setId(id);
        appInstance.setInternalId(new Identifier(internalId));
        return appInstance;
    }
}
