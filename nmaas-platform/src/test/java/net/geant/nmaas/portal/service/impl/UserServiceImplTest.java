package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserRoleRepository userRoleRepository;

    @Mock
    ConfigurationManager configurationManager;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    public void setup(){
        userService = new UserServiceImpl(userRepository, userRoleRepository, new BCryptPasswordEncoder(), configurationManager, new ModelMapper());
    }

    @Test
    public void hasPrivilegeShouldReturnFalseDueToEmptyUser(){
        User user = null;
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        Role role = Role.ROLE_USER;
        assertFalse(userService.hasPrivilege(user, domain, role));
    }

    @Test
    public void hasPrivilegeShouldReturnFalseDueToEmptyDomain(){
        User user = new User("test", true);
        Domain domain = null;
        Role role = Role.ROLE_USER;
        assertFalse(userService.hasPrivilege(user, domain, role));
    }

    @Test
    public void hasPrivilegeShouldReturnFalseDueToEmptyRole(){
        User user = new User("test", true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        Role role = null;
        assertFalse(userService.hasPrivilege(user, domain, role));
    }

    @Test
    public void hasPrivilegeShouldReturnFalseDueToEmptyUserRole(){
        User user = new User("test", true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        Role role = Role.ROLE_USER;
        when(userRoleRepository.findByDomainAndUserAndRole(domain, user, role)).thenReturn(null);
        assertFalse(userService.hasPrivilege(user, domain, role));
    }

    @Test
    public void hasPrivilegeShouldPassCorrectly(){
        User user = new User("test", true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        Role role = Role.ROLE_USER;
        UserRole userRole = new UserRole(user, domain, role);
        when(userRoleRepository.findByDomainAndUserAndRole(domain, user, role)).thenReturn(userRole);
        assertTrue(userService.hasPrivilege(user, domain, role));
    }

    @Test
    void adminShouldUpdateData(){
        User admin = new User("admin", true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        admin.setRoles(Collections.singletonList(new UserRole(admin, domain, Role.ROLE_SYSTEM_ADMIN)));
        when(userRepository.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        assertTrue(userService.canUpdateData(admin.getUsername(), Collections.singletonList(new UserRole(admin, domain, Role.ROLE_USER))));
    }

    @Test
    void domainAdminShouldUpdateDataOfUserInHisDomain(){
        User admin = new User("admin", true);
        User user = new User("test", true);
        Domain domain = new Domain("testdom", "testdom");
        admin.setRoles(Collections.singletonList(new UserRole(admin, domain, Role.ROLE_DOMAIN_ADMIN)));
        when(userRepository.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        assertTrue(userService.canUpdateData(admin.getUsername(), Collections.singletonList(new UserRole(user, domain, Role.ROLE_USER))));
    }

    @Test
    void domainAdminShouldNotUpdateDataOfUserNotInHisDomain(){
        User admin = new User("admin", true);
        User user = new User("test", true);
        Domain domain = new Domain("testdom", "testdom");
        Domain otherDomain = new Domain("domtest", "domtest");
        admin.setRoles(Collections.singletonList(new UserRole(admin, domain, Role.ROLE_DOMAIN_ADMIN)));
        when(userRepository.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        assertFalse(userService.canUpdateData(admin.getUsername(), Collections.singletonList(new UserRole(user, otherDomain, Role.ROLE_USER))));
    }

    @Test
    void userShouldNotUpdateOtherUserData(){
        User admin = new User("admin", true);
        User user = new User("test", true);
        Domain domain = new Domain("testdom", "testdom");
        admin.setRoles(Collections.singletonList(new UserRole(admin, domain, Role.ROLE_USER)));
        when(userRepository.findByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        assertFalse(userService.canUpdateData(admin.getUsername(), Collections.singletonList(new UserRole(user, domain, Role.ROLE_USER))));
    }

    @Test
    public void findAllShouldReturnListWithElements(){
        User user = new User("test1", true);
        User user1 = new User("test2", true);
        User user2 = new User("test3", true);
        List<User> testList = new ArrayList<>();
        testList.add(user);
        testList.add(user1);
        testList.add(user2);
        when(userRepository.findAll()).thenReturn(testList);
        List<User> resultList = userService.findAll();
        assertEquals(3, resultList.size());
        assertEquals("test1", resultList.get(0).getUsername());
        assertEquals("test2", resultList.get(1).getUsername());
        assertEquals("test3", resultList.get(2).getUsername());
    }

    @Test
    public void findByUsernameShouldReturnEmptyOptionalWhenNull(){
        assertEquals(Optional.empty(), userService.findByUsername(null));
    }

    @Test
    public void findByUsernameShouldReturnUserObject(){
        User user = new User("test1", true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        Optional<User> resultUser = userService.findByUsername("test");
        assertTrue(resultUser.isPresent());
        assertEquals("test1", resultUser.get().getUsername());
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalWhenNull(){
        assertEquals(Optional.empty(), userService.findById(null));
    }

    @Test
    public void findByIdShouldReturnUserObject(){
        User user = new User("test1", true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Optional<User> resultUser = userService.findById((long) 0);
        assertTrue(resultUser.isPresent());
        assertEquals("test1", resultUser.get().getUsername());
    }

    @Test
    public void findBySamlTokenShouldReturnEmptyOptionalWhenNull(){
        assertEquals(Optional.empty(), userService.findBySamlToken(null));
    }

    @Test
    public void findBySamlTokenShouldReturnUserObject(){
        User user = new User("test1", true);
        when(userRepository.findBySamlToken(anyString())).thenReturn(Optional.of(user));
        Optional<User> resultUser = userService.findBySamlToken("token");
        assertTrue(resultUser.isPresent());
        assertEquals("test1", resultUser.get().getUsername());
    }

    @Test
    public void existsByUsernameCheckParamShouldThrowException(){
        assertThrows(IllegalArgumentException.class, () -> {
            userService.existsByUsername(null);
        });
    }

    @Test
    public void existsByIdCheckParamShouldThrowException(){
        assertThrows(IllegalArgumentException.class, () -> {
            userService.existsById(null);
        });
    }

    @Test
    public void existsByUsernameShouldThrowFalseWhenThereIsNoUserWithSpecifiedName(){
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        assertFalse(userService.existsByUsername("test1"));
    }

    @Test
    public void existsByIdShouldThrowFalseWhenThereIsNoUserWithSpecifiedId(){
        when(userRepository.existsById(anyLong())).thenReturn(false);
        assertFalse(userService.existsById((long) 0));
    }

    @Test
    public void existsByUsernameShouldThrowTrue(){
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        assertTrue(userService.existsByUsername("test1"));
    }

    @Test
    public void existsByIdShouldThrowTrue(){
        when(userRepository.existsById(anyLong())).thenReturn(true);
        assertTrue(userService.existsById((long) 0));
    }

    @Test
    public void existsByEmailShouldThrowTrue(){
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertTrue(userService.existsByEmail("test@test.com"));
    }

    @Test
    public void existsByEmailShouldThrowFalseWhenThereIsNoUserWithSpecifiedId(){
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        assertFalse(userService.existsByEmail("test@test.com"));
    }

    @Test
    public void shouldRegisterUserWithGlobalGuestRole(){
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        Registration registration = new Registration("test", "testpass","test@test.com", "name", "surname", 1L, true, true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(1L, false, false, "en", false, false, new ArrayList<>()));
        User user = userService.register(registration, domain, null);
        verify(userRepository, times(1)).save(any());
        assertEquals(1, user.getRoles().size());
        assertEquals(Role.ROLE_GUEST, user.getRoles().get(0).getRole());
        assertEquals(domain, user.getRoles().get(0).getDomain());
    }

    @Test
    public void shouldRegisterUserWithGlobalGuestRoleAndRoleInDomain(){
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(1L, false, false, "en", false, false, new ArrayList<>()));
        Registration registration = new Registration("test", "testpass","test@test.com", "name", "surname", 1L, true, true);
        Domain globalDomain = new Domain("GLOBAL", "GLOBAL");
        Domain domain = new Domain("Non Global", "NONGLO");
        User user = userService.register(registration, globalDomain, domain);
        verify(userRepository, times(1)).save(any());
        assertEquals(2, user.getRoles().size());
        assertEquals(globalDomain, user.getRoles().get(0).getDomain());
        assertEquals(domain, user.getRoles().get(1).getDomain());
    }

    @Test
    public void shouldNotRegisterUserWhenUserAlreadyExists(){
        assertThrows(SignupException.class, () -> {
            Registration registration = new Registration("test", "testpass", "test@test.com", "name", "surname", 1L, true, true);
            Domain domain = new Domain("GLOBAL", "GLOBAL");
            when(userRepository.existsByUsername(registration.getUsername())).thenReturn(true);
            userService.register(registration, domain, null);
        });
    }

    @Test
    public void shouldNotRegisterUserWhenUserAlreadyExistsByMail(){
        assertThrows(SignupException.class, () -> {
            Registration registration = new Registration("test", "testpass", "test@test.com", "name", "surname", 1L, true, true);
            Domain domain = new Domain("GLOBAL", "GLOBAL");
            when(userRepository.existsByEmail(registration.getEmail())).thenReturn(true);
            userService.register(registration, domain, null);
        });
    }

    @Test
    public void shouldRegisterSSOUser(){
        UserSSOLogin ssoUser = new UserSSOLogin("test|1234|id");
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(1L, false, false, "en", false, false, new ArrayList<>()));
        User user = userService.register(ssoUser, domain);
        verify(userRepository, times(1)).save(any());
        assertEquals(user.getSamlToken(), ssoUser.getUsername());
    }

    @Test
    public void updateShouldFailDueToEmptyUser(){
        assertThrows(IllegalArgumentException.class, () -> {
            userService.update(null);
        });
    }

    @Test
    public void updateShouldFailDueToEmptyUserId(){
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("test", true);
            userService.update(user);
        });
    }

    @Test
    public void updateShouldFailDueToUserDoNotExist(){
        assertThrows(ProcessingException.class, () -> {
            when(userRepository.existsById(anyLong())).thenReturn(false);
            User user = new User("test", true);
            user.setId((long) 0);
            userService.update(user);
        });
    }

    @Test
    public void updateShouldPassCorrectly(){
        when(userRepository.existsById(anyLong())).thenReturn(true);
        User user = new User("test", true);
        user.setId((long) 0);
        userService.update(user);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    public void deleteShouldFailDueToEmptyUser(){
        assertThrows(IllegalArgumentException.class, () -> {
            userService.delete(null);
        });
    }

    @Test
    public void deleteShouldFailDueToEmptyUserId(){
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("test", true);
            userService.delete(user);
        });
    }

    @Test
    public void deleteShouldPassCorrectly(){
        User user = new User("test", true);
        user.setId((long) 0);
        userService.delete(user);
        verify(userRepository).delete(user);
    }

    @Test
    public void setEnabledFlagShouldChangeFlagToTrue(){
        userService.setEnabledFlag((long) 0, true);
        verify(userRepository).setEnabledFlag((long) 0, true);
    }

    @Test
    public void setEnabledFlagShouldChangeFlagToFalse(){
        userService.setEnabledFlag((long) 0, false);
        verify(userRepository).setEnabledFlag((long) 0, false);
    }

    @Test
    public void setTermsOfUseAcceptedFlagShouldChangeFlagToTrue(){
        userService.setTermsOfUseAcceptedFlag((long) 0, true);
        verify(userRepository).setTermsOfUseAcceptedFlag((long) 0, true);
    }

    @Test
    public void setTermsOfUseAcceptedFlagShouldChangeFlagToFalse(){
        userService.setTermsOfUseAcceptedFlag((long) 0, false);
        verify(userRepository).setTermsOfUseAcceptedFlag((long) 0, false);
    }

    @Test
    public void setPrivacyPolicyAcceptedFlagShouldChangeFlagToTrue(){
        userService.setPrivacyPolicyAcceptedFlag((long) 0, true);
        verify(userRepository).setPrivacyPolicyAcceptedFlag((long) 0, true);
    }

    @Test
    public void setPrivacyPolicyAcceptedFlagShouldChangeFlagToFalse(){
        userService.setPrivacyPolicyAcceptedFlag((long) 0, false);
        verify(userRepository).setPrivacyPolicyAcceptedFlag((long) 0, false);
    }

    @Test
    public void findAllUsersEmailWithAdminRole(){
        List<User> users = new ArrayList<>();
        List<UserRole> userRoles = new ArrayList<>();

        Domain domain = new Domain((long) 1, "test", "test");
        User user = new User("test");
        user.setFirstname("test");
        user.setLastname("test");
        user.setEmail("test1@email.com");
        UserRole userRole = new UserRole(user, domain, Role.ROLE_SYSTEM_ADMIN);
        userRoles.add(userRole);

        user.setRoles(userRoles);
        users.add(user);
        when(userRepository.findAll()).thenReturn(users);
        assertEquals(1, userService.findAllUsersWithAdminRole().size());
        assertEquals(userService.findAllUsersWithAdminRole().get(0).getEmail(), user.getEmail());
    }

}
