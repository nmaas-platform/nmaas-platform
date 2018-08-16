package net.geant.nmaas.portal.api.market;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.NewUserRequest;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@Transactional(value=TxType.REQUIRES_NEW)
@Rollback
public class UsersControllerTest extends BaseControllerTest {

	final static String DOMAIN = "DOMAIN";

	@Autowired
	UserRepository userRepo;

	@Autowired
	UsersController userController;

	@Autowired
	DomainService domains;

	User user1 = null;

	@Before
	public void setUp() throws Exception {
		mvc = createMVC();

		domains.createGlobalDomain();
		domains.createDomain(DOMAIN, DOMAIN);

		//Add extra users, default admin is already there
		userRepo.save(new User("manager", true, "manager", domains.getGlobalDomain().get(), Arrays.asList(Role.ROLE_TOOL_MANAGER)));
		user1 = userRepo.save(new User("user1", true, "user1", domains.findDomain(DOMAIN).get(), Arrays.asList(Role.ROLE_USER)));
		userRepo.save(new User("user2", true, "user2", domains.findDomain(DOMAIN).get(), Arrays.asList(Role.ROLE_USER)));


		prepareSecurity();
	}

	@After
	public void tearDown() throws Exception {
		user1 = null;
	}

	@Test
	public void testGetUsers() {
		assertEquals(4, userController.getUsers(null).size());
	}

	@Test
	public void testGetRoles() {
		assertEquals(7, userController.getRoles().size());
	}

	@Test
	public void testAddUser() throws SignupException {
		Id id = userController.addUser(new NewUserRequest("tester"));
		assertNotNull(id);

		assertEquals(5, userController.getUsers(null).size());
	}

	@Test
	public void testGetUser() throws MissingElementException {
		net.geant.nmaas.portal.api.domain.User user = userController.retrieveUser(1L);
		assertEquals(new Long(1), user.getId());
		assertEquals("admin", user.getUsername());

	}

	@Test
	public void testSuccessUpdatingWithNonExistingUsername() throws ProcessingException, MissingElementException {
		String oldUsername = user1.getUsername();
		String newUsername = "newUser1";
		userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, newUsername, null), null);

		User modUser1 = userRepo.findOne(user1.getId());
		assertEquals(newUsername, modUser1.getUsername());
	}

	@Test
	public void testFailureUpdatingWithExistingUsername() throws MissingElementException {
		String oldUsername = user1.getUsername();
		String newUsername = "admin";
		try {
			userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, newUsername, null), null);
			fail("There should not be two users with the same username.");
		} catch (ProcessingException e) {

		}
	}

	@Test
	public void testUpdateUserPasswordAndRole() throws ProcessingException, MissingElementException {
		String newPass = "newPass";
		String oldPass = user1.getPassword();
		userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, user1.getUsername(), newPass), null);
		User modUser1 = userRepo.findOne(user1.getId());

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
	public void testGetMessageWhenUserUpdated(){
        Role role1 = Role.ROLE_USER;
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), role1);

        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole1);
        net.geant.nmaas.portal.persistent.entity.User user = new User("user1");
        user.setFirstname("FirstName");
        user.setLastname("Lastname");
        user.setEmail("email@email.com");
        user.setRoles(userRoles);
        user.setEnabled(true);

        UserRequest userRequest = new UserRequest(2L, "user2", "password");
        userRequest.setEmail("email1@email.com");
        userRequest.setFirstname("FirstName1");
        userRequest.setLastname("LastName1");

        String message = userController.getMessageWhenUserUpdated(user, userRequest);
        assertEquals("\n" +
                "||| Username changed from - user1 to - user2|||\n" +
                "||| Email changed from - email@email.com to - email1@email.com|||\n" +
                "||| First name changed from - FirstName to - FirstName1|||\n" +
                "||| Last name changed from - Lastname to - LastName1|||\n" +
                "||| Enabled flag changed from - true to - false|||\n" +
                "||| Role changed from - ROLE_USER to - |||", message);
    }

}
