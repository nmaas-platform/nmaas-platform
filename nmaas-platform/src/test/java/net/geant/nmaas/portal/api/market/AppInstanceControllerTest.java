package net.geant.nmaas.portal.api.market;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
import net.geant.nmaas.orchestration.*;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AppInstanceControllerTest {

    private final AppLifecycleManager appLifecycleManager = mock(AppLifecycleManager.class);
    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    private final UserService userService = mock(UserService.class);
    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

    private AppInstanceController appInstanceController;

    private Application application;
    private Domain domain1;
    private Domain domain2;
    private Domain global;
    private final String name = "app1";
    private String templateString = "{\"template\":\"xD\"}";
    private String identifierValue = "id12";
    private User owner;
    private User admin;

    private final Pageable pageable = mock(Pageable.class);
    private final Pageable pageableInvalid = mock(Pageable.class);

    @BeforeEach
    public void setup() {
        owner = new User("owner");
        owner.setId(2L);
        admin = new User("admin");
        admin.setId(1L);
        domain1 = new Domain(2L, "domain one", "dom-1");
        domain2 = new Domain(3L, "domain two", "dom-2");
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
        when(domainService.findDomain(domain1.getId())).thenReturn(Optional.of(domain1));

        ConfigWizardTemplate configWizardTemplate = mock(ConfigWizardTemplate.class);
        when(configWizardTemplate.getTemplate()).thenReturn(templateString);
        application.setConfigWizardTemplate(configWizardTemplate);

        appInstanceController = new AppInstanceController(
                new ModelMapper(),
                applicationService,
                applicationBaseService,
                userService,
                appLifecycleManager,
                appDeploymentMonitor,
                applicationInstanceService,
                domainService,
                appDeploymentRepositoryManager,
                applicationEventPublisher
        );
        appInstanceController.modelMapper = new ModelMapper();
        appInstanceController.userService = userService;

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
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAll(pageable)).thenReturn(appInstancePage);

        List<AppInstanceBase> result = appInstanceController.getAllInstances(pageable);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllInstancesWithParamsWhenIsSystemAdminAndDomainIsGlobal() {
        AppInstance appInstance = new AppInstance(application, name, domain1, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAll(pageable)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceController.getAllInstances(global.getId(), principal, pageable);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllInstancesWithParamsWhenIsSystemAdminAndDomainIsGlobalAndPageableInvalid() {
        AppInstance appInstance = new AppInstance(application, name, domain1, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);

        when(applicationInstanceService.findAll()).thenReturn(appInstanceList);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceController.getAllInstances(global.getId(), principal, pageableInvalid);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllInstancesWithParamsWhenIsUserAndPageableInvalid() {
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByDomain(domain1,null)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceController.getAllInstances(domain1.getId(), principal, pageableInvalid);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllMyInstancesInAllDomainWhenIsSystemAdminAndDomainIsGlobal() {
        AppInstance appInstance = new AppInstance(application, name, domain1, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByOwner(admin, pageable)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(admin.getUsername());

        List<AppInstanceBase> result = appInstanceController.getMyAllInstances(global.getId(), principal, pageable);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllUserInstancesInDomain() {
        AppInstance appInstance = new AppInstance(application, name, domain1, admin);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByOwner(admin, domain1, pageable)).thenReturn(appInstancePage);

        List<AppInstanceBase> result = appInstanceController.getUserAllInstances(domain1.getId(), admin.getUsername(), pageable);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(admin.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllMyInstancesInDomain() {
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAllByOwner(owner, pageable)).thenReturn(appInstancePage);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        List<AppInstanceBase> result = appInstanceController.getMyAllInstances(principal, pageable);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllRunningInstancesOfUserInDomain() {
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);

        when(applicationInstanceService.findAllByOwnerAndDomain(owner, domain1)).thenReturn(appInstanceList);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);

        List<AppInstanceView> result = appInstanceController.getRunningAppInstances(domain1.getId(), principal);

        assertEquals(1, result.size());
        AppInstanceBase appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
//        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAppInstance() {
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);

        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());
        when(applicationBaseService.findByName(anyString())).thenReturn(new ApplicationBase(name));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        AppInstanceViewExtended appInstanceView = appInstanceController.getAppInstance(1L, principal);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
//        assertEquals(application.getId(), appInstanceView.getApplication().getId());
        assertEquals(domain1.getId(), appInstanceView.getDomain().getId());

        MissingElementException me = assertThrows(MissingElementException.class,
                () -> appInstanceController.getAppInstance(-1L, principal)
        );

        assertEquals("App instance not found.", me.getMessage());
    }

    @Test
    public void shouldConvertAppInstanceToAppInstanceViewWithApplicationIdAndDomainId() {
        ModelMapper modelMapper = new ModelMapper();
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        AppInstanceView appInstanceView = modelMapper.map(appInstance, AppInstanceView.class);
        assertEquals(application.getId(), appInstanceView.getApplicationId());
        assertEquals(domain1.getId(), appInstanceView.getDomainId());
    }

    @Test
    public void shouldConvertAppInstanceToAppInstanceViewExtendedWithApplicationViewAndDomainView() {
        ModelMapper modelMapper = new ModelMapper();
        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        AppInstanceViewExtended appInstanceView = modelMapper.map(appInstance, AppInstanceViewExtended.class);

        assertEquals(application.getId(), appInstanceView.getApplicationId());
        assertEquals(domain1.getId(), appInstanceView.getDomainId());

        ApplicationDTO av = appInstanceView.getApplication();
        assertEquals(application.getId(), av.getApplication().getId());
        assertEquals(application.getName(), av.getApplication().getName());

        DomainBase dv = appInstanceView.getDomain();
        assertEquals(domain1.getId(), dv.getId());
        assertEquals(domain1.getName(), dv.getName());
        assertEquals(domain1.getCodename(), dv.getCodename());

        // application base is null after model mapping!
        assertNull(av.getApplicationBase());
    }

    @Test
    public void ShouldGetAppInstanceState() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(owner.getUsername());

        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        appInstance.setId(1L);
        appInstance.setInternalId(new Identifier(identifierValue));

        when(appDeploymentMonitor.state(any(Identifier.class))).thenReturn(AppLifecycleState.APPLICATION_DEPLOYED);
        when(appDeploymentMonitor.previousState(any(Identifier.class))).thenReturn(AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS);

        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());

        AppInstanceStatus ais = this.appInstanceController.getState(1L, principal);

        assertEquals(appInstance.getId(), ais.getAppInstanceId());
    }

    private final static String VALID_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDuQ6IUs8q207aA/q+KRswa+Ui+hx2c8yN/EoSIGCRhoadKkn1dN1GCGr6hn4te7BvWunGuRbLxtKf23IQvud3NuhWVrNCwJbHOIJ3To+45IBnGuur7u5CDBPR8tsvbkk4jde8j58K2xM+9GeGBxZhXEvgVs+uQwDqMhHeWCS9sqcf0Es0fXlQOffQCEiRnGOrd7cL1iIr7fimqGrGYmqxu3gfzhEPrMNHoXW5QArne48gK0EZvxmMoP5FWXLQx3itzDKfPaIB//uRBbBTNFUd6FWjZs2S1vsmKbV7LU0BBRu+CLfbw41eFuQUbx2/hQc+JbV0E5l31oCi04cZtfr1CKvmmA4t13UyooCPZWafS/uBi8n8eVoOT+VisEhbsFQJydulWeEeFF5bIwrMxPx4SucmvnsgZouemHSpuLvwIFanycPc6PWDL7gx6MLbLHulvNO22FVdRnuisgspGM85H1WFD51L5ARUz/bTltbYRKtcXhi3lYAETPmHjdiQCOp9pWNTTs+JHTz1mfA7LSVoceWO+5mdMEGwH3sEeZ/PgK6rUBocEV+xP7nj+i2L+KS/c+NvC49etjHiGCxUfXZozNSoma/tkSav2tvx10DWG8Yb93CAyqSyW1VdQIE/jE0PNWWwhvDzj1td4qsJw2+x8bCZVUChf50WxuEtBAFzVjw== user@vm1"; // user@vm1

    @Test
    public void shouldUpdateAppInstanceMembersList() {

        ModelMapper modelMapper = new ModelMapper();

        AppInstance appInstance = new AppInstance(application, name, domain1, owner);
        appInstance.setId(1L);
        appInstance.setInternalId(new Identifier(identifierValue));
        appInstance.setMembers(new HashSet<>());


        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());

        User user1domain1 = getUserWithSshKey("username1",  domain1);
        User user2domain1 = getUserWithSshKey("username2",  domain1);
        User user3domain2 = getUserWithSshKey("username3",  domain2);
        User user4domain1 = getUserWithSshKey("username4",  domain1);
        User user5domain1 = getUserWithSshKey("username5",  domain1);

        appInstance.getMembers().add(user4domain1);
        appInstance.getMembers().add(user5domain1);

        when(userService.findByUsername(user1domain1.getUsername())).thenReturn(Optional.of(user1domain1));
        when(userService.findByUsername(user2domain1.getUsername())).thenReturn(Optional.of(user2domain1));
        when(userService.findByUsername(user3domain2.getUsername())).thenReturn(Optional.of(user3domain2));

        List<User> users = new ArrayList<>();
        users.add(user1domain1);
        users.add(user2domain1);
        users.add(user3domain2);
        users.add(user5domain1);

        List<UserBase> members = users.stream().map(u -> modelMapper.map(u, UserBase.class)).collect(Collectors.toList());

        appInstanceController.updateMembers(appInstance.getId(), members);

        verify(applicationEventPublisher, times(2)).publishEvent(any(AddUserToRepositoryGitlabEvent.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(RemoveUserFromRepositoryGitlabEvent.class));
    }

    public static SSHKeyEntity getDefaultSSHKey(User owner) {
        return new SSHKeyEntity(owner, "default", VALID_KEY);
    }

    public static User getUserWithSshKey(String username, Domain domain) {
        User user = new User(username, true, "", domain,  Role.ROLE_GUEST);
        user.setSshKeys(new HashSet<>());
        user.getSshKeys().add(getDefaultSSHKey(user));
        return user;
    }
}
