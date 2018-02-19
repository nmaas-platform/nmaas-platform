package net.geant.nmaas.portal.api.market;

import static org.junit.Assert.*;

import java.util.Arrays;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

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
import net.geant.nmaas.portal.api.auth.UserSignup;
import net.geant.nmaas.portal.api.domain.Id;
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
		domains.createDomain(DOMAIN);
		
		//Add extra users, default admin is already there
		userRepo.save(new User("manager", "manager", domains.getGlobalDomain(), Arrays.asList(Role.ROLE_TOOL_MANAGER)));
		user1 = userRepo.save(new User("user1", "user1", domains.findDomain(DOMAIN), Arrays.asList(Role.ROLE_USER)));
		userRepo.save(new User("user2", "user2", domains.findDomain(DOMAIN), Arrays.asList(Role.ROLE_USER)));

		
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
		assertEquals(5, userController.getRoles().size());
	}

	@Test
	public void testAddUser() throws SignupException {
		Id id = userController.addUser(new UserSignup("tester", "tester", null));
		assertNotNull(id);
		
		assertEquals(5, userController.getUsers(null).size());
	}

	@Test
	public void testGetUser() {
		net.geant.nmaas.portal.api.domain.User user = userController.getUser(1L);
		assertEquals(new Long(1), user.getId());
		assertEquals("admin", user.getUsername());
		
	}

	@Test
	public void testSuccessUpdatingWithNonExistingUsername() throws ProcessingException {
		String oldUsername = user1.getUsername();
		String newUsername = "newUser1";
		userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, newUsername, null));
		
		User modUser1 = userRepo.findOne(user1.getId());
		assertEquals(newUsername, modUser1.getUsername());
	}
	
	@Test
	public void testFailureUpdatingWithExistingUsername() {
		String oldUsername = user1.getUsername();
		String newUsername = "admin";
		try {
			userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, newUsername, null));
			fail("There should not be two users with the same username.");
		} catch (ProcessingException e) {
			
		}		
	}	
	
	@Test
	public void testUpdateUserPasswordAndRole() throws ProcessingException {
		String newPass = "newPass";
		String oldPass = user1.getPassword();
		userController.updateUser(user1.getId(), new net.geant.nmaas.portal.api.domain.UserRequest(null, user1.getUsername(), newPass));
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

}
