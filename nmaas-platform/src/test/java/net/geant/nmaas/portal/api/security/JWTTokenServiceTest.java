package net.geant.nmaas.portal.api.security;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.SecurityConfig;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;


@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes={SecurityConfig.class})
public class JWTTokenServiceTest {

	@Autowired
	JWTTokenService tokenService;
	
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
	public void testToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ADMIN);
		roles.add(Role.USER);
		User tester = new User("tester", "test123", roles);
		
		String token = tokenService.getToken(tester);
		assertNotNull(token);
		
		Claims claims = tokenService.getClaims(token);
		Object scopes = claims.get("scopes");
		assertNotNull(scopes);
		assertTrue(scopes instanceof List<?>);
		assertEquals(2, ((List<SimpleGrantedAuthority>)scopes).size());
	}

	@Test
	public void testInvalidToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ADMIN);
		roles.add(Role.USER);
		User tester = new User("tester", "test123", roles);
		
		String token = tokenService.getToken(tester);
		assertNotNull(token);

		try {
			Jwts.parser().setSigningKey("invalidKey").parse(token);
			fail("Signed token has been valideted with invalid key");
		} catch(SignatureException e) {
			
		}
	}
	
	@Test
	public void testValidateRefreshToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ADMIN);
		roles.add(Role.USER);
		User tester = new User("tester", "test123", roles);

		String refreshToken = tokenService.getRefreshToken(tester);
		assertNotNull(refreshToken);
		
		assertTrue(tokenService.validateRefreshToken(refreshToken));
				
	}
	
}
