package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@Transactional(value=TxType.REQUIRES_NEW)
@Rollback
public class UsersControllerIntTest extends BaseControllerTest {

    final static String DOMAIN = "DOMAIN";

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UsersController userController;

    @Autowired
    private DomainService domains;

    @Autowired
    private JWTTokenService jwtTokenService;

    private String token;

    private User user1;

    private Principal principal = mock(Principal.class);

    @Before
    public void setUp() throws Exception {
        mvc = createMVC();
        when(principal.getName()).thenReturn("admin");

        domains.createGlobalDomain();
        domains.createDomain(DOMAIN, DOMAIN);

        //Add extra users, default admin is already there
        User admin = userRepo.save(new User("manager", true, "manager", domains.getGlobalDomain().get(), Arrays.asList(Role.ROLE_SUPERADMIN)));
        user1 = userRepo.save(new User("user1", true, "user1", domains.findDomain(DOMAIN).get(), Arrays.asList(Role.ROLE_USER)));
        userRepo.save(new User("user2", true, "user2", domains.findDomain(DOMAIN).get(), Arrays.asList(Role.ROLE_USER)));

        UserToken userToken = new UserToken(jwtTokenService.getToken(admin), jwtTokenService.getRefreshToken(admin));
        token = userToken.getToken();

        prepareSecurity();
    }

    @Test
    public void testDisableUser() throws Exception {
        MvcResult result = mvc.perform(put("/api/users/status/" + user1.getId() + "?enabled=false")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    @Test
    public void testEnableUser() throws Exception {
        MvcResult result =  mvc.perform(put("/api/users/status/" + user1.getId() + "?enabled=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    @Test
    public void testGetUsers() {
        assertEquals(4, userController.getUsers(Pageable.unpaged()).size());
    }

    @Test
    public void testGetRoles() {
        assertEquals(7, userController.getRoles().size());
    }

    @Test
    public void testGetUser() throws MissingElementException {
        net.geant.nmaas.portal.api.domain.User user = userController.retrieveUser(1L);
        assertEquals(new Long(1), user.getId());
        assertEquals("admin", user.getUsername());

    }

    @Test
    public void testUpdateUserPasswordAndRole() throws ProcessingException, MissingElementException {
        String newPass = "newPass";
        String oldPass = user1.getPassword();
        userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, user1.getUsername(), newPass), principal);
        User modUser1 = userRepo.findById(user1.getId()).get();

        assertEquals(user1.getUsername(), modUser1.getUsername());
        assertNotEquals(oldPass, modUser1.getPassword());
        assertEquals(1, modUser1.getRoles().size());
        //assertEquals(Role.TOOL_MANAGER, modUser1.getRoles().get(0).getRole());
    }

    @Test
    public void testDeleteUser() {
        //Update test when user delete is supported
        try {
            userController.deleteUser(user1.getId());
            fail();
        } catch(Exception ex) {

        }
    }

    @Test
    public void testGetRolesAsString(){
        Role role1 = Role.ROLE_USER;
        Role role2 = Role.ROLE_SUPERADMIN;
        Role role3 = Role.ROLE_DOMAIN_ADMIN;
        UserRole userRole1 = new UserRole(new User("TEST1"), new Domain("TEST", "TEST"), role1);
        UserRole userRole2 = new UserRole(new User("TEST2"), new Domain("TEST", "TEST"), role2);
        UserRole userRole3 = new UserRole(new User("TEST3"), new Domain("TEST", "TEST"), role3);

        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole1);
        userRoles.add(userRole2);
        userRoles.add(userRole3);

        assertEquals("ROLE_USER, ROLE_SUPERADMIN, ROLE_DOMAIN_ADMIN", userController.getRoleAsString(userRoles));
    }

    @Test
    public void testGetMessageWhenUserUpdated(){
        Role role1 = Role.ROLE_USER;
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role1);

        Role role2 = Role.ROLE_TOOL_MANAGER;
        UserRole userRole2 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role2);

        List<UserRole> userRoles1 = new ArrayList<>();
        userRoles1.add(userRole1);
        userRoles1.add(userRole2);

        Role role3 = Role.ROLE_DOMAIN_ADMIN;
        net.geant.nmaas.portal.api.domain.UserRole userRole3 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole3.setRole(role3);
        userRole3.setDomainId(1L);
        Set<net.geant.nmaas.portal.api.domain.UserRole> userRoles3 = new HashSet<>();
        userRoles3.add(userRole3);

        net.geant.nmaas.portal.persistent.entity.User user = new User("user1");
        user.setFirstname("FirstName");
        user.setLastname("Lastname");
        user.setEmail("email@email.com");
        user.setRoles(userRoles1);
        user.setEnabled(true);

        UserRequest userRequest = new UserRequest(2L, "user2", "password");
        userRequest.setEmail("email1@email.com");
        userRequest.setFirstname("FirstName1");
        userRequest.setLastname("LastName1");
        userRequest.setRoles(userRoles3);

        String message = userController.getMessageWhenUserUpdated(user, userRequest);
        assertEquals(
                System.lineSeparator() + "||| Username changed from - user1 to - user2|||" +
                        System.lineSeparator() + "||| Email changed from - email@email.com to - email1@email.com|||" +
                        System.lineSeparator() + "||| First name changed from - FirstName to - FirstName1|||" +
                        System.lineSeparator() + "||| Last name changed from - Lastname to - LastName1|||" +
                        System.lineSeparator() + "||| Enabled flag changed from - true to - false|||" +
                        System.lineSeparator() + "||| Role changed from - ROLE_USER, ROLE_TOOL_MANAGER to - ROLE_DOMAIN_ADMIN@domain1|||", message);
    }

    @Test
    public void testGetMessageWhenUserUpdatedWithSameRolesInDifferentOrder(){
        Role role1 = Role.ROLE_USER;
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role1);

        Role role2 = Role.ROLE_TOOL_MANAGER;
        UserRole userRole2 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role2);

        List<UserRole> userRoles1 = new ArrayList<>();
        userRoles1.add(userRole1);
        userRoles1.add(userRole2);

        Role role3 = Role.ROLE_TOOL_MANAGER;
        net.geant.nmaas.portal.api.domain.UserRole userRole3 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole3.setRole(role3);

        Role role4 = Role.ROLE_USER;
        net.geant.nmaas.portal.api.domain.UserRole userRole4 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole4.setRole(role4);

        Set<net.geant.nmaas.portal.api.domain.UserRole> userRoles2 = new HashSet<>();
        userRoles2.add(userRole3);
        userRoles2.add(userRole4);

        net.geant.nmaas.portal.persistent.entity.User user = new User("user1");
        user.setFirstname("FirstName");
        user.setLastname("Lastname");
        user.setEmail("email@email.com");
        user.setRoles(userRoles1);
        user.setEnabled(true);

        UserRequest userRequest = new UserRequest(2L, "user2", "password");
        userRequest.setEmail("email1@email.com");
        userRequest.setFirstname("FirstName1");
        userRequest.setLastname("LastName1");
        userRequest.setRoles(userRoles2);

        String message = userController.getMessageWhenUserUpdated(user, userRequest);
        assertEquals(
                System.lineSeparator() + "||| Username changed from - user1 to - user2|||" +
                        System.lineSeparator() + "||| Email changed from - email@email.com to - email1@email.com|||" +
                        System.lineSeparator() + "||| First name changed from - FirstName to - FirstName1|||" +
                        System.lineSeparator() + "||| Last name changed from - Lastname to - LastName1|||" +
                        System.lineSeparator() + "||| Enabled flag changed from - true to - false|||", message);
    }

    @Test
    public void testGetRoleWithDomainIdAsString(){
        Role role1 = Role.ROLE_USER;
        net.geant.nmaas.portal.api.domain.UserRole userRole1 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole1.setRole(role1);
        userRole1.setDomainId(1L);

        Role role2 = Role.ROLE_GUEST;
        net.geant.nmaas.portal.api.domain.UserRole userRole2 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole2.setRole(role2);
        userRole2.setDomainId(2L);

        Set<net.geant.nmaas.portal.api.domain.UserRole> userRoles = new LinkedHashSet<>();
        userRoles.add(userRole1);
        userRoles.add(userRole2);

        assertEquals("ROLE_USER@domain1, ROLE_GUEST@domain2", userController.getRoleWithDomainIdAsString(userRoles));

    }
}
