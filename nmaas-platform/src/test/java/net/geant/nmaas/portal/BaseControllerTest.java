package net.geant.nmaas.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;

@ContextConfiguration(classes = {ApiSecurityConfig.class, ConvertersConfig.class, PersistentConfig.class})
@TestPropertySource("classpath:db.properties")
public class BaseControllerTest {

	protected final static String ADMIN_USERNAME = "admin";
	protected final static String ADMIN_PASSWORD = "admin";
	
    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected Filter springSecurityFilterChain;

    protected MockMvc mvc;
	
    @Autowired
	protected JWTTokenService tokenService;
    
    @Autowired
    DomainService domains;
    
	public BaseControllerTest() {
		
	}
	
	protected MockMvc createMVC() {
		mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
		return mvc;
	}
	
	protected void prepareSecurity() {
		SecurityContext context = SecurityContextHolder.createEmptyContext();		
		context.setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN_USERNAME, null, Arrays.asList(new SimpleGrantedAuthority(Role.ROLE_SUPERADMIN.authority()))));
		SecurityContextHolder.setContext(context);
	}
	
    protected String getValidUserTokenFor(Role role) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		User admin = new User(ADMIN_USERNAME, true, ADMIN_PASSWORD, domains.getGlobalDomain().get(), roles);
		
		String token = tokenService.getToken(admin);
		
		return token;
    }
	
}
