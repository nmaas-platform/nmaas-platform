package net.geant.nmaas.portal.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JWTTokenServiceTest {

	private static final String DOMAIN = "DOMAIN";
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
		roles.add(Role.ROLE_SUPERADMIN);
		roles.add(Role.ROLE_USER);
		User tester = new User("tester", true, "test123", new Domain(DOMAIN, DOMAIN), roles);
		
		String token = tokenService.getToken(tester);
		assertNotNull(token);
		
		Claims claims = tokenService.getClaims(token);
		Object scopes = claims.get("scopes");
		assertNotNull(scopes);
		assertTrue(scopes instanceof List<?>);
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = (List<Map<String,String>>)scopes;
		assertEquals(2, list.size());
		
	}

	@Test
	public void testInvalidToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ROLE_SUPERADMIN);
		roles.add(Role.ROLE_USER);
		User tester = new User("tester", true, "test123", new Domain(DOMAIN, DOMAIN), roles);
		
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
		roles.add(Role.ROLE_SUPERADMIN);
		roles.add(Role.ROLE_USER);
		User tester = new User("tester", true, "test123", new Domain(DOMAIN, DOMAIN), roles);

		String refreshToken = tokenService.getRefreshToken(tester);
		assertNotNull(refreshToken);
		
		assertTrue(tokenService.validateRefreshToken(refreshToken));
				
	}
	
}
