package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableSet;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.model.ConfirmationEmail;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

public class RegistrationControllerTest {

    private UserService userService = mock(UserService.class);

    private DomainService domainService = mock(DomainService.class);

    private ModelMapper modelMapper = new ModelMapper();

    private NotificationService notificationService = mock(NotificationService.class);

    private List<User> userList;

    private RegistrationController registrationController;

    private Principal principal = mock(Principal.class);

    private static final Domain GLOBAL_DOMAIN = new Domain(1L,"global", "global", true);

    private static final Domain DOMAIN = new Domain(2L,"testdom", "testdom", true);

    @Before
    public void setup(){
        registrationController = new RegistrationController(userService, domainService, modelMapper, notificationService);
        User tester = new User("tester", true, "test123", DOMAIN, Role.ROLE_USER);
        tester.setId(1L);
        User admin = new User("testadmin", true, "testadmin123", DOMAIN, Role.ROLE_SYSTEM_ADMIN);
        admin.setId(2L);
        userList = Arrays.asList(tester, admin);

        when(principal.getName()).thenReturn(admin.getUsername());
        when(userService.findById(userList.get(0).getId())).thenReturn(Optional.of(userList.get(0)));
        when(userService.findByUsername(userList.get(1).getUsername())).thenReturn(Optional.of(userList.get(1)));
        doNothing().when(notificationService).sendEmail(any(ConfirmationEmail.class));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(GLOBAL_DOMAIN));
        when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
    }

    @Test
    public void shouldCompleteRegistration(){
        UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
        when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
        registrationController.completeRegistration(principal, userRequest);
        verify(userService, times(1)).update(userList.get(0));
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotCompleteRegistrationWithNonUniqueMail(){
        UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
        userRequest.setEmail("test@test.com");
        when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(userService.existsByEmail(userRequest.getEmail())).thenReturn(true);
        registrationController.completeRegistration(principal, userRequest);
    }

    @Test
    public void shouldCompleteRegistrationAndRemoveIncompleteRole(){
        UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
        userRequest.setEmail("test@nmaas.net");
        when(principal.getName()).thenReturn(userList.get(0).getUsername());
        when(userService.findByUsername(userList.get(0).getUsername())).thenReturn(Optional.of(userList.get(0)));
        when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(domainService.getMemberRoles(GLOBAL_DOMAIN.getId(), userRequest.getId())).thenReturn(ImmutableSet.of(Role.ROLE_GUEST));
        registrationController.completeRegistration(principal, userRequest);
        verify(domainService, times(1)).addMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_GUEST);
        verify(userService, times(1)).update(userList.get(0));
    }

}
