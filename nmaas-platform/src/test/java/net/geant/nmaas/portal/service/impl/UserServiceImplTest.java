package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserRoleRepository userRoleRepository;

    @InjectMocks
    UserServiceImpl userService;

    @Before
    public void setup(){
        userService = new UserServiceImpl(userRepository, userRoleRepository);
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

    @Test(expected = IllegalArgumentException.class)
    public void existsByUsernameCheckParamShouldThrowException(){
        userService.existsByUsername(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void existsByIdCheckParamShouldThrowException(){
        userService.existsById(null);
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

    @Test(expected = ObjectAlreadyExistsException.class)
    public void registerWithUsernameAndDomainShouldThrowExceptionThatUserAlreadyExists(){
        User user = new User("test1", true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        userService.register("test", domain);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerWithUsernameAndDomainShouldThrowExceptionThatUsernameIsIncorrect(){
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        userService.register(null, domain);
    }

    @Test
    public void registerWithUsernameAndDomainShouldRegisterAndReturnObject(){
        User user = new User("test", true);
        Domain domain = new Domain("GLOBAL", "GLOBAL");
        when(userRepository.save(isA(User.class))).thenReturn(user);
        User result = userService.register("test", domain);
        assertEquals("test", result.getUsername());
        assertNull(result.getPassword());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShouldFailDueToEmptyUser(){
        userService.update(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShouldFailDueToEmptyUserId(){
        User user = new User("test", true);
        userService.update(user);
    }

    @Test(expected = ProcessingException.class)
    public void updateShouldFailDueToUserDoNotExist(){
        when(userRepository.existsById(anyLong())).thenReturn(false);
        User user = new User("test", true);
        user.setId((long) 0);
        userService.update(user);
    }

    @Test
    public void updateShouldPassCorrectly(){
        when(userRepository.existsById(anyLong())).thenReturn(true);
        User user = new User("test", true);
        user.setId((long) 0);
        userService.update(user);
        verify(userRepository).saveAndFlush(user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteShouldFailDueToEmptyUser(){
        userService.delete(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteShouldFailDueToEmptyUserId(){
        User user = new User("test", true);
        userService.delete(user);
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
        User user = User.builder().firstname("test").lastname("test").email("test1@email.com").build();
        UserRole userRole = new UserRole(user, domain, Role.ROLE_SYSTEM_ADMIN);
        userRoles.add(userRole);

        user.setRoles(userRoles);
        users.add(user);
        when(userRepository.findAll()).thenReturn(users);
        assertEquals("test1@email.com,", userService.findAllUsersEmailWithAdminRole());
    }

}
