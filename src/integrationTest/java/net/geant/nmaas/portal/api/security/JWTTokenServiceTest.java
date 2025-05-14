package net.geant.nmaas.portal.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JWTTokenServiceTest {

	private static final String DOMAIN = "DOMAIN";
	@Autowired
	JWTTokenService tokenService;
	
	@Test
	public void testToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ROLE_SYSTEM_ADMIN);
		roles.add(Role.ROLE_USER);
		User tester = new User("tester", true, "test123", new Domain(DOMAIN, DOMAIN), roles);
		
		String token = tokenService.getToken(tester);
		assertNotNull(token);
		
		Claims claims = tokenService.getClaims(token);
		Object tokenRoles = claims.get("roles");
		assertNotNull(tokenRoles);
		assertTrue(tokenRoles instanceof List<?>);
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = (List<Map<String,String>>)tokenRoles;
		assertEquals(2, list.size());
	}

	@Test
	public void testInvalidToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ROLE_SYSTEM_ADMIN);
		roles.add(Role.ROLE_USER);
		User tester = new User("tester", true, "test123", new Domain(DOMAIN, DOMAIN), roles);
		
		String token = tokenService.getToken(tester);
		assertNotNull(token);

		String invalidKey = "tslswE0MQnRWWuc6RXeA4jJNgugdsFtstP3xMShYD0FD2yCj9TIeJ0QlJrfLKvjcXdu2O5T81VZd6C51ThnyTTgNuDCDoLMchFIZ8sbxyvsY5Z83yNkbEpE9ObsxthTRYUxFlNhsZaOQX2xlMjHyK81Q0BOqLOyFKuWqYCSNtPsUn3YmfzeIJmDLpIdL6A6rmKksfR8C5hUi6LNeHm1InuaHmoFHMdG7SuaquRWYETO1jit3z6Ozn7oq26IuBgwKDKLCOiKzQ00F1ePkU8Wmh00LhS3J21TwLouK3jbxUmvQABM5VBUH0BlWdOwjKkasA3lM5uCIK7QTvDtdeHtgdJ605GrMtxCTUrdkDpFVUkJoYjuE9JUPKSZphOZUZbnXAfD0X7FOaTxlSRSXX1u0I2FwwOale07KGpttpbQ55p2WWKasqKwgihxoZjDnasfiDBHWGgp2EAPpmHVDecFb8YrmeYfJ8h8glrnPbpTdloxGAi4fjxd37qskaDz1bLF4";

		try {
			Jwts.parser().setSigningKey(invalidKey).build().parseSignedClaims(token);
			fail("Signed token has been valideted with invalid key");
		} catch(SignatureException e) {
			
		}
	}
	
	@Test
	public void testValidateRefreshToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ROLE_SYSTEM_ADMIN);
		roles.add(Role.ROLE_USER);
		User tester = new User("tester", true, "test123", new Domain(DOMAIN, DOMAIN), roles);

		String refreshToken = tokenService.getRefreshToken(tester);
		assertNotNull(refreshToken);
		
		assertTrue(tokenService.validateRefreshToken(refreshToken));
	}
	
}
