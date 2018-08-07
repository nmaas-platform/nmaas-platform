package net.geant.nmaas.portal.auth.basic;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.Role;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BasicAuthControllerIntegrationTest extends BaseControllerTest {
    @Autowired
    private BasicAuthController basicAuthController;

    @Before
    public void setup() {
        mvc = createMVC();
    }
	
    @Test
    public void testSuccessfulLogin() throws Exception {
    	
    	mvc.perform(post("/api/auth/basic/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\": \"admin\",\"password\": \"admin\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
    }

    @Test
    public void testUnsuccessfulLoginWithWrongCredentials() throws Exception {

        mvc.perform(post("/portal/api/auth/basic/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"admin1\",\"password\": \"admin1\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testSuccessAuthPing() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);

        mvc.perform(get("/portal/api/auth/basic/ping")
                .header("Authorization", "Bearer " + token))
                .andExpect(content().string(containsString(ADMIN_USERNAME)))
                .andExpect(status().isOk());

    }
	
    @Test
    public void testFailedAuthPing() throws Exception {    	
    	mvc.perform(get("/portal/api/auth/basic/ping"))
    				.andExpect(status().is4xxClientError());
    		
    }
}