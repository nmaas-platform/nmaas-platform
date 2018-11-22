package net.geant.nmaas.portal.persistent;

import javax.validation.ConstraintViolationException;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes={PersistentConfig.class}/* loader = AnnotationConfigContextLoader.class, */ )
@EnableAutoConfiguration
@Transactional
@Rollback
public class UserRepositoryTest {

	final static String DOMAIN = "domain";
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	DomainService domains;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		domains.createGlobalDomain();
		domains.createDomain(DOMAIN, DOMAIN);
		userRepository.deleteAll();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldCreateTwoUsersOneWithRoleUserAndSecondWithRoleSystemAdminAndAddSecondUserRoleUser() {
		User tester = new User("tester", true, "test123", domains.findDomain(DOMAIN).get(), Role.ROLE_USER);
		tester.setEmail("test@test.com");
		User admin = new User("testadmin", true, "testadmin123", domains.getGlobalDomain().get(), Role.ROLE_SYSTEM_ADMIN);
		admin.setEmail("admin@test.com");
		admin.getRoles().add(new UserRole(admin, domains.findDomain(DOMAIN).get(), Role.ROLE_USER));
		userRepository.save(tester);
		userRepository.save(admin);
		
		
		assertEquals(2, userRepository.count());
		
		Optional<User> adminPersisted = userRepository.findByUsername("testadmin");
		assertNotNull(adminPersisted.get());
		assertNotNull(adminPersisted.get().getId());
		assertEquals(2, adminPersisted.get().getRoles().size());
		
	}

	@Test
	public void shouldSetEnabledFlag(){
		User enableTestUser = new User("enableTest", false, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER);
		enableTestUser.setEmail("enableUser@test.com");
		userRepository.save(enableTestUser);

		Optional<User> enableTestUserFalse = userRepository.findByUsername("enableTest");
		assertNotNull(enableTestUserFalse.get());
		assertFalse(enableTestUserFalse.get().isEnabled());

		userRepository.setEnabledFlag(enableTestUserFalse.get().getId(), true);

		Optional<User> enableTestUserTrue = userRepository.findByUsername("enableTest");
		assertNotNull(enableTestUserTrue.get());
		assertTrue(enableTestUserTrue.get().isEnabled());

		userRepository.delete(enableTestUserTrue.get());
	}

	@Test
	public void shouldSetTermsOfUseAcceptedFlag(){
		User termsOfUseAcceptedTestUser = new User("termsTest", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, false, true);
		termsOfUseAcceptedTestUser.setEmail("terms@email.com");
		userRepository.save(termsOfUseAcceptedTestUser);

		Optional<User> termsOfUseAcceptedTestUserFalse = userRepository.findByUsername("termsTest");
		assertNotNull(termsOfUseAcceptedTestUserFalse.get());
		assertFalse(termsOfUseAcceptedTestUserFalse.get().isTermsOfUseAccepted());

		userRepository.setTermsOfUseAcceptedFlag(termsOfUseAcceptedTestUserFalse.get().getId(), true);

		Optional<User> termsOfUseAcceptedTestUserTrue = userRepository.findByUsername("termsTest");
		assertNotNull(termsOfUseAcceptedTestUserTrue.get());
		assertTrue(termsOfUseAcceptedTestUserTrue.get().isTermsOfUseAccepted());

		userRepository.delete(termsOfUseAcceptedTestUserTrue.get());
	}

	@Test
	public void shouldSetPrivacyPolicyAcceptedFlag(){
		User privacyPolicyAcceptedTestUser = new User("privacyTest", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		privacyPolicyAcceptedTestUser.setEmail("privacy@test.com");
		userRepository.save(privacyPolicyAcceptedTestUser);

		Optional<User> privacyPolicyAcceptedTestUserFalse = userRepository.findByUsername("privacyTest");
		assertNotNull(privacyPolicyAcceptedTestUserFalse.get());
		assertFalse(privacyPolicyAcceptedTestUserFalse.get().isPrivacyPolicyAccepted());

		userRepository.setPrivacyPolicyAcceptedFlag(privacyPolicyAcceptedTestUserFalse.get().getId(), true);

		Optional<User> privacyPolicyAcceptedTestUserTrue = userRepository.findByUsername("privacyTest");
		assertNotNull(privacyPolicyAcceptedTestUserTrue.get());
		assertTrue(privacyPolicyAcceptedTestUserTrue.get().isPrivacyPolicyAccepted());

		userRepository.delete(privacyPolicyAcceptedTestUserTrue.get());
	}

	@Test
	public void shouldSaveUserWithMail(){
		User testUser = new User("testUser", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		testUser.setEmail("email@email.com");
		userRepository.save(testUser);
	}

	@Test(expected = ConstraintViolationException.class)
	public void shouldNotSaveUserWithWrongMailFormat(){
		User testUser = new User("testUser", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		testUser.setEmail("emailemail.com");
		userRepository.save(testUser);
	}

	@Test
	public void shouldSaveUserWithSamlToken(){
		User testUser = new User("testUser", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		testUser.setSamlToken("test|1234|saml");
		userRepository.save(testUser);
	}

	@Test(expected = ConstraintViolationException.class)
	public void shouldNotSaveUserWithoutBothMailAndToken(){
		User testUser = new User("testUser", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		userRepository.save(testUser);
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void shouldNotSaveUserWithNonUnique(){
		User testUser = new User("testUser", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		testUser.setEmail("test@test.com");
		userRepository.save(testUser);
		User testUser2 = new User("testUser2", true, "test123",
				domains.findDomain(DOMAIN).get(), Role.ROLE_USER, true, false);
		testUser2.setEmail("test@test.com");
		userRepository.save(testUser2);
	}
}
