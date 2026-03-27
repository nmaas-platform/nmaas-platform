package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.api.dto.applications.AppInstanceState;
import net.geant.nmaas.api.dto.applications.AppInstanceStatus;
import net.geant.nmaas.api.dto.applications.ApplicationStateChangeRequest;
import net.geant.nmaas.api.dto.applications.ApplicationStateDto;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.PortalException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.persistence.repositories.RatingRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationControllerTest {

    private final ModelMapper modelMapper = new ModelMapper();

    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final UserService userService = mock(UserService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final RatingRepository ratingRepository = mock(RatingRepository.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppInstanceController appInstanceController = mock(AppInstanceController.class);
    private final ApplicationSubscriptionService applicationSubscriptionService = mock(ApplicationSubscriptionService.class);

    private ApplicationController controller;

    @BeforeEach
    void setup() {
        controller = new ApplicationController(
                modelMapper,
                applicationService,
                applicationBaseService,
                userService,
                eventPublisher,
                ratingRepository,
                applicationInstanceService,
                appInstanceController,
                applicationSubscriptionService
        );
    }

    @Test
    void shouldReturnAllBasesForSystemAdmin() {
        Principal principal = principal("admin");
        when(userService.findByUsername("admin")).thenReturn(
                Optional.of(userWithRoles("admin", Role.ROLE_SYSTEM_ADMIN)));

        ApplicationBase base1 = new ApplicationBase(1L, "app-a");
        base1.setOwner("owner-a");
        ApplicationBase base2 = new ApplicationBase(2L, "app-b");
        base2.setOwner("owner-b");

        when(applicationBaseService.findAll()).thenReturn(List.of(base1, base2));
        when(ratingRepository.getApplicationRating(1L)).thenReturn(new Integer[]{4, 5});
        when(ratingRepository.getApplicationRating(2L)).thenReturn(new Integer[]{3});

        var result = controller.getAllApplicationBaseBasedOnRole(principal);

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnOnlyOwnedBasesForToolManager() {
        Principal principal = principal("tool-manager");
        when(userService.findByUsername("tool-manager"))
                .thenReturn(Optional.of(userWithRoles("tool-manager", Role.ROLE_TOOL_MANAGER)));

        ApplicationBase owned = new ApplicationBase(1L, "owned-app");
        owned.setOwner("tool-manager");
        ApplicationBase other = new ApplicationBase(2L, "other-app");
        other.setOwner("another-user");

        when(applicationBaseService.findAll()).thenReturn(List.of(owned, other));
        when(ratingRepository.getApplicationRating(1L)).thenReturn(new Integer[]{5});

        var result = controller.getAllApplicationBaseBasedOnRole(principal);

        assertEquals(1, result.size());
        assertEquals("owned-app", result.getFirst().getName());
    }

    @Test
    void shouldThrowWhenApplicationVersionNotFound() {
        ApplicationBase base = new ApplicationBase(10L, "my-app");
        when(applicationBaseService.findByName("my-app")).thenReturn(base);
        when(applicationService.findApplication("my-app", "9.9.9")).thenReturn(Optional.empty());

        assertThrows(MissingElementException.class,
                () -> controller.getApplicationByNameAndVersion("my-app", "9.9.9"));
    }

    @Test
    void shouldRejectDeletingStateWhenAnyInstanceIsRunning() {
        long appId = 100L;
        Application app = new Application(appId, "my-app", "1.0.0");
        app.setState(net.geant.nmaas.portal.persistence.entity.ApplicationState.ACTIVE);

        AppInstance instance = new AppInstance();
        instance.setId(44L);

        when(applicationService.findApplication(appId)).thenReturn(Optional.of(app));
        when(applicationInstanceService.findAllByApplication(app)).thenReturn(List.of(instance));
        when(appInstanceController.getState(eq(44L), any(Principal.class)))
                .thenReturn(new AppInstanceStatus(44L,
                        AppInstanceState.DEPLOYING,
                        AppInstanceState.REQUESTED,
                        null,
                        null,
                        null));

        ProcessingException ex = assertThrows(ProcessingException.class,
                () -> controller.changeApplicationState(
                        appId,
                        new ApplicationStateChangeRequest(ApplicationStateDto.DELETED,
                                "",
                                false),
                        principal("admin")
                ));

        assertEquals("Can't set state to DELETED. There is still 1 running instances of this version.",
                ex.getMessage());
        verify(applicationService, never()).changeApplicationState(any(), any());
    }

    @Test
    void shouldChangeStateToDeletedWhenNoRunningInstancesAndSendOwnerNotification() {
        long appId = 101L;
        Application app = new Application(appId, "my-app", "1.0.0");
        app.setState(net.geant.nmaas.portal.persistence.entity.ApplicationState.ACTIVE);

        AppInstance removedInstance = new AppInstance();
        removedInstance.setId(51L);
        AppInstance doneInstance = new AppInstance();
        doneInstance.setId(52L);

        ApplicationBase base = new ApplicationBase(1L, "my-app");
        base.setOwner("owner");
        User owner = new User("owner", true);

        when(applicationService.findApplication(appId)).thenReturn(Optional.of(app));
        when(applicationInstanceService.findAllByApplication(app)).thenReturn(List.of(removedInstance, doneInstance));
        when(appInstanceController.getState(eq(51L), any(Principal.class)))
                .thenReturn(
                        new AppInstanceStatus(51L,
                                AppInstanceState.REMOVED,
                                AppInstanceState.UNDEPLOYING,
                                null,
                                null,
                                null)
                );
        when(appInstanceController.getState(eq(52L), any(Principal.class)))
                .thenReturn(
                        new AppInstanceStatus(52L,
                                AppInstanceState.DONE,
                                AppInstanceState.DEPLOYING,
                                null,
                                null,
                                null));
        when(applicationBaseService.findByName("my-app")).thenReturn(base);
        when(userService.findByUsername("owner")).thenReturn(Optional.of(owner));

        assertDoesNotThrow(() -> controller.changeApplicationState(
                appId,
                new ApplicationStateChangeRequest(
                        ApplicationStateDto.DELETED, "reason", false),
                principal("admin")
        ));

        verify(applicationService).changeApplicationState(app,
                net.geant.nmaas.portal.persistence.entity.ApplicationState.DELETED);
        verify(applicationBaseService).updateApplicationVersionState("my-app",
                "1.0.0",
                net.geant.nmaas.portal.persistence.entity.ApplicationState.DELETED);

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(MailType.APP_DELETED, eventCaptor.getValue().getMailAttributes().getMailType());
        assertEquals(1, eventCaptor.getValue().getMailAttributes().getAddresses().size());
        assertEquals("owner",
                eventCaptor.getValue().getMailAttributes().getAddresses().getFirst().getUsername());
    }

    @Test
    void shouldSendBroadcastNotificationForActiveStateWhenRequested() {
        long appId = 102L;
        Application app = new Application(appId, "my-app", "1.0.0");
        app.setState(net.geant.nmaas.portal.persistence.entity.ApplicationState.NEW);

        User enabled = new User("enabled-user", true);
        User disabled = new User("disabled-user", false);

        when(applicationService.findApplication(appId)).thenReturn(Optional.of(app));
        when(userService.findAll()).thenReturn(List.of(enabled, disabled));

        controller.changeApplicationState(
                appId,
                new ApplicationStateChangeRequest(ApplicationStateDto.ACTIVE, "", true),
                principal("admin")
        );

        verify(applicationService).changeApplicationState(app,
                net.geant.nmaas.portal.persistence.entity.ApplicationState.ACTIVE);
        verify(applicationBaseService).updateApplicationVersionState("my-app",
                "1.0.0",
                net.geant.nmaas.portal.persistence.entity.ApplicationState.ACTIVE);

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(MailType.NEW_ACTIVE_APP, eventCaptor.getValue().getMailAttributes().getMailType());
        assertEquals(1, eventCaptor.getValue().getMailAttributes().getAddresses().size());
        assertEquals("enabled-user",
                eventCaptor.getValue().getMailAttributes().getAddresses().getFirst().getUsername());
    }

    @Test
    void shouldBlockDeleteApplicationWhenUserIsNotOwnerAndNotSystemAdmin() {
        long appId = 201L;
        Application app = new Application(appId, "my-app", "1.2.3");
        ApplicationBase base = new ApplicationBase(1L, "my-app");
        base.setOwner("owner");

        when(applicationService.findApplication(appId)).thenReturn(Optional.of(app));
        when(applicationBaseService.findByName("my-app")).thenReturn(base);
        when(userService.findByUsername("regular-user"))
                .thenReturn(Optional.of(userWithRoles("regular-user", Role.ROLE_TOOL_MANAGER)));

        assertThrows(PortalException.class,
                () -> controller.deleteApplication(appId, principal("regular-user")));

        verify(applicationService, never()).delete(any(Long.class));
    }

    private static Principal principal(String name) {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(name);
        return principal;
    }

    private static User userWithRoles(String username, Role role) {
        User user = new User(username, true);
        Domain domain = new Domain(1L, "GLOBAL", "GLOBAL");
        user.setRoles(List.of(new UserRole(user, domain, role)));
        return user;
    }
}
