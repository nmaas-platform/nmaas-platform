package net.geant.nmaas.portal;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;

public class BaseControllerTest {

	protected final static String USERNAME = "tester";
	protected final static String PASSWORD = "tester123";
	
    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected Filter springSecurityFilterChain;

    protected MockMvc mvc;
	
    @Autowired
	protected JWTTokenService tokenService;
    
	public BaseControllerTest() {
		
	}
	
	protected MockMvc createMVC() {
		mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
		return mvc;
	}
	
    protected String getValidUserTokenFor(Role role) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		User tester = new User(USERNAME, PASSWORD, roles);
		
		String token = tokenService.getToken(tester);
		
		return token;
    }
	
}
