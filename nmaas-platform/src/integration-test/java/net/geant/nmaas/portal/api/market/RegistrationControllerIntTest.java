package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.utils.captcha.CaptchaValidator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RegistrationControllerIntTest extends BaseControllerTestSetup {

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CaptchaValidator captchaValidator;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mvc = createMVC();
        objectMapper = new ObjectMapper();
        when(captchaValidator.verifyToken(anyString())).thenReturn(true);
    }

    @AfterEach
    public void teardown(){
        userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equalsIgnoreCase(UsersHelper.ADMIN.getUsername()))
                .forEach(user -> userRepository.delete(user));
    }

    @Test
    @Transactional
    public void testSuccessfulRegistration() throws Exception {
    	mvc.perform(post("/api/auth/basic/registration?token=test-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(getDefaultRegistration()))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
    	assertTrue(userRepository.existsByUsername("testUser"));
    }

    private Registration getDefaultRegistration(){
        Registration user = new Registration("testUser");
        user.setEmail("test@test.com");
        user.setPassword(RandomStringUtils.random(10, true, false));
        user.setPrivacyPolicyAccepted(true);
        user.setTermsOfUseAccepted(true);
        user.setFirstname(RandomStringUtils.random(5, true, false));
        user.setLastname(RandomStringUtils.random(5, true, false));
        return user;
    }
}