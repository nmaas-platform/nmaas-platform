package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.api.dto.applications.AppInstanceRequest;
import net.geant.nmaas.api.dto.users.UserBase;
import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppInstanceControllerTest {

    private static final String NAME = "app1";
    private static final String IDENTIFIER_VALUE = "id12";
    private static final String VALID_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDuQ6IUs8q207aA/q+KRswa+Ui+hx2c8yN/EoSIGCRhoadKkn1dN1GCGr6hn4te7BvWunGuRbLxtKf23IQvud3NuhWVrNCwJbHOIJ3To+45IBnGuur7u5CDBPR8tsvbkk4jde8j58K2xM+9GeGBxZhXEvgVs+uQwDqMhHeWCS9sqcf0Es0fXlQOffQCEiRnGOrd7cL1iIr7fimqGrGYmqxu3gfzhEPrMNHoXW5QArne48gK0EZvxmMoP5FWXLQx3itzDKfPaIB//uRBbBTNFUd6FWjZs2S1vsmKbV7LU0BBRu+CLfbw41eFuQUbx2/hQc+JbV0E5l31oCi04cZtfr1CKvmmA4t13UyooCPZWafS/uBi8n8eVoOT+VisEhbsFQJydulWeEeFF5bIwrMxPx4SucmvnsgZouemHSpuLvwIFanycPc6PWDL7gx6MLbLHulvNO22FVdRnuisgspGM85H1WFD51L5ARUz/bTltbYRKtcXhi3lYAETPmHjdiQCOp9pWNTTs+JHTz1mfA7LSVoceWO+5mdMEGwH3sEeZ/PgK6rUBocEV+xP7nj+i2L+KS/c+NvC49etjHiGCxUfXZozNSoma/tkSav2tvx10DWG8Yb93CAyqSyW1VdQIE/jE0PNWWwhvDzj1td4qsJw2+x8bCZVUChf50WxuEtBAFzVjw== user@vm1";

    private final AppLifecycleManager appLifecycleManager = mock(AppLifecycleManager.class);
    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final UserService userService = mock(UserService.class);
    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    private AppInstanceController appInstanceController;

    private Application application;
    private Domain domain1;
    private Domain domain2;
    private User owner;

    @BeforeEach
    void setup() {
        ApplicationBase appBase = new ApplicationBase();
        appBase.setId(1L);
        owner = new User("owner");
        owner.setId(2L);
        User admin = new User("admin");
        admin.setId(1L);
        domain1 = new Domain(2L, "domain one", "dom-1");
        domain2 = new Domain(3L, "domain two", "dom-2");
        application = new Application(NAME, "1.0");
        application.setId(1L);
        application.setState(ApplicationState.ACTIVE);

        when(userService.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        when(userService.findByUsername(owner.getUsername())).thenReturn(Optional.of(owner));
        when(userService.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userService.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(applicationBaseService.findByVersionId(1L)).thenReturn(appBase);
        when(domainService.findDomain(domain1.getId())).thenReturn(Optional.of(domain1));

        appInstanceController = new AppInstanceController(
                new ModelMapper(),
                applicationService,
                applicationBaseService,
                userService,
                appLifecycleManager,
                appDeploymentMonitor,
                applicationInstanceService,
                domainService,
                applicationEventPublisher,
                configurationManager
        );
    }

    @Test
    void shouldUpdateAppInstanceMembersList() {
        ModelMapper modelMapper = new ModelMapper();

        AppInstance appInstance = new AppInstance(application, NAME, domain1, owner, false);
        appInstance.setId(1L);
        appInstance.setInternalId(new Identifier(IDENTIFIER_VALUE));
        appInstance.setMembers(new HashSet<>());

        when(applicationInstanceService.find(1L)).thenReturn(Optional.of(appInstance));
        when(applicationInstanceService.find(-1L)).thenReturn(Optional.empty());

        User user1domain1 = getUserWithSshKey("username1", domain1);
        User user2domain1 = getUserWithSshKey("username2", domain1);
        User user3domain2 = getUserWithSshKey("username3", domain2);
        User user4domain1 = getUserWithSshKey("username4", domain1);
        User user5domain1 = getUserWithSshKey("username5", domain1);

        appInstance.getMembers().add(user4domain1);
        appInstance.getMembers().add(user5domain1);

        when(userService.findByUsername(user1domain1.getUsername())).thenReturn(Optional.of(user1domain1));
        when(userService.findByUsername(user2domain1.getUsername())).thenReturn(Optional.of(user2domain1));
        when(userService.findByUsername(user3domain2.getUsername())).thenReturn(Optional.of(user3domain2));

        List<User> users = List.of(user1domain1, user2domain1, user3domain2, user5domain1);
        List<UserBase> members = users.stream().map(u -> modelMapper.map(u, UserBase.class)).toList();

        appInstanceController.updateMembers(appInstance.getId(), members);

        verify(applicationEventPublisher, times(2)).publishEvent(any(AddUserToRepositoryGitlabEvent.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(RemoveUserFromRepositoryGitlabEvent.class));
    }

    @Test
    void shouldCreateAppInstanceThrowExceptionIfNameOfNewInstanceAlreadyExists() {
        Principal principal = mock(Principal.class);
        AppInstanceRequest appInstanceRequest = new AppInstanceRequest(1L, "instancename", false);
        Long domainId = 2L;
        AppInstance appInstance = new AppInstance(application, "InstanceName", domain1, owner, false);

        when(applicationInstanceService.findAllByDomain(domain1)).thenReturn(List.of(appInstance));
        when(domainService.findDomain(domainId)).thenReturn(Optional.of(domain1));
        when(appDeploymentMonitor.state(appInstance.getInternalId())).thenReturn(AppLifecycleState.UNKNOWN);
        when(applicationService.findApplication(appInstanceRequest.applicationId())).thenReturn(Optional.of(application));

        assertThrows(IllegalArgumentException.class, () ->
                appInstanceController.createAppInstance(appInstanceRequest, principal, domainId, null)
        );
    }

    public static SSHKeyEntity getDefaultSSHKey(User owner) {
        return new SSHKeyEntity(owner, "default", VALID_KEY);
    }

    public static User getUserWithSshKey(String username, Domain domain) {
        User user = new User(username, true, "", domain, Role.ROLE_GUEST);
        user.setSshKeys(new HashSet<>());
        user.getSshKeys().add(getDefaultSSHKey(user));
        return user;
    }
}
