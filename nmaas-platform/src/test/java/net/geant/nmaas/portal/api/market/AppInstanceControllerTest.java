package net.geant.nmaas.portal.api.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.*;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.api.domain.AppInstanceViewExtended;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.*;
import java.util.stream.Stream;


public class AppInstanceControllerTest {

    private AppLifecycleManager appLifecycleManager = mock(AppLifecycleManager.class);
    private AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private DomainService domainService = mock(DomainService.class);
    private ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    private UserService userService = mock(UserService.class);

    private AppInstanceController appInstanceController;

    private Application application;
    private Domain domain;
    private Domain global;
    private String name = "app1";
    private String templateString = "{\"template\":\"xD\"}";
    private String identifierValue = "id12";
    private User owner;
    private User admin;

    private Pageable pageable = mock(Pageable.class);
    private Pageable pageableInvalid = mock(Pageable.class);

    @BeforeEach
    public void setup() {
        owner = new User("owner");
        owner.setId(2L);
        admin = new User("admin");
        admin.setId(1L);
        domain = new Domain(2L, "domain one", "dom-1");
        global = new Domain(1L, "GLOBAL", "GLOBAL");
        application = new Application(name,"1.0","admin");
        application.setId(1L);
        Set<UserRole> roleSet = new HashSet<>();
        roleSet.add(new UserRole(admin, global, Role.ROLE_SYSTEM_ADMIN));
        admin.setNewRoles(roleSet);

        when(userService.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        when(userService.findByUsername(owner.getUsername())).thenReturn(Optional.of(owner));
        when(userService.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userService.findById(owner.getId())).thenReturn(Optional.of(owner));

        when(domainService.findDomain(global.getId())).thenReturn(Optional.of(global));
        when(domainService.findDomain(domain.getId())).thenReturn(Optional.of(domain));

        ConfigWizardTemplate configWizardTemplate = mock(ConfigWizardTemplate.class);
        when(configWizardTemplate.getTemplate()).thenReturn(templateString);
        application.setConfigWizardTemplate(configWizardTemplate);

        appInstanceController = new AppInstanceController(
                appLifecycleManager,
                appDeploymentMonitor,
                applicationInstanceService,
                domainService,
                appDeploymentRepositoryManager);
        appInstanceController.modelMapper = new ModelMapper();
        appInstanceController.users = userService;

        when(pageable.getOffset()).thenReturn(0L);
        when(pageable.getPageNumber()).thenReturn(0);
        when(pageable.getPageSize()).thenReturn(20);
        Sort sort = mock(Sort.class);
        Sort.Order order = mock(Sort.Order.class);
        when(sort.get()).thenReturn(Stream.of(order));
        when(order.getProperty()).thenReturn("createdAt");
        when(pageable.getSort()).thenReturn(sort);

        when(pageableInvalid.getOffset()).thenReturn(0L);
        when(pageableInvalid.getPageNumber()).thenReturn(0);
        when(pageableInvalid.getPageSize()).thenReturn(20);
        Sort sortI = mock(Sort.class);
        Sort.Order orderI = mock(Sort.Order.class);
        when(sortI.get()).thenReturn(Stream.of(orderI));
        when(orderI.getProperty()).thenReturn("randomNotExistingProperty");
        when(pageableInvalid.getSort()).thenReturn(sortI);

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));

        when(appDeploymentMonitor.userAccessDetails(any())).thenThrow(new InvalidDeploymentIdException());
        AppDeployment appDeployment = mock(AppDeployment.class);
        when(appDeployment.getDescriptiveDeploymentId()).thenReturn(new Identifier(identifierValue));
        when(appDeploymentRepositoryManager.load(any())).thenReturn(appDeployment);
    }

    @Test
    public void shouldGetAllInstancesWithPageable() {
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAll(pageable)).thenReturn(appInstancePage);

        List<AppInstanceView> result = appInstanceController.getAllInstances(pageable);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllInstancesWithParamsWhenIsSystemAdminAndDomainIsGlobal() {
        AppInstance appInstance = new AppInstance(application, name, domain, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAll(pageable)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceView> result = appInstanceController.getAllInstances(global.getId(), principal, pageable);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllInstancesWithParamsWhenIsSystemAdminAndDomainIsGlobalAndPageableInvalid() {
        AppInstance appInstance = new AppInstance(application, name, domain, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAll(null)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceView> result = appInstanceController.getAllInstances(global.getId(), principal, pageableInvalid);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllInstancesWithParamsWhenIsUserAndPageableInvalid() {
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByDomain(domain,null)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceView> result = appInstanceController.getAllInstances(domain.getId(), principal, pageableInvalid);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllMyInstancesInAllDomainWhenIsSystemAdminAndDomainIsGlobal() {
        AppInstance appInstance = new AppInstance(application, name, domain, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByOwner(admin, pageable)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceView> result = appInstanceController.getMyAllInstances(global.getId(), principal, pageable);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllUserInstancesInDomain() {
        AppInstance appInstance = new AppInstance(application, name, domain, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByOwner(admin, domain, pageable)).thenReturn(appInstancePage);

        List<AppInstanceView> result = appInstanceController.getUserAllInstances(domain.getId(), admin.getUsername(), pageable);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllMyInstancesInDomain() {
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByOwner(owner, pageable)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        List<AppInstanceView> result = appInstanceController.getMyAllInstances(principal, pageable);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllRunningInstancesOfUserInDomain() {
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);

        when(applicationInstanceService.findAllByOwnerAndDomain(owner, domain)).thenReturn(appInstanceList);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);

        List<AppInstanceView> result = appInstanceController.getRunningAppInstances(domain.getId(), principal);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAppInstance() {
        AppInstance appInstance = new AppInstance(application, name, domain, owner);

        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        AppInstanceViewExtended appInstanceView = appInstanceController.getAppInstance(1L, principal);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
        assertEquals(application.getId(), appInstanceView.getApplication().getId());
        assertEquals(domain.getId(), appInstanceView.getDomain().getId());

        MissingElementException me = assertThrows(MissingElementException.class,
                () -> appInstanceController.getAppInstance(-1L, principal)
        );

        assertEquals("App instance not found.", me.getMessage());
    }

    @Test
    public void shouldConvertAppInstanceToAppInstanceViewWithApplicationIdAndDomainId() {
        ModelMapper modelMapper = new ModelMapper();
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        AppInstanceView appInstanceView = modelMapper.map(appInstance, AppInstanceView.class);
        assertEquals(application.getId(), appInstanceView.getApplicationId());
        assertEquals(domain.getId(), appInstanceView.getDomainId());
    }

    @Test
    public void shouldConvertAppInstanceToAppInstanceViewExtendedWithApplicationViewAndDomainView() {
        ModelMapper modelMapper = new ModelMapper();
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        AppInstanceViewExtended appInstanceView = modelMapper.map(appInstance, AppInstanceViewExtended.class);
        assertEquals(application.getId(), appInstanceView.getApplicationId());
        assertEquals(domain.getId(), appInstanceView.getDomainId());
        DomainView dv = appInstanceView.getDomain();
        ApplicationView av = appInstanceView.getApplication();
        assertEquals(application.getId(), av.getId());
        assertEquals(application.getName(), av.getName());
        assertEquals(domain.getId(), dv.getId());
        assertEquals(domain.getName(), dv.getName());
        assertEquals(domain.getCodename(), dv.getCodename());
    }
}
