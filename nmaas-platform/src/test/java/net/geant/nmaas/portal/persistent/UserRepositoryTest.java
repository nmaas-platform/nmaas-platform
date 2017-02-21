package net.geant.nmaas.portal.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes={PersistentConfig.class}/* loader = AnnotationConfigContextLoader.class, */ )
@Transactional()
@Rollback
public class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		User tester = new User("tester", "test123", Role.USER);
		User admin = new User("admin", "admin123", Role.ADMIN);
		admin.getRoles().add(new UserRole(admin, Role.USER));
		userRepository.save(tester);
		userRepository.save(admin);
		
		
		assertEquals(2, userRepository.count());
		
		Optional<User> adminPersisted = userRepository.findByUsername("admin");
		assertNotNull(adminPersisted.get());
		assertNotNull(adminPersisted.get().getId());
		assertEquals(2, adminPersisted.get().getRoles().size());
		
	}

	
}
