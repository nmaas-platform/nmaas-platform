package net.geant.nmaas.portal.auth.basic;

import javax.servlet.Filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class BasicAuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mvc;

    @Autowired
	JWTTokenService tokenService;
    
    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }
	
    
    public String getToken() {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.ADMIN);
		roles.add(Role.USER);
		User tester = new User("tester", "test123", roles);
		
		String token = tokenService.getToken(tester);
		
		return token;
    }

    @Test
    public void testSuccessAuthPing() throws Exception {
    	String token = getToken();
    	
    	mvc.perform(get("/portal/api/auth/basic/ping")
    				.header("Authorization", "Bearer " + token))
    				.andExpect(content().string(containsString("tester")))
    				.andExpect(status().isOk());
    		
    }
	
    @Test
    public void testFailedAuthPing() throws Exception {    	
    	mvc.perform(get("/portal/api/auth/basic/ping"))
    				.andExpect(status().is4xxClientError());
    		
    }
	
}
