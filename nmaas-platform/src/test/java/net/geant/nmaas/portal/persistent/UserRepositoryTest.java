package net.geant.nmaas.portal.persistent;

import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
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
	public void test() {
		User tester = new User("tester", true, "test123", domains.findDomain(DOMAIN).get(), Role.ROLE_USER);
		User admin = new User("testadmin", true, "testadmin123", domains.getGlobalDomain().get(), Role.ROLE_SUPERADMIN);
		admin.getRoles().add(new UserRole(admin, domains.findDomain(DOMAIN).get(), Role.ROLE_USER));
		userRepository.save(tester);
		userRepository.save(admin);
		
		
		assertEquals(2, userRepository.count());
		
		Optional<User> adminPersisted = userRepository.findByUsername("testadmin");
		assertNotNull(adminPersisted.get());
		assertNotNull(adminPersisted.get().getId());
		assertEquals(2, adminPersisted.get().getRoles().size());
		
	}

	
}
