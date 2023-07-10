package net.geant.nmaas.portal.api.auth;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BasicAuthControllerIntTest extends BaseControllerTestSetup {

    @BeforeEach
    void setup() {
        mvc = createMVC();
    }
	
    @Test
    void shouldLoginSuccessfully() {
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/auth/basic/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\": \"admin\",\"password\": \"admin\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldNotLoginWithWrongCredentials() {
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/auth/basic/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\": \"admin1\",\"password\": \"admin1\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        });
    }

    @Test
    void shouldAuthPingSuccessfully() {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/auth/basic/ping")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(content().string(containsString(ADMIN_USERNAME)))
                    .andExpect(status().isOk());
        });
    }
	
    @Test
    void shouldNotAuthPing() {
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/auth/basic/ping"))
                    .andExpect(status().is4xxClientError());
        });
    }

}