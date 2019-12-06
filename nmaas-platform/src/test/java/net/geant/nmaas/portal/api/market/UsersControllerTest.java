package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableSet;
import net.geant.nmaas.portal.api.domain.PasswordChange;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.domain.UserRoleView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsersControllerTest {

	private static final Domain GLOBAL_DOMAIN = new Domain(1L,"global", "global", true);

	private static final Domain DOMAIN = new Domain(2L,"testdom", "testdom", true);

	private UserService userService = mock(UserService.class);

	private DomainService domainService = mock(DomainService.class);

	private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

	private ModelMapper modelMapper = new ModelMapper();

	private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

	private UsersController usersController;

	private List<User> userList;

	private Principal principal = mock(Principal.class);

	private JWTTokenService jwtTokenService = mock(JWTTokenService.class);

	@BeforeEach
	public void setup(){
		usersController = new UsersController(userService, domainService, modelMapper, passwordEncoder, jwtTokenService, eventPublisher);
		User tester = new User("tester", true, "test123", DOMAIN, Role.ROLE_USER);
		tester.setId(1L);
		User admin = new User("testadmin", true, "testadmin123", DOMAIN, Role.ROLE_SYSTEM_ADMIN);
		admin.setId(2L);
		userList = Arrays.asList(tester, admin);

		when(principal.getName()).thenReturn(admin.getUsername());
		when(userService.findById(userList.get(0).getId())).thenReturn(Optional.of(userList.get(0)));
		when(userService.findByUsername(userList.get(1).getUsername())).thenReturn(Optional.of(userList.get(1)));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(GLOBAL_DOMAIN));
		when(domainService.findDomain(DOMAIN.getId())).thenReturn(Optional.of(DOMAIN));
		when(userService.findAllUsersWithAdminRole()).thenReturn(new ArrayList<UserView>() {{
			add(new UserView(2L, "admin"));
		}});
	}

	@Test
	public void shouldReturnRoles(){
		List<Role> roles = usersController.getRoles();
		assertThat("Number of roles mismatch", roles.size() == 8);
	}

	@Test
	public void shouldRetrieveUser(){
		when(userService.findById(userList.get(0).getId())).thenReturn(Optional.of(userList.get(0)));
		UserRoleView userRole = modelMapper.map(userList.get(0).getRoles().get(0), UserRoleView.class);
		UserView user = usersController.retrieveUser(userList.get(0).getId());
		assertThat("Wrong username", user.getUsername().equals(userList.get(0).getUsername()));
		assertThat("Wrong role", user.getRoles().iterator().next().getRole().equals(userRole.getRole()));
	}

	@Test
	public void shouldNotRetrieveNonExistingUser(){
		assertThrows(MissingElementException.class, () -> {
			Long userId = 5L;
			when(userService.findById(userId)).thenReturn(Optional.empty());
			usersController.retrieveUser(userId);
		});
	}

	@Test
	public void shouldUpdateUser(){
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		userRequest.setEmail("test@nmaas.net");
		userRequest.setFirstname("test");
		usersController.updateUser(userList.get(0).getId(), userRequest, principal);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldNotUpdateNonExistingUser(){
		assertThrows(MissingElementException.class, () -> {
			Long userId = 5L;
			when(userService.findById(userId)).thenReturn(Optional.empty());
			UserRequest userRequest = new UserRequest(userId, "test", "pass");
			usersController.updateUser(userId, userRequest, principal);
		});
	}

	@Test
	public void shouldNotUpdateWithNullId(){
		assertThrows(MissingElementException.class, () -> {
			Long userId = null;
			UserRequest userRequest = new UserRequest(userId, userList.get(0).getUsername(), userList.get(0).getPassword());
			when(userService.findById(userId)).thenReturn(Optional.empty());
			usersController.updateUser(userId, userRequest, principal);
		});
	}

	@Test
	public void shouldNotUpdateWithNullUserRequest(){
		assertThrows(MissingElementException.class, () -> {
			Long userId = 1L;
			usersController.updateUser(userId, null, principal);
		});
	}

	@Test
	public void shouldNotUpdateUserWithNonUniqueEmail(){
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		assertThrows(ProcessingException.class, () -> {
			when(userService.existsByEmail(anyString())).thenReturn(true);
			UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
			userRequest.setEmail("test@nmaas.net");
			userRequest.setFirstname("test");
			usersController.updateUser(userList.get(0).getId(), userRequest, principal);
			verify(userService, times(2)).update(userList.get(0));
		});
	}

	@Test
	public void shouldUpdateUserWithNullEmail(){
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		userRequest.setEmail(null);
		userRequest.setFirstname("test");
		usersController.updateUser(userList.get(0).getId(), userRequest, principal);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldGetUserRoles(){
		Set<UserRoleView> result = usersController.getUserRoles(userList.get(0).getId());
		UserRoleView userRole = modelMapper.map(userList.get(0).getRoles().get(0), UserRoleView.class);
		assertThat("Wrong roles set", result.iterator().next().getRole().equals(userRole.getRole()));
	}

	@Test
	public void shouldNotGetUserRolesForNonExistingUser(){
		assertThrows(MissingElementException.class, () -> {
			Long userId = 5L;
			when(userService.findById(userId)).thenReturn(Optional.empty());
			Set<UserRoleView> result = usersController.getUserRoles(userId);
		});
	}

	@Test
	public void shouldRemoveUserRoleWithGlobalDomainAndAddGuestRole(){
		UserRoleView userRole = new UserRoleView();
		userRole.setRole(Role.ROLE_OPERATOR);
		usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).removeMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_OPERATOR);
		verify(domainService, times(1)).addGlobalGuestUserRoleIfMissing(userList.get(0).getId());
	}

	@Test
	public void shouldRemoveUserRoleWithNonGlobalDomain(){
		UserRoleView userRole = new UserRoleView();
		userRole.setRole(Role.ROLE_OPERATOR);
		userRole.setDomainId(DOMAIN.getId());
		usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).removeMemberRole(DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_OPERATOR);
	}

	@Test
	public void shouldNotRemoveUserRoleWithNullRequest(){
		assertThrows(MissingElementException.class, () -> {
			usersController.removeUserRole(userList.get(0).getId(), null, principal);
		});
	}

	@Test
	public void shouldNotRemoveUserRoleWhenUserIdIsNull(){
		assertThrows(MissingElementException.class, () -> {
			Long userId = null;
			UserRoleView userRole = new UserRoleView();
			userRole.setRole(Role.ROLE_OPERATOR);
			when(userService.findById(userId)).thenReturn(Optional.empty());
			usersController.removeUserRole(userId, userRole, principal);
		});
	}

	@Test
	public void shouldNotRemoveUserRoleWhenUserRoleIsNull(){
		assertThrows(MissingElementException.class, () -> {
			UserRoleView userRole = null;
			usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
		});
	}

	@Test
	public void shouldNotRemoveUserRoleWithoutDomain(){
		assertThrows(MissingElementException.class, () -> {
			UserRoleView userRole = new UserRoleView();
			userRole.setRole(Role.ROLE_OPERATOR);
			when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
			usersController.removeUserRole(userList.get(0).getId(), userRole, principal);
		});
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

	@Test
	public void shouldNotChangePasswordOnPreviousPasswordMismatch(){
		assertThrows(ProcessingException.class, () -> {
			when(principal.getName()).thenReturn(userList.get(0).getUsername());
			when(userService.findByUsername(userList.get(0).getUsername())).thenReturn(Optional.of(userList.get(0)));
			PasswordChange passwordChange = new PasswordChange("wrongpass", "test1234");
			when(passwordEncoder.matches(userList.get(0).getPassword(), passwordChange.getPassword())).thenReturn(false);
			usersController.changePassword(principal, passwordChange);
			verify(userService, times(1)).update(userList.get(0));
		});
	}

	@Test
	public void shouldGetDomainUsers(){
		Long domainId = 1L;
		when(domainService.getMembers(domainId)).thenReturn(userList);
		List<UserView> users = usersController.getDomainUsers(domainId);
		assertThat("List size mismatch", users.size() == userList.size());
	}

	@Test
	public void shouldGetDomainUser(){
		Long domainId = 1L;
		Long userId = 1L;
		when(domainService.getMember(domainId, userId)).thenReturn(userList.get(0));
		UserView user = usersController.getDomainUser(domainId, userId);
		assertThat("User mismatch", user.getUsername().equals(userList.get(0).getUsername()));
	}

	@Test
	public void shouldNotGetDomainUserWhenDomainNotExists(){
		assertThrows(MissingElementException.class, () -> {
			Long domainId = 5L;
			Long userId = 1L;
			when(domainService.getMember(domainId, userId)).thenThrow(ObjectNotFoundException.class);
			UserView user = usersController.getDomainUser(domainId, userId);
		});
	}

	@Test
	public void shouldNotGetDomainUserWhenUserNotExist(){
		assertThrows(ProcessingException.class, () -> {
			Long domainId = 1L;
			Long userId = 8L;
			when(domainService.getMember(domainId, userId)).thenThrow(ProcessingException.class);
			UserView user = usersController.getDomainUser(domainId, userId);
		});
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
		UserRoleView userRole = new UserRoleView();
		userRole.setDomainId(DOMAIN.getId());
		userRole.setRole(Role.ROLE_USER);
		usersController.addUserRole(DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).addMemberRole(DOMAIN.getId(), userList.get(0).getId(), userRole.getRole());
	}

	@Test
	public void shouldAddUserRoleToGlobalDomain(){
		UserRoleView userRole = new UserRoleView();
		userRole.setDomainId(GLOBAL_DOMAIN.getId());
		userRole.setRole(Role.ROLE_OPERATOR);
		when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
		usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).addMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole.getRole());
	}

	@Test
	public void shouldNotAddUserRoleWithNullRole(){
		assertThrows(MissingElementException.class, () -> {
			usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), null, principal);
		});
	}

	@Test
	public void shouldNotAddUserRoleWithNullUserRole(){
		assertThrows(MissingElementException.class, () -> {
			UserRoleView userRole = new UserRoleView();
			userRole.setRole(null);
			usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		});
	}

	@Test
	public void shouldNotAddGlobalUserRoleInNotGlobalDomain(){
		assertThrows(ProcessingException.class, () -> {
			UserRoleView userRole = new UserRoleView();
			userRole.setDomainId(DOMAIN.getId());
			userRole.setRole(Role.ROLE_OPERATOR);
			when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
			usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		});
	}

	@Test
	public void shouldNotAddNonGlobalRoleToGlobalDomain(){
		assertThrows(ProcessingException.class, () -> {
			UserRoleView userRole = new UserRoleView();
			userRole.setDomainId(GLOBAL_DOMAIN.getId());
			userRole.setRole(Role.ROLE_DOMAIN_ADMIN);
			when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
			usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		});
	}

	@Test
	public void shouldNotAddGlobalRoleToCustomDomain(){
		assertThrows(ProcessingException.class, () -> {
			UserRoleView userRole = new UserRoleView();
			userRole.setDomainId(DOMAIN.getId());
			userRole.setRole(Role.ROLE_SYSTEM_ADMIN);
			usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		});
	}

	@Test
	public void shouldRemoveUserRole(){
		String userRole = "ROLE_SYSTEM_ADMIN";
		usersController.removeUserRole(DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		verify(domainService, times(1)).removeMemberRole(DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_SYSTEM_ADMIN);
	}

	@Test
	public void shouldNotConvertIncorrectStringWhenRemovingUserRole(){
		assertThrows(MissingElementException.class, () -> {
			String userRole = "ROLE_WRONG";
			usersController.removeUserRole(DOMAIN.getId(), userList.get(0).getId(), userRole, principal);
		});
	}

	@Test
	public void shouldSetEnabledFlag(){
		usersController.setEnabledFlag(userList.get(0).getId(), true, principal);
		verify(userService, times(1)).setEnabledFlag(userList.get(0).getId(), true);
		verify(eventPublisher, times(1)).publishEvent(any());
	}

	@Test
	public void shouldSetDisabledFlag(){
		usersController.setEnabledFlag(userList.get(0).getId(), false, principal);
		verify(userService, times(1)).setEnabledFlag(userList.get(0).getId(), false);
		verify(eventPublisher, times(1)).publishEvent(any());
	}

	@Test
	public void shouldCompleteRegistration(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
		usersController.completeRegistration(principal, userRequest);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldCompleteRegistrationAndSendEmail(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
		usersController.completeRegistration(principal, userRequest);
		verify(userService, times(1)).update(userList.get(0));
		verify(eventPublisher, times(1)).publishEvent(any());
	}

	@Test
	public void shouldCompleteRegistrationWithFullData(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		userRequest.setFirstname("First");
		userRequest.setLastname("Last");
		when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
		usersController.completeRegistration(principal, userRequest);
		verify(userService, times(1)).update(userList.get(0));
	}

	@Test
	public void shouldNotCompleteRegistrationWithNonUniqueUsername(){
		assertThrows(ProcessingException.class, () -> {
			UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
			when(userService.existsByUsername(userRequest.getUsername())).thenReturn(true);
			usersController.completeRegistration(principal, userRequest);
		});
	}

	@Test
	public void shouldNotCompleteRegistrationWithNonUniqueMail(){
		assertThrows(ProcessingException.class, () -> {
			UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
			userRequest.setEmail("test@test.com");
			when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
			when(userService.existsByEmail(userRequest.getEmail())).thenReturn(true);
			usersController.completeRegistration(principal, userRequest);
		});
	}

	@Test
	public void shouldCompleteRegistrationAndRemoveIncompleteRole(){
		UserRequest userRequest = new UserRequest(userList.get(0).getId(), userList.get(0).getUsername(), userList.get(0).getPassword());
		userRequest.setEmail("test@nmaas.net");
		when(principal.getName()).thenReturn(userList.get(0).getUsername());
		when(userService.findByUsername(userList.get(0).getUsername())).thenReturn(Optional.of(userList.get(0)));
		when(userService.existsByUsername(userRequest.getUsername())).thenReturn(false);
		when(domainService.getMemberRoles(GLOBAL_DOMAIN.getId(), userRequest.getId())).thenReturn(ImmutableSet.of(Role.ROLE_GUEST));
		usersController.completeRegistration(principal, userRequest);
		verify(domainService, times(1)).addMemberRole(GLOBAL_DOMAIN.getId(), userList.get(0).getId(), Role.ROLE_GUEST);
		verify(userService, times(1)).update(userList.get(0));
	}

}
