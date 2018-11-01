package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableSet;
import net.geant.nmaas.portal.api.domain.PasswordChange;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.domain.UserRole;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.model.ConfirmationEmail;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsersControllerTest {

	private static final Domain GLOBAL_DOMAIN = new Domain(1L,"global", "global", true);

	private static final Domain DOMAIN = new Domain(2L,"testdom", "testdom", true);

	private UserService userService = mock(UserService.class);

	private DomainService domainService = mock(DomainService.class);

	private NotificationService notificationService = mock(NotificationService.class);

	private ModelMapper modelMapper = new ModelMapper();

	private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

	private UsersController usersController;

	private List<User> userList;

	private Principal principal = mock(Principal.class);

	@Before
	public void setup(){
		usersController = new UsersController(userService, domainService, notificationService, modelMapper, passwordEncoder);
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
	public void shouldReturnRoles(){
		List<Role> roles = usersController.getRoles();
		assertThat("Number of roles mismatch", roles.size() == 8);
	}

	@Test
	public void shouldRetrieveUser(){
		when(userService.findById(userList.get(0).getId())).thenReturn(Optional.of(userList.get(0)));
		UserRole userRole = modelMapper.map(userList.get(0).getRoles().get(0), UserRole.class);
		net.geant.nmaas.portal.api.domain.User user = usersController.retrieveUser(userList.get(0).getId());
		assertThat("Wrong username", user.getUsername().equals(userList.get(0).getUsername()));
		assertThat("Wrong role", user.getRoles().iterator().next().getRole().equals(userRole.getRole()));
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotRetrieveNonExistingUser(){
		Long userId = 5L;
		when(userService.findById(userId)).thenReturn(Optional.empty());
		usersController.retrieveUser(userId);
	}

	@Test
	public void shouldUpdateUser(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		userRequest.setEmail("test@nmaas.net");
		userRequest.setFirstname("test");
		usersController.updateUser(userList.get(0).getId(), userRequest, principal);
		verify(userService, times(2)).update(userList.get(0));
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotUpdateNonExistingUser(){
		Long userId = 5L;
		when(userService.findById(userId)).thenReturn(Optional.empty());
		UserRequest userRequest = new UserRequest(userId, "test", "pass");
		usersController.updateUser(userId, userRequest, principal);
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotUpdateWithNullId(){
		Long userId = null;
		UserRequest userRequest = new UserRequest(userId, userList.get(0).getUsername(), userList.get(0).getPassword());
		when(userService.findById(userId)).thenReturn(Optional.empty());
		usersController.updateUser(userId, userRequest, principal);
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotUpdateWithNullUserRequest(){
		Long userId = 1L;
		usersController.updateUser(userId, null, principal);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldNotUpdateWithSystemComponentRole(){
		when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		UserRole userRole = new UserRole();
		userRole.setDomainId(GLOBAL_DOMAIN.getId());
		userRole.setRole(Role.ROLE_SYSTEM_COMPONENT);;
		userRequest.setRoles(ImmutableSet.of(userRole));
		usersController.updateUser(userList.get(0).getId(), userRequest, principal);
	}

	@Test
	@Ignore
	public void shouldDeleteUser(){
		//Update test when user delete is supported
	}

	@Test
	public void shouldGetUserRoles(){
		Set<UserRole> result = usersController.getUserRoles(userList.get(0).getId());
		UserRole userRole = modelMapper.map(userList.get(0).getRoles().get(0), UserRole.class);
		assertThat("Wrong roles set", result.iterator().next().getRole().equals(userRole.getRole()));
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotGetUserRolesForNonExistingUser(){
		Long userId = 5L;
		when(userService.findById(userId)).thenReturn(Optional.empty());
		Set<UserRole> result = usersController.getUserRoles(userId);
	}

	@Test
	public void shouldRemoveUserRoleWithGlobalDomainAndAddGuestRole(){
		UserRole userRole = new UserRole();
		userRole.setRole(Role.ROLE_OPERATOR);
		usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).removeMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_OPERATOR);
		verify(domainService, times(1)).addMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_GUEST);
	}

	@Test
	public void shouldRemoveUserRoleWithNonGlobalDomain(){
		UserRole userRole = new UserRole();
		userRole.setRole(Role.ROLE_OPERATOR);
		userRole.setDomainId(DOMAIN.getId());
		usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).removeMemberRole(DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_OPERATOR);
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotRemoveUserRoleWhenUserIdIsNull(){
		Long userId = null;
		UserRole userRole = new UserRole();
		userRole.setRole(Role.ROLE_OPERATOR);
		when(userService.findById(userId)).thenReturn(Optional.empty());
		usersController.removeUserRole(userId, userRole, principal);
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotRemoveUserRoleWhenUserRoleIsNull(){
		UserRole userRole = null;
		usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotRemoveUserRoleWithoutDomain(){
		UserRole userRole = new UserRole();
		userRole.setRole(Role.ROLE_OPERATOR);
		when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
		usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
	}

	@Test
	public void shouldCompleteRegistration(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
		usersController.completeRegistration(principal, userRequest);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldCompleteRegistrationAndRemoveIncompleteRole(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		userRequest.setEmail("test@nmaas.net");
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		when(userService.findByUsername(userList.get(0).getUsername())).thenReturn(Optional.of(userList.get(0)));
		when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
		usersController.completeRegistration(principal, userRequest);
		verify(domainService, times(1)).removeMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_INCOMPLETE);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldChangePassword(){
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		when(userService.findByUsername(userList.get(0).getUsername())).thenReturn(Optional.of(userList.get(0)));
		PasswordChange passwordChange = new PasswordChange(userList.get(0).getPassword(), "test1234");
		when(passwordEncoder.matches(userList.get(0).getPassword(), passwordChange.getPassword())).thenReturn(true);
		usersController.changePassword(principal, passwordChange);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test(expected = ProcessingException.class)
	public void shouldNotChangePasswordOnPreviousPasswordMismatch(){
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		when(userService.findByUsername(userList.get(0).getUsername())).thenReturn(Optional.of(userList.get(0)));
		PasswordChange passwordChange = new PasswordChange("wrongpass", "test1234");
		when(passwordEncoder.matches(userList.get(0).getPassword(), passwordChange.getPassword())).thenReturn(false);
		usersController.changePassword(principal, passwordChange);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldGetDomainUsers(){
		Long domainId = 1L;
		when(domainService.getMembers(domainId)).thenReturn(userList);
		List<net.geant.nmaas.portal.api.domain.User> users = usersController.getDomainUsers(domainId);
		assertThat("List size mismatch", users.size() == userList.size());
	}

	@Test
	public void shouldGetDomainUser(){
		Long domainId = 1L;
		Long userId = 1L;
		when(domainService.getMember(domainId, userId)).thenReturn(userList.get(0));
		net.geant.nmaas.portal.api.domain.User user = usersController.getDomainUser(domainId, userId);
		assertThat("User mismatch", user.getUsername().equals(userList.get(0).getUsername()));
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotGetDomainUserWhenDomainNotExists(){
		Long domainId = 5L;
		Long userId = 1L;
		when(domainService.getMember(domainId, userId)).thenThrow(ObjectNotFoundException.class);
		net.geant.nmaas.portal.api.domain.User user = usersController.getDomainUser(domainId, userId);
	}

	@Test(expected = ProcessingException.class)
	public void shouldNotGetDomainUserWhenUserNotExist(){
		Long domainId = 1L;
		Long userId = 8L;
		when(domainService.getMember(domainId, userId)).thenThrow(net.geant.nmaas.portal.exceptions.ProcessingException.class);
		net.geant.nmaas.portal.api.domain.User user = usersController.getDomainUser(domainId, userId);
	}

	@Test
	public void shouldRemoveDomainUser(){
		usersController.removeDomainUser(DOMAIN.getId(), userList.get(0).getId());
		verify(domainService, times(1)).removeMember(DOMAIN.getId(), userList.get(0).getId());
	}

	@Test
	public void shouldGetUserDomainRoles(){
		Set<Role> roles = usersController.getUserRoles(DOMAIN.getId(), userList.get(0).getId());
		verify(domainService, times(1)).getMemberRoles(DOMAIN.getId(), userList.get(0).getId());
	}

	@Test
	public void shouldAddUserRoleToCustomDomain(){
		UserRole userRole = new UserRole();
		userRole.setDomainId(DOMAIN.getId());
		userRole.setRole(Role.ROLE_USER);
		usersController.addUserRole(DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).addMemberRole(DOMAIN.getId(), userList.get(0).getId(), userRole.getRole());
	}

	@Test
	public void shouldAddUserRoleToGlobalDomain(){
		UserRole userRole = new UserRole();
		userRole.setDomainId(GLOBAL_DOMAIN.getId());
		userRole.setRole(Role.ROLE_OPERATOR);
		when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
		usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).addMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole.getRole());
	}

	@Test(expected = ProcessingException.class)
	public void shouldNotAddNonGlobalRoleToGlobalDomain(){
		UserRole userRole = new UserRole();
		userRole.setDomainId(GLOBAL_DOMAIN.getId());
		userRole.setRole(Role.ROLE_DOMAIN_ADMIN);
		when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
		usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
	}

	@Test(expected = ProcessingException.class)
	public void shouldNotAddGlobalRoleToCustomDomain(){
		UserRole userRole = new UserRole();
		userRole.setDomainId(DOMAIN.getId());
		userRole.setRole(Role.ROLE_SYSTEM_ADMIN);
		usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
	}

	@Test(expected = ProcessingException.class)
	public void shouldNotAddSystemComponentRoleToUser(){
		UserRole userRole = new UserRole();
		userRole.setDomainId(GLOBAL_DOMAIN.getId());
		userRole.setRole(Role.ROLE_SYSTEM_COMPONENT);
		usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
	}

	@Test
	public void shouldRemoveUserRole(){
		String userRole = "ROLE_SYSTEM_ADMIN";
		usersController.removeUserRole(DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).removeMemberRole(DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_SYSTEM_ADMIN);
	}

	@Test(expected = MissingElementException.class)
	public void shouldNotConvertIncorrectStringWhenRemovingUserRole(){
		String userRole = "ROLE_WRONG";
		usersController.removeUserRole(DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
	}

	@Test
	public void shouldSetEnabledFlag(){
		usersController.setEnabledFlag(userList.get(0).getId(), true, principal);
		verify(userService, times(1)).setEnabledFlag(userList.get(0).getId(), true);
	}

}
