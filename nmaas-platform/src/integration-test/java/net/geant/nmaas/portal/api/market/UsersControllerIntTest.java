package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.PasswordReset;
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
import net.geant.nmaas.portal.service.UserService;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_GUEST;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_NOT_ACCEPTED;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_SYSTEM_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_TOOL_MANAGER;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@Transactional(value=TxType.REQUIRES_NEW)
@Rollback
public class UsersControllerIntTest extends BaseControllerTestSetup {

    final static String DOMAIN = "domain";

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UsersController userController;

    @Autowired
    private JWTTokenService jwtTokenService;

    private String token;
    private String tokenForUserWithNotAcceptedTermsAndPolicy;

    private User userEntity;
    private User user3;

    private Principal principal = mock(Principal.class);

    @Before
    public void setUp() throws Exception {
        mvc = createMVC();
        when(principal.getName()).thenReturn("admin");

        domains.createGlobalDomain();
        domains.createDomain(DOMAIN, DOMAIN);

        //Add extra users, default admin is already there
        User admin = new User("manager", true, "manager", domains.getGlobalDomain().get(), Arrays.asList(ROLE_SYSTEM_ADMIN));
        admin.setEmail("manager@testemail.com");
        userRepo.save(admin);

        User userStub = new User("userEntity", true, "userEntity", domains.findDomain(DOMAIN).get(), Arrays.asList(ROLE_USER));
        userStub.setFirstname("Test");
        userStub.setLastname("Test");
        userStub.setEmail("test@gmail.com");
        userEntity = userRepo.save(userStub);
        User user2 = new User("user2", true, "user2", domains.findDomain(DOMAIN).get(), Arrays.asList(ROLE_USER));
        user2.setEmail("user2@testemail.com");
        userRepo.save(user2);

        user3 = new User("user3", true, "user3", domains.getGlobalDomain().get(), ROLE_NOT_ACCEPTED, false, false);
        user3.setEmail("user3@testemail.com");
        userRepo.save(user3);

        UserToken userToken = new UserToken(tokenService.getToken(admin), tokenService.getRefreshToken(admin));
        token = userToken.getToken();

        UserToken userNotAcceptedTermsAndPolicyToken = new UserToken(tokenService.getToken(user3), tokenService.getRefreshToken(user3));
        tokenForUserWithNotAcceptedTermsAndPolicy = userNotAcceptedTermsAndPolicyToken.getToken();

        prepareSecurity();
    }

    @Ignore
    @Test
    public void testDisableUser() throws Exception {
        MvcResult result = mvc.perform(put("/api/users/status/" + userEntity.getId() + "?enabled=false")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    @Ignore
    @Test
    public void testEnableUser() throws Exception {
        MvcResult result =  mvc.perform(put("/api/users/status/" + userEntity.getId() + "?enabled=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    @Test
    public void testSetAcceptanceOfTermsOfUseAndPrivacyPolicy() throws Exception{
        MvcResult result =  mvc.perform(post("/api/users/terms/" + user3.getUsername())
                .header("Authorization", "Bearer " + tokenForUserWithNotAcceptedTermsAndPolicy)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    @Test
    public void testGetUsers() {
        assertEquals(5, userController.getUsers(Pageable.unpaged()).size());
    }

    @Test
    public void testGetRoles() {
        assertEquals(8, userController.getRoles().size());
    }

    @Test
    public void testGetUser() throws MissingElementException {
        net.geant.nmaas.portal.api.domain.User user = userController.retrieveUser(1L);
        assertEquals(new Long(1), user.getId());
        assertEquals("admin", user.getUsername());
    }

    @Test
    public void shouldUpdateUserWithNewFirstNameAndLastName() {
        String newFirstName = "TestFirstName";
        String newLastName = "TestLastName";
        UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
        userRequest.setFirstname(newFirstName);
        userRequest.setLastname(newLastName);
        userController.updateUser(userEntity.getId(), userRequest, principal);
        User modUser1 = userRepo.findById(userEntity.getId()).get();

        assertEquals(modUser1.getFirstname(), newFirstName);
        assertEquals(modUser1.getLastname(), newLastName);
    }

    @Test
    public void shouldUpdateUserWithNewEmail(){
        String newEmail = "admin@testemail.com";
        UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
        userRequest.setEmail(newEmail);
        userController.updateUser(userEntity.getId(), userRequest, principal);
        User modUser1 = userRepo.findById(userEntity.getId()).get();

        assertEquals(modUser1.getEmail(), newEmail);
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotUpdateUserWithTakenEmail(){
        String newEmail = user3.getEmail();
        UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
        userRequest.setEmail(newEmail);
        userController.updateUser(userEntity.getId(), userRequest, principal);
    }

    @Test
    public void testDeleteUser() {
        //Update test when user delete is supported
        try {
            userController.deleteUser(userEntity.getId());
            fail();
        } catch(Exception ex) {

        }
    }

    @Test
    public void testGetRolesAsString(){
        Role role1 = ROLE_USER;
        Role role2 = ROLE_SYSTEM_ADMIN;
        Role role3 = ROLE_DOMAIN_ADMIN;
        UserRole userRole1 = new UserRole(new User("TEST1"), new Domain("TEST", "TEST"), role1);
        UserRole userRole2 = new UserRole(new User("TEST2"), new Domain("TEST", "TEST"), role2);
        UserRole userRole3 = new UserRole(new User("TEST3"), new Domain("TEST", "TEST"), role3);

        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole1);
        userRoles.add(userRole2);
        userRoles.add(userRole3);

        assertEquals("ROLE_USER, ROLE_SYSTEM_ADMIN, ROLE_DOMAIN_ADMIN", userController.getRoleAsString(userRoles));
    }

    @Test
    public void testGetMessageWhenUserUpdated(){
        Role role1 = ROLE_USER;
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role1);

        Role role2 = ROLE_TOOL_MANAGER;
        UserRole userRole2 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role2);

        List<UserRole> userRoles1 = new ArrayList<>();
        userRoles1.add(userRole1);
        userRoles1.add(userRole2);

        Role role3 = ROLE_DOMAIN_ADMIN;
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
                " Username [user1] -> [user2] Email [email@email.com] -> [email1@email.com] First name [FirstName] -> [FirstName1] Last name [Lastname] -> [LastName1] Enabled flag [true] -> [false] Roles changed [ROLE_USER, ROLE_TOOL_MANAGER] -> [ROLE_DOMAIN_ADMIN@domain1]",
                message
        );
    }

    @Test
    public void testGetMessageWhenUserUpdatedWithSameRolesInDifferentOrder(){
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), ROLE_USER);
        UserRole userRole2 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), ROLE_TOOL_MANAGER);

        net.geant.nmaas.portal.persistent.entity.User user = new User("user1");
        user.setFirstname("FirstName");
        user.setLastname("Lastname");
        user.setEmail("email@email.com");
        user.setRoles(Stream.of(userRole1, userRole2).collect(Collectors.toList()));
        user.setEnabled(true);

        net.geant.nmaas.portal.api.domain.UserRole userRole3 = new net.geant.nmaas.portal.api.domain.UserRole(ROLE_TOOL_MANAGER, 1L);
        net.geant.nmaas.portal.api.domain.UserRole userRole4 = new net.geant.nmaas.portal.api.domain.UserRole(ROLE_USER, 1L);

        UserRequest userRequest = new UserRequest(2L, "user2", "password");
        userRequest.setFirstname("FirstName1");
        userRequest.setLastname("LastName1");
        userRequest.setEmail("email1@email.com");
        userRequest.setRoles(Stream.of(userRole3, userRole4).collect(Collectors.toSet()));

        String message = userController.getMessageWhenUserUpdated(user, userRequest);
        assertEquals(
                " Username [user1] -> [user2] Email [email@email.com] -> [email1@email.com] First name [FirstName] -> [FirstName1] Last name [Lastname] -> [LastName1] Enabled flag [true] -> [false]",
                message
        );
    }

    @Test
    public void testGetRoleWithDomainIdAsString(){
        Role role1 = ROLE_USER;
        net.geant.nmaas.portal.api.domain.UserRole userRole1 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole1.setRole(role1);
        userRole1.setDomainId(1L);

        Role role2 = ROLE_GUEST;
        net.geant.nmaas.portal.api.domain.UserRole userRole2 = new net.geant.nmaas.portal.api.domain.UserRole();
        userRole2.setRole(role2);
        userRole2.setDomainId(2L);

        Set<net.geant.nmaas.portal.api.domain.UserRole> userRoles = new LinkedHashSet<>();
        userRoles.add(userRole1);
        userRoles.add(userRole2);

        assertEquals("ROLE_USER@domain1, ROLE_GUEST@domain2", userController.getRoleWithDomainIdAsString(userRoles));

    }

    @Test
    public void shouldValidateResetRequest() throws Exception {
        MvcResult result = mvc.perform(post("/api/users/reset/validate")
                .content(jwtTokenService.getResetToken(user3.getEmail()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(user3.getEmail()));
    }

    @Test
    public void shouldNotValidateResetRequest() throws Exception {
        mvc.perform(post("/api/users/reset/validate")
                .content(jwtTokenService.getResetToken("notexisting@email.co.uk"))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldResetPassword() throws Exception {
        PasswordReset passwordReset = new PasswordReset(jwtTokenService.getResetToken(user3.getEmail()), "test");
        mvc.perform(post("/api/users/reset")
                .content(new ObjectMapper().writeValueAsString(passwordReset))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    @Test
    public void shouldNotResetPassword() throws Exception {
        PasswordReset passwordReset = new PasswordReset(jwtTokenService.getResetToken("notexistingemail@mail.com"), "test");
        mvc.perform(post("/api/users/reset")
                .content(new ObjectMapper().writeValueAsString(passwordReset))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }
}
