package net.geant.nmaas.portal.api.user;

import net.geant.nmaas.api.dto.PasswordChangeRequest;
import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.UserDto;
import net.geant.nmaas.api.dto.users.UserRequest;
import net.geant.nmaas.api.dto.users.UserRoleDto;
import net.geant.nmaas.api.dto.users.UserInfoDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserEntryListRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UsersControllerTest {

    private static final Domain GLOBAL_DOMAIN = new Domain(1L, "global", "global", true);

    private static final Domain DOMAIN = new Domain(2L, "testdom", "testdom", true);

    private final UserService userService = mock(UserService.class);

    private final DomainService domainService = mock(DomainService.class);

    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final ModelMapper modelMapper = new ModelMapper();

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private UsersController usersController;

    private List<User> userList;

    private final Principal principal = mock(Principal.class);

    private final JWTTokenService jwtTokenService = mock(JWTTokenService.class);

    private final UserLoginRegisterService userLoginService = mock(UserLoginRegisterService.class);

    private final ApplicationInstanceService instanceService = mock(ApplicationInstanceService.class);

    private final UserEntryListRepository userEntryListRepository = mock(UserEntryListRepository.class);

    @BeforeEach
    void setup() {
        usersController = new UsersController(userService, domainService, modelMapper, passwordEncoder, jwtTokenService, eventPublisher, userLoginService, instanceService, userEntryListRepository);
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
        when(userService.findAllUsersWithAdminRole()).thenReturn(new ArrayList<UserDto>() {{
            add(new UserDto(2L, "admin", true));
        }});
    }

    @Test
    void shouldReturnRoles() {
        List<Role> roles = usersController.getRoles();
        assertThat("Number of roles mismatch", roles.size() == 10);
    }

    @Test
    void shouldGetUser() {
        when(userService.findById(userList.getFirst().getId())).thenReturn(Optional.of(userList.getFirst()));
        when(userLoginService.getUserFirstAndLastSuccessfulLoginDate(userList.getFirst())).thenReturn(Optional.empty());
        UserRoleDto userRole = modelMapper.map(userList.getFirst().getRoles().getFirst(), UserRoleDto.class);
        UserDto user = (UserDto) usersController.getUser(userList.getFirst().getId(), principal);
        assertThat("Wrong username", user.getUsername().equals(userList.getFirst().getUsername()));
        assertThat("Wrong role", user.getRoles().iterator().next().getRole().equals(userRole.getRole()));
    }

    @Test
    void shouldNotRetrieveNonExistingUser() {
        assertThrows(MissingElementException.class, () -> {
            Long userId = 5L;
            when(userService.findById(userId)).thenReturn(Optional.empty());
            usersController.getUser(userId, principal);
        });
    }

    @Test
    void shouldUpdateUser() {
        when(principal.getName()).thenReturn(userList.getFirst().getUsername());
        UserRequest userRequest = new UserRequest(userList.getFirst().getId(), userList.getFirst().getUsername(), userList.getFirst().getPassword());
        userRequest.setEmail("test@nmaas.net");
        userRequest.setFirstname("test");
        usersController.updateUser(userList.getFirst().getId(), userRequest, principal);
        verify(userService, times(1)).update(userList.getFirst());
    }

    @Test
    void shouldNotUpdateNonExistingUser() {
        assertThrows(MissingElementException.class, () -> {
            Long userId = 5L;
            when(userService.findById(userId)).thenReturn(Optional.empty());
            UserRequest userRequest = new UserRequest(userId, "test", "pass");
            usersController.updateUser(userId, userRequest, principal);
        });
    }

    @Test
    void shouldNotUpdateWithNullId() {
        assertThrows(MissingElementException.class, () -> {
            Long userId = null;
            UserRequest userRequest = new UserRequest(userId, userList.getFirst().getUsername(), userList.getFirst().getPassword());
            when(userService.findById(userId)).thenReturn(Optional.empty());
            usersController.updateUser(userId, userRequest, principal);
        });
    }

    @Test
    void shouldNotUpdateWithNullUserRequest() {
        assertThrows(MissingElementException.class, () -> {
            Long userId = 1L;
            usersController.updateUser(userId, null, principal);
        });
    }

    @Test
    void shouldNotUpdateUserWithNonUniqueEmail() {
        when(principal.getName()).thenReturn(userList.getFirst().getUsername());
        assertThrows(ProcessingException.class, () -> {
            when(userService.existsByEmail(anyString())).thenReturn(true);
            UserRequest userRequest = new UserRequest(userList.getFirst().getId(), userList.getFirst().getUsername(), userList.get(0).getPassword());
            userRequest.setEmail("test@nmaas.net");
            userRequest.setFirstname("test");
            usersController.updateUser(userList.getFirst().getId(), userRequest, principal);
            verify(userService, times(2)).update(userList.getFirst());
        });
    }

    @Test
    void shouldUpdateUserWithNullEmail() {
        when(principal.getName()).thenReturn(userList.getFirst().getUsername());
        UserRequest userRequest = new UserRequest(userList.getFirst().getId(), userList.getFirst().getUsername(), userList.getFirst().getPassword());
        userRequest.setEmail(null);
        userRequest.setFirstname("test");
        usersController.updateUser(userList.getFirst().getId(), userRequest, principal);
        verify(userService, times(1)).update(userList.getFirst());
    }

    @Test
    void shouldGetUserRoles() {
        Set<UserRoleDto> result = usersController.getUserRoles(userList.getFirst().getId());
        UserRoleDto userRole = modelMapper.map(userList.getFirst().getRoles().getFirst(), UserRoleDto.class);
        assertThat("Wrong roles set", result.iterator().next().getRole().equals(userRole.getRole()));
    }

    @Test
    void shouldNotGetUserRolesForNonExistingUser() {
        assertThrows(MissingElementException.class, () -> {
            Long userId = 5L;
            when(userService.findById(userId)).thenReturn(Optional.empty());
            Set<UserRoleDto> result = usersController.getUserRoles(userId);
        });
    }

    @Test
    void shouldRemoveUserRoleWithGlobalDomainAndAddGuestRole() {
        UserRoleDto userRole = new UserRoleDto();
        userRole.setRole(RoleDto.ROLE_OPERATOR);
        usersController.removeUserRole(userList.getFirst().getId(), userRole, principal);
        verify(domainService, times(1)).removeMemberRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), Role.ROLE_OPERATOR);
        verify(domainService, times(1)).addGlobalGuestUserRoleIfMissing(userList.getFirst().getId());
    }

    @Test
    void shouldRemoveUserRoleWithNonGlobalDomain() {
        UserRoleDto userRole = new UserRoleDto();
        userRole.setRole(RoleDto.ROLE_OPERATOR);
        userRole.setDomainId(DOMAIN.getId());
        usersController.removeUserRole(userList.getFirst().getId(), userRole, principal);
        verify(domainService, times(1)).removeMemberRole(DOMAIN.getId(), userList.getFirst().getId(), Role.ROLE_OPERATOR);
    }

    @Test
    void shouldNotRemoveUserRoleWithNullRequest() {
        MissingElementException me = assertThrows(MissingElementException.class, () -> {
            usersController.removeUserRole(userList.getFirst().getId(), null, principal);
        });

        assertEquals("userRole is null", me.getMessage());
    }

    @Test
    void shouldNotRemoveUserRoleWithNullUserRole() {
        UserRoleDto ur = new UserRoleDto();
        ur.setRole(null);
        MissingElementException me = assertThrows(MissingElementException.class, () -> {
            usersController.removeUserRole(userList.getFirst().getId(), ur, principal);
        });

        assertEquals("Missing role", me.getMessage());
    }

    @Test
    void shouldNotRemoveUserRoleWhenUserIdIsNull() {
        assertThrows(MissingElementException.class, () -> {
            Long userId = null;
            UserRoleDto userRole = new UserRoleDto();
            userRole.setRole(RoleDto.ROLE_OPERATOR);
            when(userService.findById(userId)).thenReturn(Optional.empty());
            usersController.removeUserRole(userId, userRole, principal);
        });
    }

    @Test
    void shouldNotRemoveUserRoleWhenUserRoleIsNull() {
        assertThrows(MissingElementException.class, () -> {
            UserRoleDto userRole = null;
            usersController.removeUserRole(userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldNotRemoveUserRoleWithoutDomain() {
        assertThrows(MissingElementException.class, () -> {
            UserRoleDto userRole = new UserRoleDto();
            userRole.setRole(RoleDto.ROLE_OPERATOR);
            when(domainService.getGlobalDomain()).thenReturn(Optional.empty());
            usersController.removeUserRole(userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldChangePassword() {
        when(principal.getName()).thenReturn(userList.getFirst().getUsername());
        when(userService.findByUsername(userList.getFirst().getUsername())).thenReturn(Optional.of(userList.getFirst()));
        PasswordChangeRequest passwordChange = new PasswordChangeRequest(userList.getFirst().getPassword(), "test1234");
        when(passwordEncoder.matches(userList.getFirst().getPassword(), passwordChange.password())).thenReturn(true);
        usersController.changePassword(principal, passwordChange);
        verify(userService, times(1)).update(userList.getFirst());
    }

    @Test
    void shouldNotChangePasswordOnPreviousPasswordMismatch() {
        assertThrows(ProcessingException.class, () -> {
            when(principal.getName()).thenReturn(userList.getFirst().getUsername());
            when(userService.findByUsername(userList.getFirst().getUsername())).thenReturn(Optional.of(userList.getFirst()));
            PasswordChangeRequest passwordChange = new PasswordChangeRequest("wrongpass", "test1234");
            when(passwordEncoder.matches(userList.getFirst().getPassword(), passwordChange.password())).thenReturn(false);
            usersController.changePassword(principal, passwordChange);
            verify(userService, times(1)).update(userList.getFirst());
        });
    }

    @Test
    void shouldGetDomainUsers() {
        Long domainId = 1L;
        when(domainService.getMembers(domainId)).thenReturn(userList);
        List<UserInfoDto> users = usersController.getDomainUsers(domainId);
        assertThat("List size mismatch", users.size() == userList.size());
    }

    @Test
    void shouldGetDomainUser() {
        Long domainId = 1L;
        Long userId = 1L;
        when(domainService.getMember(domainId, userId)).thenReturn(userList.getFirst());
        UserDto user = usersController.getDomainUser(domainId, userId);
        assertThat("User mismatch", user.getUsername().equals(userList.getFirst().getUsername()));
    }

    @Test
    void shouldNotGetDomainUserWhenDomainNotExists() {
        assertThrows(MissingElementException.class, () -> {
            Long domainId = 5L;
            Long userId = 1L;
            when(domainService.getMember(domainId, userId)).thenThrow(ObjectNotFoundException.class);
            UserDto user = usersController.getDomainUser(domainId, userId);
        });
    }

    @Test
    void shouldNotGetDomainUserWhenUserNotExist() {
        assertThrows(ProcessingException.class, () -> {
            Long domainId = 1L;
            Long userId = 8L;
            when(domainService.getMember(domainId, userId)).thenThrow(ProcessingException.class);
            usersController.getDomainUser(domainId, userId);
        });
    }

    @Test
    void shouldRemoveDomainUser() {
        usersController.removeDomainUser(DOMAIN.getId(), userList.getFirst().getId());
        verify(domainService, times(1)).removeMember(DOMAIN.getId(), userList.getFirst().getId());
    }

    @Test
    void shouldGetUserDomainRoles() {
        Set<Role> roles = usersController.getUserRoles(DOMAIN.getId(), userList.getFirst().getId());
        verify(domainService, times(1)).getMemberRoles(DOMAIN.getId(), userList.getFirst().getId());
    }

    @Test
    void shouldAddUserRoleToCustomDomain() {
        UserRoleDto userRole = new UserRoleDto();
        userRole.setDomainId(DOMAIN.getId());
        userRole.setRole(RoleDto.ROLE_USER);

        usersController.addUserRole(DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        verify(domainService, times(1)).addMemberRole(DOMAIN.getId(), userList.getFirst().getId(), Role.valueOf(userRole.getRole().name()));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shouldAddUserRoleToGlobalDomain() {
        UserRoleDto userRole = new UserRoleDto();
        userRole.setDomainId(GLOBAL_DOMAIN.getId());
        userRole.setRole(RoleDto.ROLE_OPERATOR);
        when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
        usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        verify(domainService, times(1)).addMemberRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), Role.valueOf(userRole.getRole().name()));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldNotAddUserRoleWithNullRole() {
        assertThrows(MissingElementException.class, () -> {
            usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), null, principal);
        });
    }

    @Test
    void shouldNotAddUserRoleWithNullUserRole() {
        assertThrows(MissingElementException.class, () -> {
            UserRoleDto userRole = new UserRoleDto();
            userRole.setRole(null);
            usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldNotAddGlobalUserRoleInNotGlobalDomain() {
        assertThrows(ProcessingException.class, () -> {
            UserRoleDto userRole = new UserRoleDto();
            userRole.setDomainId(DOMAIN.getId());
            userRole.setRole(RoleDto.ROLE_OPERATOR);
            when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
            usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldNotAddNonGlobalRoleToGlobalDomain() {
        assertThrows(ProcessingException.class, () -> {
            UserRoleDto userRole = new UserRoleDto();
            userRole.setDomainId(GLOBAL_DOMAIN.getId());
            userRole.setRole(RoleDto.ROLE_DOMAIN_ADMIN);
            when(domainService.findDomain(GLOBAL_DOMAIN.getId())).thenReturn(Optional.of(GLOBAL_DOMAIN));
            usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldNotAddGlobalRoleToCustomDomain() {
        assertThrows(ProcessingException.class, () -> {
            UserRoleDto userRole = new UserRoleDto();
            userRole.setDomainId(DOMAIN.getId());
            userRole.setRole(RoleDto.ROLE_SYSTEM_ADMIN);
            usersController.addUserRole(GLOBAL_DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldRemoveUserRole() {
        String userRole = "ROLE_SYSTEM_ADMIN";
        usersController.removeUserRole(DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        verify(domainService, times(1)).removeMemberRole(DOMAIN.getId(), userList.getFirst().getId(), Role.ROLE_SYSTEM_ADMIN);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shouldNotConvertIncorrectStringWhenRemovingUserRole() {
        assertThrows(MissingElementException.class, () -> {
            String userRole = "ROLE_WRONG";
            usersController.removeUserRole(DOMAIN.getId(), userList.getFirst().getId(), userRole, principal);
        });
    }

    @Test
    void shouldSetEnabledFlag() {
        usersController.setEnabledFlag(userList.getFirst().getId(), true, principal);
        verify(userService, times(1)).setEnabledFlag(userList.getFirst().getId(), true);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shouldSetDisabledFlag() {
        usersController.setEnabledFlag(userList.getFirst().getId(), false, principal);
        verify(userService, times(1)).setEnabledFlag(userList.getFirst().getId(), false);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shouldDeleteUser() {
        User tester = userList.getFirst();
        tester.setRoles(new ArrayList<>());
        when(instanceService.findAllByOwner(tester.getId())).thenReturn(new ArrayList<>());
        usersController.deleteUser(tester.getId());
        verify(userService, times(1)).deleteById(tester.getId());
    }

}
